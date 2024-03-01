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
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.{AddSectionsService, UserDataService}
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
                                       userDataService: UserDataService,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: AddSectionsView,
                                       agentView: AddSectionsAgentView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(taxYear: Int): Action[AnyContent] = (identify(taxYear) andThen taxYearAction(taxYear) andThen getData(taxYear)) {
    implicit request =>
      val state = addSectionsService.getState(request.userAnswers)
      request.isAgent match {
        case true => Ok(agentView(taxYear, AddSectionsViewModel(state, taxYear, "addSections.agent")))
        case false => Ok(view(taxYear, AddSectionsViewModel(state, taxYear, "addSections")))
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

    val affinityGroup = if (request.isAgent) "Agent" else "Individual"
    val ua: UserAnswers = request.userAnswers.getOrElse(UserAnswers(request.mtdItId, taxYear))

    if (vm.isComplete) {
      def isUpdated: Boolean = ua.data.value.contains("isCompleted")
      val userAnsWithCompletedStatus = ua.copy(data = JsObject(ua.data.fields ++ Seq("isCompleted" -> Json.toJson("completed"))))

      userDataService.set(userAnsWithCompletedStatus)

      sendAuditEvent("CompleteTailoring",
        "complete-tailoring",
        userAnsWithCompletedStatus,
        ua.mtdItId,
        affinityGroup,
        taxYear,
        isUpdated)

      Future.successful(Redirect(controllers.routes.TaskListController.onPageLoad(taxYear)))
    } else {
      val userAnswersWithSectionStatus = ua.data.fields ++ Seq("sectionStatus" -> Json.toJson(state.getStatus))
      val uaWithStatus = ua.copy(data = JsObject(userAnswersWithSectionStatus))

      sendAuditEvent("IncompleteTailoring",
        "incomplete-tailoring",
        uaWithStatus,
        uaWithStatus.mtdItId,
        affinityGroup,
        taxYear)

      Future.successful(Redirect(controllers.routes.TaxReturnNotReadyController.onPageLoad(taxYear)))
    }
  }

  private def sendAuditEvent(auditName: String,
                             transactionName: String,
                             ua: UserAnswers,
                             mtdItId: String,
                             affinityGroup: String,
                             taxYear: Int,
                             isUpdated: Boolean = false
                            )(implicit hc: HeaderCarrier): Future[AuditResult] =
    auditService.auditModel(
      AuditModel(auditName, transactionName, AuditDetail(ua.data, isUpdated, mtdItId, affinityGroup, taxYear))
    )
}
