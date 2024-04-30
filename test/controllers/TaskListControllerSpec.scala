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

import base.SpecBase
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.TaskListPageViewModel
import views.html.TaskListView
import views.html.TaskListAgentView


class TaskListControllerSpec extends SpecBase {

  private implicit val messages: Messages = stubMessages()

  private val vm = TaskListPageViewModel(fullUserAnswers, "taskList")

  private val agentVm = TaskListPageViewModel(fullUserAnswers, "taskList.agent")

  "TaskList Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(fullUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.TaskListController.onPageLoad(taxYear).url).withSession(validTaxYears)

        val result = route(application, request).value

        val view = application.injector.instanceOf[TaskListView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(taxYear, vm)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET for an agent" in {

      val application = applicationBuilder(userAnswers = Some(fullUserAnswers), isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, routes.TaskListController.onPageLoad(taxYear).url).withSession(validTaxYears)

        val result = route(application, request).value

        val view = application.injector.instanceOf[TaskListAgentView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(taxYear, agentVm)(request, messages(application)).toString
      }
    }
  }
}
