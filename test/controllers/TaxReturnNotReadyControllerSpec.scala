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
import play.api.test.FakeRequest
import play.api.test.Helpers._
import models.SectionState
import models.TagStatus._
import viewmodels.TaxReturnNotReadyViewModel
import views.html.TaxReturnNotReadyView
import views.html.TaxReturnNotReadyAgentView


class TaxReturnNotReadyControllerSpec extends SpecBase {

  private val state = SectionState(NotStarted, CannotStartYet, CannotStartYet, CannotStartYet)

  private val vm = TaxReturnNotReadyViewModel(state, "taxReturnNotReady")

  private val agentVm = TaxReturnNotReadyViewModel(state, "taxReturnNotReady.agent")


  "TaxReturnNotReady Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.TaxReturnNotReadyController.onPageLoad(taxYear).url).withSession(validTaxYears)

        val result = route(application, request).value

        val view = application.injector.instanceOf[TaxReturnNotReadyView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(taxYear, vm)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET for an agent" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, routes.TaxReturnNotReadyController.onPageLoad(taxYear).url).withSession(validTaxYears)

        val result = route(application, request).value

        val view = application.injector.instanceOf[TaxReturnNotReadyAgentView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(taxYear, agentVm)(request, messages(application)).toString
      }
    }
  }
}
