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

import audit.AuditDescriptors._
import audit.{AuditDescriptors, AuditModel, AuditService}
import config.FrontendAppConfig
import controllers.actions.TaxYearAction.taxYearAction
import controllers.actions._
import models.requests.OptionalDataRequest
import models.{Done, SectionState, UserAnswers}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.{AddSectionsService, TaskListDataService, UserDataService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.AddSectionsViewModel
import views.html.{AddSectionsAgentView, AddSectionsView}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddSectionsController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       identify: IdentifierActionProvider,
                                       getData: DataRetrievalActionProvider,
                                       addSectionsService: AddSectionsService,
                                       taskListDataService: TaskListDataService,
                                       auditService: AuditService,
                                       appConfig:FrontendAppConfig,
                                       userDataService: UserDataService,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: AddSectionsView,
                                       agentView: AddSectionsAgentView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging with AuditDescriptors {

  def onPageLoad(taxYear: Int): Action[AnyContent] = (identify(taxYear) andThen taxYearAction(taxYear) andThen getData(taxYear)) {
    implicit request =>

      val state = addSectionsService.getState(request.userAnswers)

      if (request.isAgent) {
        Ok(agentView(taxYear, AddSectionsViewModel(state, taxYear, "addSections.agent")))
      } else {
        Ok(view(taxYear, AddSectionsViewModel(state, taxYear, "addSections")))
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

    val IS_COMPLETED: String = "isCompleted"
    val IS_UPDATE: String = "isUpdate"

    val affinityGroup: String = if (request.isAgent) {
      "agent"
    } else {
      "individual"
    }

    val ua: UserAnswers = request.userAnswers.getOrElse(UserAnswers(request.mtdItId, taxYear))

    if (vm.isComplete) {

      val isPreviouslyCompleted: Boolean = ua.data.value.contains(IS_COMPLETED)
      val isUpdate: Boolean = isPreviouslyCompleted && (ua.data.value(IS_UPDATE) == Json.toJson(true))

      val uaWithCompletedStatus: UserAnswers =
        ua.copy(data = JsObject(ua.data.fields ++ Seq(IS_COMPLETED -> Json.toJson("completed"))))

      (isPreviouslyCompleted, isUpdate) match {
        case (true, true) =>
          submitAudit(UserDataUpdatedType.toString, UserDataUpdatedTransaction.toString, uaWithCompletedStatus, ua.mtdItId, affinityGroup, taxYear)
        case (false, _) =>
          submitAudit(UserDataCompleteType.toString,
            UserDataCompleteTransaction.toString,
            // Remove isUpdate field for completed event as it is not needed here.
            uaWithCompletedStatus.copy(data = JsObject(uaWithCompletedStatus.data.fields.filterNot(_._1 == IS_UPDATE))),
            ua.mtdItId,
            affinityGroup,
            taxYear)
        case _ =>
          logger.info("[AddSectionsController][handleAudit] No audit was submitted as user data has not been modified.")
          Future.successful(AuditResult.Success)
      }
      userDataService.setWithoutUpdate(uaWithCompletedStatus.copy(data = JsObject(uaWithCompletedStatus.data.fields ++ Seq(IS_UPDATE -> Json.toJson(false)))))

      saveTaskListData(ua)

    } else {

      val uaWithStatus = ua.copy(data = JsObject(ua.data.fields ++ Seq("sectionStatus" -> Json.toJson(state.getStatus), IS_UPDATE -> Json.toJson(false))))

      submitAudit(UserDataIncompleteType.toString, UserDataIncompleteTransaction.toString, uaWithStatus, ua.mtdItId, affinityGroup, taxYear)
      Future.successful(Redirect(controllers.routes.TaxReturnNotReadyController.onPageLoad(taxYear)))
    }
  }

  private def saveTaskListData(ua: UserAnswers)(implicit hc: HeaderCarrier): Future[Result] = {
    taskListDataService.set(ua).map {
      case Done => Redirect(appConfig.submissionFrontendTaskListRedirect(ua.taxYear))
    }
  }

  private def submitAudit(auditName: String,
                          transactionName: String,
                          ua: UserAnswers,
                          mtdItId: String,
                          affinityGroup: String,
                          taxYear: Int
                         )(implicit hc: HeaderCarrier): Future[AuditResult] =
    auditService.auditModel(
      AuditModel(auditName, transactionName, ua.data, mtdItId, affinityGroup, taxYear)
    )
}
