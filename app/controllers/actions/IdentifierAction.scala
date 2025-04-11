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
import models.Enrolment.SupportingAgent
import models.authorisation.DelegatedAuthRules.{agentDelegatedAuthRule, supportingAgentDelegatedAuthRule}
import models.authorisation.Enrolment.{Individual, Nino}
import models.requests.IdentifierRequest
import play.api.mvc.Results._
import play.api.mvc._
import services.SessionDataService
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals._
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.auth.core.{Enrolment => HMRCEnrolment, _}
import uk.gov.hmrc.http.{HeaderCarrier, SessionKeys}
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.{Logging, SessionDataHelper}

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
                                              config: FrontendAppConfig,
                                              val sessionDataService: SessionDataService,
                                              val parser: BodyParsers.Default)
                                             (implicit val executionContext: ExecutionContext)
  extends IdentifierAction with AuthorisedFunctions with Logging with SessionDataHelper {

  protected val primaryContext: String = "AuthenticatedIdentifierAction"

  private val unauthorized: Future[Result] = Future.successful(Unauthorized)

  private lazy val logInRedirectResult = Redirect(config.loginUrl(taxYear))
  private lazy val logInRedirectFutureResult = Future.successful(logInRedirectResult)

  protected[actions] def getEnrolmentValueOpt(checkedKey: String,
                                              checkedIdentifier: String,
                                              enrolments: Enrolments): Option[String] =
    enrolments.enrolments.collectFirst {
      case HMRCEnrolment(`checkedKey`, enrolmentIdentifiers, _, _) => enrolmentIdentifiers.collectFirst {
        case EnrolmentIdentifier(`checkedIdentifier`, identifierValue) => identifierValue
      }
    }.flatten

  protected[actions] def sessionIdBlock(extraLoggingContext: String,
                                        errorLogString: String,
                                        errorAction: Future[Result])
                                       (block: String => Future[Result])
                                       (implicit request: Request[_], hc: HeaderCarrier): Future[Result] = {
    val methodLoggingContext: String = "sessionIdBlock"

    val infoLogger: String => Unit = infoLog(
      secondaryContext = methodLoggingContext,
      extraContext = Some(extraLoggingContext)
    )

    val warnLogger: String => Unit = warnLog(
      secondaryContext = methodLoggingContext,
      extraContext = Some(extraLoggingContext)
    )

    infoLogger("Attempting to retrieve session ID from request")

    hc.sessionId.map(_.value).orElse(request.headers.get(SessionKeys.sessionId)) match {
      case Some(sessionId) =>
        infoLogger("Session ID retrieved successfully. Invoking block for request")
        block(sessionId)
      case None =>
        warnLogger(errorLogString)
        errorAction
    }
  }

  override def invokeBlock[A](request: Request[A],
                              block: IdentifierRequest[A] => Future[Result]): Future[Result] = {
    val methodLoggingContext: String = "invokeBlock"

    val infoLogger: String => Unit = infoLog(methodLoggingContext)
    val warnLogger: String => Unit = warnLog(methodLoggingContext)
    val errorLogger: String => Unit = errorLog(methodLoggingContext)

    implicit lazy val headerCarrier: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(
      request = request,
      session = request.session
    )

    implicit val implRequest: Request[A] = request

    infoLogger("Received request to check authentication for user")

    val successLogString: String = "Authentication succeeded for"
    val errorLogString: String = "User authentication failed with reason:"

    authorised().retrieve(affinityGroup and allEnrolments and confidenceLevel) {
      case Some(AffinityGroup.Individual) ~ enrolments ~ confidenceLevel =>
        infoLogger(s"$successLogString Individual user. Attempting secondary checks")
        nonAgentAuthentication(block, enrolments, confidenceLevel, methodLoggingContext)
      case Some(AffinityGroup.Organisation) ~ enrolments ~ confidenceLevel =>
        infoLogger(s"$successLogString Organisation user. Attempting secondary checks")
        nonAgentAuthentication(block, enrolments, confidenceLevel, methodLoggingContext)
      case Some(AffinityGroup.Agent) ~ enrolments ~ _ =>
        infoLogger(s"$successLogString Agent user. Attempting secondary checks")
        agentAuth(block, enrolments)
      case _ =>
        errorLogger(s"$errorLogString Missing affinity group. Returning an error status")
        unauthorized
    }.recover {
      case _: NoActiveSession =>
        warnLogger(s"$errorLogString No active session. Redirecting to sign in")
        logInRedirectResult
      case e =>
        /*
        * TODO - This wildcard case will catch any unhandled exceptions which occur during the block action.
        *        This is probably an unintended side effect, and causes some confusing errors to occur.
        *
        *        i.e - When a call to IF fails due to a timeout we may see a 401 error returned from this action
        *              rather than a more appropriate response (500 INTERNAL_SERVER_ERROR view in this case)
        *
        *        We may want to address this in future to find out why the error handler is not working properly
        * */
        errorLogger(s"Request failed with unhandled exception: ${e.getMessage}. Returning an error status")
        Unauthorized
    }
  }

  protected[actions] def nonAgentAuthentication[A](block: IdentifierRequest[A] => Future[Result],
                                                   enrolments: Enrolments,
                                                   confidenceLevel: ConfidenceLevel,
                                                   extraLoggingContext: String)
                                                  (implicit request: Request[A], hc: HeaderCarrier): Future[Result] = {
    val methodLoggingContext: String = "nonAgentAuthentication"

    val infoLogger: String => Unit = infoLog(
      secondaryContext = methodLoggingContext,
      extraContext = Some(extraLoggingContext)
    )

    val warnLogger: String => Unit = warnLog(
      secondaryContext = methodLoggingContext,
      extraContext = Some(extraLoggingContext)
    )

    infoLogger("Received request to complete additional checks for non-agent user")

    if (confidenceLevel.level >= ConfidenceLevel.L250.level) {
      infoLogger("Non-agent user has sufficient confidence levels. Checking for session and enrolment data")

      val optionalMtdItId: Option[String] = getEnrolmentValueOpt(Individual.key, Individual.value, enrolments)
      val optionalNino: Option[String] = getEnrolmentValueOpt(Nino.key, Nino.value, enrolments)

      (optionalMtdItId, optionalNino) match {
        case (Some(mtdItId), Some(nino)) =>
          infoLogger("Appropriate enrolments located for user. Checking for session ID in request")
          sessionIdBlock(
            extraLoggingContext = methodLoggingContext,
            errorLogString = "No session ID was found for the request. Redirecting user to login",
            errorAction = logInRedirectFutureResult
          )(sessionId => block(IdentifierRequest(request, nino, mtdItId, sessionId, utr = "", isAgent = false)))
        case (_, None) =>
          warnLogger("Could not find HMRC-NI enrolment for user. Redirecting user to login")
          logInRedirectFutureResult
        case (None, _) =>
          warnLogger("Could not find MTD-IT enrolment for user. Redirecting to MTD sign-up")
          Future.successful(Redirect(config.signUpUrlIndividual))
      }
    } else {
      warnLogger("Non-agent user has insufficient confidence levels. Redirecting to IV uplift")
      Future.successful(Redirect(config.incomeTaxSubmissionIvRedirect))
    }
  }

  private def predicate(mtdId: String): Predicate =
    HMRCEnrolment(Individual.key)
      .withIdentifier(Individual.value, mtdId)
      .withDelegatedAuthRule(agentDelegatedAuthRule)

  private def secondaryAgentPredicate(mtdId: String): Predicate =
    HMRCEnrolment(SupportingAgent.key)
      .withIdentifier(SupportingAgent.value, mtdId)
      .withDelegatedAuthRule(supportingAgentDelegatedAuthRule)

  protected[actions] def agentAuth[A](block: IdentifierRequest[A] => Future[Result],
                                      enrolments: Enrolments)
                                     (implicit request: Request[A], hc: HeaderCarrier): Future[Result] = {
    val methodLoggingContext: String = "agentAuth"

    val infoLogger: String => Unit = infoLog(methodLoggingContext)
    val warnLogger: String => Unit = warnLog(methodLoggingContext)

    infoLogger("Received request to complete additional checks for agent user. Checking for agent enrolment")

    getSessionDataBlock(
      errorAction = () => Future.successful(Redirect(config.viewAndChangeEnterUtrUrl))
    )(sessionData => {
      getEnrolmentValueOpt(Enrolment.Agent.key, Enrolment.Agent.value, enrolments) match {
        case Some(_) =>
          authorised(predicate(sessionData.mtditid)) {
            infoLogger("Agent authentication completed successfully for user. Invoking block for request")
            block(IdentifierRequest(request, sessionData, isAgent = true))
          }.recoverWith {
            agentRecovery(sessionData.mtditid)
          }
        case None =>
          warnLogger("Could not find HMRC-AS-AGENT enrolment for user. Redirecting to agent set-up")
          Future.successful(Redirect(config.setUpAgentServicesAccountUrl()))
      }
    })
  }

  private def agentRecovery(mtdItId: String)
                           (implicit hc: HeaderCarrier): PartialFunction[Throwable, Future[Result]] = {
    val methodLoggingContext: String = "agentRecovery"

    val warnLogger: String => Unit = warnLog(
      secondaryContext = methodLoggingContext,
      extraContext = Some("invokeBlock")
    )

    val errorLogger: String => Unit = errorLog(
      secondaryContext = methodLoggingContext,
      extraContext = Some("invokeBlock")
    )

    val agentErrorString = "Agent authentication failed with reason:"
    val supportingAgentErrorString = "Supporting agent authentication failed with reason:"

    {
      case _: NoActiveSession =>
        warnLogger(s"$agentErrorString No active session. Redirecting to V&C")
        Future(Redirect(config.viewAndChangeEnterUtrUrl))
      case e: AuthorisationException =>
        warnLogger(s"$agentErrorString ${e.getMessage}. Checking for supporting agent credentials")
        authorised(secondaryAgentPredicate(mtdItId)) {
          warnLogger("Agent user is a supporting agent. Redirecting to supporting agent error page")
          Future.successful(Redirect(controllers.routes.SupportingAgentAuthErrorController.show.url))
        }.recoverWith {
          case _: AuthorisationException =>
            errorLogger(s"$supportingAgentErrorString No secondary delegated authority. Returning an error status")
            Future(Unauthorized)
          case _ =>
            errorLogger(s"$supportingAgentErrorString Unhandled error. Returning an error status")
            Future(InternalServerError)
        }
      case e =>
        errorLogger(s"Agent authentication failed with unhandled exception: ${e.getMessage}")
        Future(InternalServerError)
    }
  }
}

class EarlyPrivateLaunchIdentifierActionProviderImpl @Inject()(authConnector: AuthConnector,
                                                               sessionDataService: SessionDataService,
                                                               config: FrontendAppConfig,
                                                               parser: BodyParsers.Default)(implicit executionContext: ExecutionContext)
  extends IdentifierActionProvider {

  def apply(taxYear: Int): IdentifierAction = new EarlyPrivateLaunchIdentifierAction(taxYear)(
    authConnector = authConnector,
    sessionDataService = sessionDataService,
    config = config,
    parser = parser
  )
}

class EarlyPrivateLaunchIdentifierAction @Inject()(taxYear: Int)
                                                  (override val authConnector: AuthConnector,
                                                   val sessionDataService: SessionDataService,
                                                   config: FrontendAppConfig,
                                                   val parser: BodyParsers.Default)
                                                  (implicit val executionContext: ExecutionContext)
  extends IdentifierAction with AuthorisedFunctions with Logging with SessionDataHelper {

  protected val primaryContext: String = "EarlyPrivateLaunchIdentifierAction"

  override def invokeBlock[A](request: Request[A],
                              block: IdentifierRequest[A] => Future[Result]): Future[Result] = {
    val methodLoggingContext: String = "invokeBlock"

    implicit lazy val headerCarrier: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(
      request = request,
      session = request.session
    )

    implicit val implRequest: Request[A] = request

    val infoLogger: String => Unit = infoLog(methodLoggingContext)
    val warnLogger: String => Unit = warnLog(methodLoggingContext)
    val errorLogger: String => Unit = errorLog(methodLoggingContext)

    infoLogger("Received request to check authorisation for EarlyPrivateLaunch user")

    getSessionDataBlock(
      errorAction = () => Future.successful(Redirect(config.viewAndChangeEnterUtrUrl))
    )(
      block = sessionData => {
        authorised().retrieve(internalId) {
          case Some(_) =>
            infoLogger("Authentication succeeded for user. Invoking block for request")
            block(IdentifierRequest(request, sessionData, isAgent = false))
          case _ =>
            errorLogger("User authentication failed with reason: Missing affinity group. Returning an error status")
            Future.successful(Unauthorized)
        }.recover {
          case _: NoActiveSession =>
            warnLogger("User authentication failed with reason: No active session. Redirecting to sign in")
            Redirect(config.loginUrl(taxYear))
          case e =>
            errorLogger(s"Request failed with unhandled exception: ${e.getMessage}. Returning an error status")
            Unauthorized
        }
      })
  }
}
