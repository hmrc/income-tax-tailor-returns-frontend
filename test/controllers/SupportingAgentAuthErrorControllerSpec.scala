/*
 * Copyright 2025 HM Revenue & Customs
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
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.SupportingAgentAuthErrorView

import java.time.LocalDate

class SupportingAgentAuthErrorControllerSpec extends SpecBase {

  "Unauthorised Controller" - {

    "must return UNAUTHORIZED and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, routes.SupportingAgentAuthErrorController.show.url).withSession("TAX_YEAR" -> "2024")

        val result = route(application, request).value

        val view = application.injector.instanceOf[SupportingAgentAuthErrorView]

        status(result) mustEqual UNAUTHORIZED
        contentAsString(result) mustEqual view(2024)(request, messages(application), config(application)).toString
      }
    }
  }

  "must return UNAUTHORIZED and the correct view for a GETxx" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, routes.SupportingAgentAuthErrorController.show.url)
        val result = route(application, request).value
        val view = application.injector.instanceOf[SupportingAgentAuthErrorView]

        val currentYear = LocalDate.now().getYear

        status(result) mustEqual UNAUTHORIZED
        contentAsString(result) mustEqual view(currentYear)(request, messages(application), config(application)).toString
      }
    }

}
