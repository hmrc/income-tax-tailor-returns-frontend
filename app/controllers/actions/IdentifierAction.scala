/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.actions

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.SessionDataConnector
import models.Enrolment
import models.SessionValues.CLIENT_MTDITID
import models.requests.IdentifierRequest
import models.session.SessionData
import play.api.Logging
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals._
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.auth.core.{Enrolment => HMRCEnrolment, _}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

trait IdentifierAction extends ActionBuilder[IdentifierRequest, AnyContent] with ActionFunction[Request, IdentifierRequest]

trait IdentifierActionProvider {
  def apply(taxYear: Int): IdentifierAction
}

class IdentifierActionProviderImpl @Inject()(authConnector: AuthConnector,
                                             config: FrontendAppConfig,
                                             sessionDataConnector: SessionDataConnector,
                                             parser: BodyParsers.Default)(implicit executionContext: ExecutionContext)
  extends IdentifierActionProvider {

  def apply(taxYear: Int): IdentifierAction = new AuthenticatedIdentifierAction(taxYear)(authConnector, config, sessionDataConnector, parser)
}

class AuthenticatedIdentifierAction @Inject()(taxYear: Int)
                                             (override val authConnector: AuthConnector,
                                              config: FrontendAppConfig,
                                              sessionDataConnector: SessionDataConnector,
                                              val parser: BodyParsers.Default)
                                             (implicit val executionContext: ExecutionContext)
  extends IdentifierAction with AuthorisedFunctions with Logging {

  private val unauthorized: Future[Result] = Future.successful(Unauthorized)

  private def getMtdItId(enrolments: Enrolments): Option[String] = {
    for {
      enrolment <- enrolments.getEnrolment(Enrolment.MtdIncomeTax.key)
      id <- enrolment.getIdentifier(Enrolment.MtdIncomeTax.value)
    } yield id.value
  }

  private def authorisedAgentServices(enrolments: Enrolments): Option[String] = {
    for {
      agentEnrolment <- enrolments.getEnrolment(Enrolment.Agent.key)
      arn <- agentEnrolment.getIdentifier(Enrolment.Agent.value)
    } yield arn.value
  }


  override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {

    implicit lazy val headerCarrier: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorised().retrieve(affinityGroup and allEnrolments and confidenceLevel) {
      case Some(AffinityGroup.Individual) ~ enrolments ~ confidenceLevel =>
        authorized(request, block, enrolments, confidenceLevel)
      case Some(AffinityGroup.Organisation) ~ enrolments ~ confidenceLevel =>
        authorized(request, block, enrolments, confidenceLevel)
      case Some(AffinityGroup.Agent) ~ enrolments ~ _ =>
        agentAuth(request, block, enrolments)
      case _ =>
        logger.info(s"[AuthorisedAction][async] - User failed to authenticate no affinityGroup")
        unauthorized
    }.recover {
      case _: NoActiveSession =>
        logger.info(s"[AuthorisedAction][async] - No active session. Redirecting to sign in")
        Redirect(config.loginUrl(taxYear))
      case e =>
        logger.info(s"[AuthorisedAction][async][recover] - User failed to authenticate ${e.getMessage}")
        Unauthorized
    }
  }

  private def authorized[A](request: Request[A], block: IdentifierRequest[A] => Future[Result],
                            enrolments: Enrolments, confidenceLevel: ConfidenceLevel): Future[Result] = {
    if (confidenceLevel.level >= ConfidenceLevel.L250.level) {
      getMtdItId(enrolments) match {
        case Some(mtdItId) =>
          block(IdentifierRequest(request, mtdItId, isAgent = false))
        case None =>
          logger.error("User did not have MTDITID Enrolment")
          Future.successful(Redirect(config.signUpUrlIndividual))
      }
    } else {
      Future.successful(Redirect(config.incomeTaxSubmissionIvRedirect))
    }
  }

  private def getSessionData(request: Request[_])(implicit hc: HeaderCarrier): Future[Option[SessionData]] = {
    if (config.sessionCookieServiceEnabled) {
      sessionDataConnector.getSessionData.map {
        case Left(_) =>
          //TODO
          // It was decided with HMRC to read from session cookie as a fallback option, unless all are other services are
          // migrated to read from V&C sesion service. This is to not impact users behaviour
          request.session.get(CLIENT_MTDITID).map(value => SessionData.empty.copy(mtditid = value))
        case Right(value) => value
      }
    } else {
      Future.successful {
        request.session.get(CLIENT_MTDITID).map(value => SessionData.empty.copy(mtditid = value))
      }
    }
  }

  def predicate(mtdId: String): Predicate =
    HMRCEnrolment("HMRC-MTD-IT")
      .withIdentifier("MTDITID", mtdId)
      .withDelegatedAuthRule("mtd-it-auth")

  private def secondaryAgentPredicate(mtdId: String): Predicate =
    HMRCEnrolment("HMRC-MTD-IT-SUPP")
      .withIdentifier("MTDITID", mtdId)
      .withDelegatedAuthRule("mtd-it-auth-supp")

  private def agentAuth[A](request: Request[A], block: IdentifierRequest[A] => Future[Result], enrolments: Enrolments)
                          (implicit hc: HeaderCarrier): Future[Result] = {

    getSessionData(request).flatMap {
      case Some(sessionData) =>
        authorisedAgentServices(enrolments) match {
          case Some(_) =>
            authorised(predicate(sessionData.mtditid)) {
              block(IdentifierRequest(request, sessionData.mtditid, isAgent = true))
            }.recoverWith(agentRecovery(request, block, sessionData.mtditid))
          case None =>
            logger.warn("User did not have HMRC-AS-AGENT enrolment ")
            Future.successful(Redirect(config.setUpAgentServicesAccountUrl))
        }
      case None =>
        logger.warn("Session data not found")
        Future.successful(Redirect(config.viewAndChangeEnterUtrUrl))
    }
  }

  private def agentRecovery[A](request: Request[A],
                               block: IdentifierRequest[A] => Future[Result],
                               mtdItId: String)
                              (implicit hc: HeaderCarrier): PartialFunction[Throwable, Future[Result]] = {
    case _: NoActiveSession =>
      logger.info(s"[AuthorisedAction][agentAuthentication] - No active session. Redirecting to ${config.viewAndChangeEnterUtrUrl}")
      Future(Redirect(config.viewAndChangeEnterUtrUrl))
    case _: AuthorisationException =>
      if (config.emaSupportingAgentsEnabled) {
        authorised(secondaryAgentPredicate(mtdItId)) {
          block(IdentifierRequest(request, mtdItId, isAgent = true, isSecondaryAgent = true))
        }.recoverWith {
          case _: AuthorisationException =>
            logger.info(s"[AuthorisedAction][agentAuthentication] - Agent does not have secondary delegated authority for Client.")
            Future(Unauthorized)
          case _ =>
            logger.info(s"[AuthorisedAction][agentAuthentication] - Downstream service error.")
            Future(InternalServerError)
        }
      } else {
        logger.info(s"[AuthorisedAction][agentAuthentication] - Agent does not have delegated authority for Client.")
        Future.successful(Redirect(config.signUpUrlAgent))
      }
  }
}

class EarlyPrivateLaunchIdentifierActionProviderImpl @Inject()(authConnector: AuthConnector,
                                                               config: FrontendAppConfig,
                                                               parser: BodyParsers.Default)(implicit executionContext: ExecutionContext)
  extends IdentifierActionProvider {

  def apply(taxYear: Int): IdentifierAction = new EarlyPrivateLaunchIdentifierAction(taxYear)(authConnector, config, parser)
}

class EarlyPrivateLaunchIdentifierAction @Inject()(taxYear: Int)
                                                  (override val authConnector: AuthConnector,
                                                   config: FrontendAppConfig,
                                                   val parser: BodyParsers.Default)
                                                  (implicit val executionContext: ExecutionContext)
  extends IdentifierAction with AuthorisedFunctions with Logging {

  override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {

    implicit lazy val headerCarrier: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorised().retrieve(internalId) {
      case Some(_) => block(IdentifierRequest(request, "1234567890", isAgent = false))
      case _ =>
        logger.info(s"[AuthorisedAction][async] - User failed to authenticate no affinityGroup")
        Future.successful(Unauthorized)
    }.recover {
      case _: NoActiveSession =>
        logger.info(s"[AuthorisedAction][async] - No active session. Redirecting to sign in")
        Redirect(config.loginUrl(taxYear))
      case e =>
        logger.info(s"[AuthorisedAction][async][recover] - User failed to authenticate ${e.getMessage}")
        Unauthorized
    }
  }
}
