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

package controllers

import audit.{AuditDetail, AuditModel, AuditService}
import controllers.actions.TaxYearAction.taxYearAction
import controllers.actions._
import models.requests.OptionalDataRequest
import models.{SectionState, UserAnswers}
import org.joda.time.{DateTime, DateTimeZone}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.AddSectionsService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.AddSectionsViewModel
import views.html.{AddSectionsAgentView, AddSectionsView}

import javax.inject.Inject
import scala.collection.immutable.Seq
import scala.concurrent.{ExecutionContext, Future}

class AddSectionsController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       identify: IdentifierActionProvider,
                                       getData: DataRetrievalActionProvider,
                                       addSectionsService: AddSectionsService,
                                       auditService: AuditService,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: AddSectionsView,
                                       agentView: AddSectionsAgentView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(taxYear: Int): Action[AnyContent] = (identify(taxYear) andThen taxYearAction(taxYear) andThen getData(taxYear)) {
    implicit request =>

      val prefix: String = if (request.isAgent) {
        "addSections.agent"
      } else {
        "addSections"
      }

      val state = addSectionsService.getState(request.userAnswers)

      val vm = AddSectionsViewModel(state, taxYear, prefix)

      if (request.isAgent) {
        Ok(agentView(taxYear, vm))
      } else {
        Ok(view(taxYear, vm))
      }
  }

  def onSubmit(taxYear: Int): Action[AnyContent] =
    (identify(taxYear) andThen taxYearAction(taxYear) andThen getData(taxYear)).async {
      implicit request =>

        val state = addSectionsService.getState(request.userAnswers)

        val vm: AddSectionsViewModel = AddSectionsViewModel(state, taxYear, "")

        handleAudit(taxYear, vm, state)
    }

  private def handleAudit(taxYear: Int, vm: AddSectionsViewModel, state: SectionState)(implicit request: OptionalDataRequest[AnyContent]): Future[Result] = {

    val affinityGroup = if (request.isAgent) {
      "Agent"
    } else {
      "Individual"
    }

    if (vm.isComplete) {
      val ua: UserAnswers = request.userAnswers.get

      sendAuditEvent("CompleteTailoring",
        "CompleteTailoring",
        ua,
        "nino",
        ua.mtdItId,
        affinityGroup,
        taxYear)

      Future.successful(Redirect(controllers.routes.TaskListController.onPageLoad(taxYear)))
    } else {
      val sectionStatus = Seq(
        s"About you: ${state.aboutYou}",
        s"Income from work: ${state.incomeFromWork}",
        s"Income from property: ${state.incomeFromProperty}",
        s"Pensions: ${state.pensions}"
      )

      val ua: scala.collection.Seq[(String, JsValue)] =
        request.userAnswers.getOrElse(UserAnswers(request.mtdItId, taxYear)).data.fields ++ Seq("sectionStatus" -> Json.toJson(sectionStatus))

      val uaWithStatus = request.userAnswers.getOrElse(UserAnswers(request.mtdItId, taxYear))
        .copy(data = JsObject(ua))

      sendAuditEvent("IncompleteTailoring",
        "incompleteTailoring",
        uaWithStatus,
        "nino",
        uaWithStatus.mtdItId,
        affinityGroup,
        taxYear)

      Future.successful(Redirect(controllers.routes.TaxReturnNotReadyController.onPageLoad(taxYear)))
    }
  }

  private def sendAuditEvent(auditName: String,
                             transactionName: String,
                             ua: UserAnswers,
                             nino: String,
                             mtdItId: String,
                             affinityGroup: String,
                             taxYear: Int)(implicit hc: HeaderCarrier): Future[AuditResult] =
    auditService.auditModel(
      AuditModel(auditName, transactionName, AuditDetail(ua.data, nino, mtdItId, affinityGroup, taxYear))
    )
}
