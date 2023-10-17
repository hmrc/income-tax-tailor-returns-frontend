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
import models.SessionValues.CLIENT_MTDITID
import models.requests.IdentifierRequest
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

class IdentifierActionProviderImpl @Inject() (authConnector: AuthConnector,
                                              config: FrontendAppConfig,
                                              parser: BodyParsers.Default)(implicit executionContext: ExecutionContext)
  extends IdentifierActionProvider {

  def apply(taxYear: Int): IdentifierAction = new AuthenticatedIdentifierAction(taxYear)(authConnector, config, parser)
}

class AuthenticatedIdentifierAction @Inject()(taxYear: Int)
                                             (override val authConnector: AuthConnector,
                                              config: FrontendAppConfig,
                                              val parser: BodyParsers.Default)
                                             (implicit val executionContext: ExecutionContext)
  extends IdentifierAction with AuthorisedFunctions with Logging{


  private val unauthorized: Future[Result] = Future.successful(Unauthorized)

  private def authorisedForMtdItId(enrolments: Enrolments): Option[String] = {
    for {
      enrolment <- enrolments.getEnrolment(Enrolment.MtdIncomeTax.key)
      id <- enrolment.getIdentifier(Enrolment.MtdIncomeTax.value)
    } yield id.value
  }

  private def authorisedForMtdItId(mtditid: String, enrolments: Enrolments): Option[String] = {
    //  todo possible check for "mtd-it-auth" rule
    enrolments.enrolments.find(x => x.identifiers.exists(i => i.value.equals(mtditid)))
      .flatMap(_.getIdentifier(Enrolment.MtdIncomeTax.value)).map(_.value)
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
            individualAuth(request, block, enrolments, confidenceLevel)
          case Some(AffinityGroup.Agent) ~ enrolments ~ _ =>
            agentAuth(request, block, enrolments)
          case _ =>
            logger.info(s"[AuthorisedAction][async] - User failed to authenticate")
            unauthorized
        }.recover {
          case _ =>
            logger.info(s"[AuthorisedAction][async] - User failed to authenticate")
            Unauthorized
        }
  }

  private def individualAuth[A](request: Request[A], block: IdentifierRequest[A] => Future[Result],
                                enrolments: Enrolments, confidenceLevel: ConfidenceLevel) = {
    authorisedForMtdItId(enrolments) match {
      case Some(mtdItId) =>
        if (confidenceLevel.level >= ConfidenceLevel.L250.level) {
          block(IdentifierRequest(request, mtdItId, isAgent = false))
        }else {
           Future.successful(Redirect(config.incomeTaxSubmissionIvRedirect))
        }
      case None =>
        logger.warn("User did not have MTDITID Enrolment")
        unauthorized
    }
  }

  private def agentAuth[A](request: Request[A], block: IdentifierRequest[A] => Future[Result], enrolments: Enrolments) = {
    request.session.get(CLIENT_MTDITID) match {
      case Some(mtdItId) =>
        authorisedAgentServices(enrolments) match {
          case Some(_) =>
            if (authorisedForMtdItId(mtdItId, enrolments).isDefined) {
              block(IdentifierRequest(request, mtdItId, isAgent = true))
            }
            else {
              logger.warn("User is not authorized for mtdItId")
              unauthorized
            }
          case _ =>
            // TODO redirect to agent services
            logger.warn("User did not have ARN")
            unauthorized
        }
      case None =>
        logger.warn("User did not have MTDID in session")
        // TODO redirect to View & Change to fix user session
        unauthorized
    }
  }
}
