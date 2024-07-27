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
import connectors.IncomeTaxSessionDataConnector
import models.Enrolment
import models.SessionValues.CLIENT_MTDITID
import models.requests.IdentifierRequest
import models.session.SessionData
import play.api.Logging
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals._
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

trait IdentifierAction extends ActionBuilder[IdentifierRequest, AnyContent] with ActionFunction[Request, IdentifierRequest]

trait IdentifierActionProvider {
  def apply(taxYear: Int): IdentifierAction
}

class IdentifierActionProviderImpl @Inject()(authConnector: AuthConnector,
                                             config: FrontendAppConfig,
                                             sessionDataConnector: IncomeTaxSessionDataConnector,
                                             parser: BodyParsers.Default)(implicit executionContext: ExecutionContext)
  extends IdentifierActionProvider {

  def apply(taxYear: Int): IdentifierAction = new AuthenticatedIdentifierAction(taxYear)(authConnector, config, sessionDataConnector, parser)
}

class AuthenticatedIdentifierAction @Inject()(taxYear: Int)
                                             (override val authConnector: AuthConnector,
                                              config: FrontendAppConfig,
                                              sessionDataConnector: IncomeTaxSessionDataConnector,
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

  private def authorisedForMtdItId(mtdItId: String, enrolments: Enrolments): Option[String] = {
    //  todo possible check for "mtd-it-auth" rule
    enrolments.enrolments.find(x => x.identifiers.exists(i => i.value.equals(mtdItId)))
      .flatMap(_.getIdentifier(Enrolment.MtdIncomeTax.value)).map(_.value)
  }

  private def authorisedAgentServices(enrolments: Enrolments): Option[String] = {
    for {
      agentEnrolment <- enrolments.getEnrolment(Enrolment.Agent.key)
      arn <- agentEnrolment.getIdentifier(Enrolment.Agent.value)
    } yield arn.value
  }

  override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {

    //TODO removing details from session to session service, it will have impact on headercarrier. revist??This might contain only seesionId?
    implicit lazy val headerCarrier: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    //TODO do we need to check mtditid against Nino?
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

  private def sessionId(request: Request[_])(implicit hc: HeaderCarrier): Option[String] = {
    lazy val key = "X-Session-ID"
    hc.sessionId.map(_.value).orElse(request.headers.get(key))
  }

  private def getSessionData(sessionId: String,request: Request[_])(implicit hc:HeaderCarrier):  Future[Option[SessionData]] = {
    if(config.sessionCookieServiceEnabled){
      sessionDataConnector.getSessionData(sessionId).map(_.toOption.flatten)
    }else{
      Future.successful {
        request.session.get(CLIENT_MTDITID).map(value => SessionData.empty.copy(mtditid = value))
      }
    }
  }

  private def agentAuth[A](request: Request[A], block: IdentifierRequest[A] => Future[Result], enrolments: Enrolments)
                          (implicit hc: HeaderCarrier): Future[Result] = {
    sessionId(request) match {
      case Some(sessionId) =>
        getSessionData(sessionId,request).flatMap {
          case Some(sessionData) =>
            authorisedAgentServices(enrolments) match {
              case Some(_) =>
                if (authorisedForMtdItId(sessionData.mtditid, enrolments).isDefined) {
                  block(IdentifierRequest(request, sessionData.mtditid, isAgent = true))
                } else {
                  logger.warn("User is not authorized for mtdItId")
                  Future.successful(Redirect(config.signUpUrlAgent))
                }
              case None =>
                logger.warn("User did not have ARN")
                Future.successful(Redirect(config.setUpAgentServicesAccountUrl))
            }
          case None =>
            logger.warn("Session data not found")
            Future.successful(Redirect(config.signUpUrlAgent))
        }
      case None =>
        logger.warn("User did not have MTDID in session")
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
