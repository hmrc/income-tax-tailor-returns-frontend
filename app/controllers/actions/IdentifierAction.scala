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
import models.Enrolment
import models.authorisation.Enrolment.{Individual, Nino}
import models.errors.MissingAgentClientDetails
import models.requests.IdentifierRequest
import play.api.mvc.Results._
import play.api.mvc._
import services.SessionDataService
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals._
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.EnrolmentHelper.getEnrolmentValueOpt
import utils.{EnrolmentHelper, Logging, SessionIdHelper}

import scala.concurrent.{ExecutionContext, Future}

trait IdentifierAction extends ActionBuilder[IdentifierRequest, AnyContent] with ActionFunction[Request, IdentifierRequest]

trait IdentifierActionProvider {
  def apply(taxYear: Int): IdentifierAction
}

class IdentifierActionProviderImpl @Inject()(authConnector: AuthConnector,
                                             config: FrontendAppConfig,
                                             sessionDataService: SessionDataService,
                                             parser: BodyParsers.Default)
                                            (implicit executionContext: ExecutionContext)
  extends IdentifierActionProvider {

  def apply(taxYear: Int): IdentifierAction = new AuthenticatedIdentifierAction(taxYear)(
    authConnector = authConnector,
    config = config,
    sessionDataService = sessionDataService,
    parser = parser
  )
}

class AuthenticatedIdentifierAction @Inject()(taxYear: Int)
                                             (override val authConnector: AuthConnector,
                                              val config: FrontendAppConfig,
                                              val sessionDataService: SessionDataService,
                                              val parser: BodyParsers.Default)
                                             (implicit val executionContext: ExecutionContext)
  extends IdentifierAction with AuthorisedFunctions with SessionIdHelper with Logging {

  override def invokeBlock[A](request: Request[A],
                              block: IdentifierRequest[A] => Future[Result]): Future[Result] = {
    val ctx: String = "invokeBlock"

    implicit val implRequest: Request[A] = request
    implicit lazy val headerCarrier: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    withSessionId(taxYear) { sessionId =>
      authorised().retrieve(affinityGroup and allEnrolments and confidenceLevel) {
        case Some(AffinityGroup.Agent) ~ enrolments ~ _ =>
          agentAuth(block, enrolments, sessionId)
        case Some(_) ~ enrolments ~ confidenceLevel =>
          nonAgentAuthentication(block, enrolments, confidenceLevel, sessionId)
        case _ =>
          errorLog(ctx)("User authentication failed with reason: Missing affinity group. Returning an error status")
          Future.successful(Unauthorized)
      }.recover {
        case _: NoActiveSession =>
          warnLog(ctx)(s"User authentication failed with reason: No active session. Redirecting to sign in")
          Redirect(config.loginUrl(taxYear))
        case e: AuthorisationException =>
          errorLog(ctx)(s"Request failed with authorisation exception: ${e.getMessage}. Returning Unauthorised status")
          Unauthorized
        case e =>
          errorLog(ctx)(s"Request failed with unhandled exception: ${e.getMessage}. Returning Internal Server Error status")
          InternalServerError
      }
    }
  }

  protected[actions] def nonAgentAuthentication[A](block: IdentifierRequest[A] => Future[Result],
                                                   enrolments: Enrolments,
                                                   confidenceLevel: ConfidenceLevel,
                                                   sessionId: String)
                                                  (implicit request: Request[A]): Future[Result] = {
    val ctx: String = "nonAgentAuthentication"

    if (confidenceLevel.level >= ConfidenceLevel.L250.level) {
      (
        getEnrolmentValueOpt(Individual.key, Individual.value, enrolments),
        getEnrolmentValueOpt(Nino.key, Nino.value, enrolments)
      ) match {
        case (Some(mtdItId), Some(nino)) =>
          block(IdentifierRequest(request, nino, mtdItId, sessionId, isAgent = false))
        case (_, None) =>
          warnLog(ctx)("Could not find HMRC-NI enrolment for user. Redirecting user to login")
          Future.successful(Redirect(config.loginUrl(taxYear)))
        case (None, _) =>
          warnLog(ctx)("Could not find MTD-IT enrolment for user. Redirecting to MTD sign-up")
          Future.successful(Redirect(config.signUpUrlIndividual))
      }
    } else {
      warnLog(ctx)("Non-agent user has insufficient confidence levels. Redirecting to IV uplift")
      Future.successful(Redirect(config.incomeTaxSubmissionIvRedirect))
    }
  }

  protected[actions] def agentAuth[A](block: IdentifierRequest[A] => Future[Result],
                                      enrolments: Enrolments,
                                      sessionId: String)
                                     (implicit request: Request[A], hc: HeaderCarrier): Future[Result] = {
    val ctx: String = "agentAuth"

    sessionDataService.getSessionData(sessionId).flatMap { sessionData =>
      getEnrolmentValueOpt(Enrolment.Agent.key, Enrolment.Agent.value, enrolments) match {
        case Some(_) =>
          authorised(EnrolmentHelper.primaryAgentPredicate(sessionData.mtditid)) {
            block(IdentifierRequest(request, sessionData, isAgent = true))
          }.recoverWith {
            agentRecovery(sessionData.mtditid)
          }
        case None =>
          warnLog(ctx)("Could not find HMRC-AS-AGENT enrolment for user. Redirecting to agent set-up")
          Future.successful(Redirect(config.setUpAgentServicesAccountUrl()))
      }
    }.recover {
      case _: MissingAgentClientDetails =>
        Redirect(config.viewAndChangeEnterUtrUrl)
    }
  }

  private def agentRecovery(mtdItId: String)
                           (implicit hc: HeaderCarrier): PartialFunction[Throwable, Future[Result]] = {
    val ctx: String = "agentRecovery"

    {
      case _: NoActiveSession =>
        warnLog(ctx)("Agent authentication failed with reason: No active session. Redirecting to V&C")
        Future(Redirect(config.viewAndChangeEnterUtrUrl))
      case e: AuthorisationException =>
        warnLog(ctx)(s"Agent authentication failed with reason: ${e.getMessage}. Checking for supporting agent credentials")
        authorised(EnrolmentHelper.secondaryAgentPredicate(mtdItId)) {
          warnLog(ctx)("Agent user is a supporting agent. Redirecting to supporting agent error page")
          Future.successful(Redirect(controllers.routes.SupportingAgentAuthErrorController.show.url))
        }.recoverWith {
          case _: AuthorisationException =>
            errorLog(ctx)("Supporting agent authentication failed with reason: No secondary delegated authority. Returning an error status")
            Future(Unauthorized)
          case _ =>
            errorLog(ctx)("Supporting agent authentication failed with reason: Unhandled error. Returning an error status")
            Future(InternalServerError)
        }
      case e =>
        errorLog(ctx)(s"Agent authentication failed with unhandled exception: ${e.getMessage}")
        Future(InternalServerError)
    }
  }
}
