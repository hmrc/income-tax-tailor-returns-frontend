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
import models.SectionState
import models.TagStatus.{CannotStartYet, Completed, NotStarted}
import play.api.Logging
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.AddSectionsViewModel
import views.html.{AddSectionsAgentView, AddSectionsView}


class AddSectionsControllerSpec extends SpecBase with Logging {

  private val addSectionsKey: String = "addSections"
  private val addSectionsAgentKey: String = "addSections.agent"
  private val taskListUrl : String = s"$submissionFrontendBaseUrl/$taxYear/tasklist"
  private def vmIncomplete(key: String) = AddSectionsViewModel(SectionState(NotStarted, CannotStartYet, CannotStartYet, NotStarted), taxYear, key)
  private def vmComplete(key: String) = AddSectionsViewModel(SectionState(Completed, Completed, Completed, Completed), taxYear, key)

  "AddSections Controller" - {

    "must return OK and the correct view for a GET with incomplete sections" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.AddSectionsController.onPageLoad(taxYear).url).withSession(validTaxYears)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AddSectionsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(taxYear, vmIncomplete(addSectionsKey))(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET when all sections are complete" in {

      val application = applicationBuilder(userAnswers = Some(fullUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.AddSectionsController.onPageLoad(taxYear).url).withSession(validTaxYears)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AddSectionsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(taxYear, vmComplete(addSectionsKey))(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET with incomplete sections for an agent" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, routes.AddSectionsController.onPageLoad(taxYear).url).withSession(validTaxYears)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AddSectionsAgentView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(taxYear, vmIncomplete(addSectionsAgentKey))(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET when all sections are complete for an agent" in {

      val application = applicationBuilder(userAnswers = Some(fullUserAnswers), isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, routes.AddSectionsController.onPageLoad(taxYear).url).withSession(validTaxYears)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AddSectionsAgentView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(taxYear, vmComplete(addSectionsAgentKey))(request, messages(application)).toString
      }
    }

    "must redirect to task list page and submit a completed audit event for an individual" in {

      val application = applicationBuilder(userAnswers = Some(fullUserAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, routes.AddSectionsController.onSubmit(taxYear).url).withSession(validTaxYears)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        await(result).header.headers.get("Location").head.contains(taskListUrl) mustBe true
      }
    }

    "must redirect to task list page and submit a completed audit event for an agent" in {

      val application = applicationBuilder(userAnswers = Some(fullUserAnswers), isAgent = true).build()

      running(application) {
        val request = FakeRequest(POST, routes.AddSectionsController.onSubmit(taxYear).url).withSession(validTaxYears)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        await(result).header.headers.get("Location").head.contains(taskListUrl) mustBe true
      }
    }

    "must redirect to task list page and submit an update audit event for an agent" in {

      val application = applicationBuilder(userAnswers = Some(
        fullUserAnswers.copy(data = JsObject(fullUserAnswers.data.fields ++ Seq("isCompleted" -> Json.toJson("completed"), "isUpdate" -> Json.toJson(true))))
      ), isAgent = true).build()

      running(application) {
        val request = FakeRequest(POST, routes.AddSectionsController.onSubmit(taxYear).url).withSession(validTaxYears)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        await(result).header.headers.get("Location").head.contains(taskListUrl) mustBe true
      }
    }

    "must redirect to task list page without submitting an event if no data has changed for an agent" in {

      val application = applicationBuilder(userAnswers = Some(
        fullUserAnswers.copy(data = JsObject(fullUserAnswers.data.fields ++ Seq("isCompleted" -> Json.toJson("completed"), "isUpdate" -> Json.toJson(false))))
      ), isAgent = true).build()

      running(application) {
        val request = FakeRequest(POST, routes.AddSectionsController.onSubmit(taxYear).url).withSession(validTaxYears)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        await(result).header.headers.get("Location").head.contains(taskListUrl) mustBe true
      }
    }

    "must redirect to tax return not ready page and submit an incomplete audit event for an individual" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, routes.AddSectionsController.onSubmit(taxYear).url).withSession(validTaxYears)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        await(result).header.headers.get("Location").head.contains("/tax-return-not-ready") mustBe true
      }
    }

    "must redirect to tax return not ready page and submit an incomplete audit event for an agent" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()

      running(application) {
        val request = FakeRequest(POST, routes.AddSectionsController.onSubmit(taxYear).url).withSession(validTaxYears)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        await(result).header.headers.get("Location").head.contains("/tax-return-not-ready") mustBe true
      }
    }

    "must redirect to tax return not ready page and submit an incomplete audit event for an individual with no userAnswers" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(POST, routes.AddSectionsController.onSubmit(taxYear).url).withSession(validTaxYears)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        await(result).header.headers.get("Location").head.contains("/tax-return-not-ready") mustBe true
      }
    }
  }
}