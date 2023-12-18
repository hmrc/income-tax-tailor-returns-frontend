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

import controllers.actions.TaxYearAction.taxYearAction
import controllers.actions._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.TaskListPageViewModel
import views.html.{TaskListAgentView, TaskListView}

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class TaskListController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       identify: IdentifierActionProvider,
                                       getData: DataRetrievalActionProvider,
                                       requireData: DataRequiredActionProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: TaskListView,
                                       agentView: TaskListAgentView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int): Action[AnyContent] =
    (identify(taxYear) andThen taxYearAction(taxYear) andThen getData(taxYear) andThen requireData(taxYear)) {
    implicit request =>

      val prefix: String = if (request.isAgent) {
        "taskList.agent"
      } else {
        "taskList"
      }

      val vm = TaskListPageViewModel(request.userAnswers, prefix)

      if (request.isAgent) {
        Ok(agentView(taxYear, vm))
      } else {
        Ok(view(taxYear, vm))
      }
  }

}
