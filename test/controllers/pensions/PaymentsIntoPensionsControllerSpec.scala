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

package controllers.pensions

import base.SpecBase
import forms.pensions.PaymentsIntoPensionsFormProvider
import models.pensions.PaymentsIntoPensions
import models.{Done, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.pensions.PaymentsIntoPensionsPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.UserDataService
import views.html.pensions.{PaymentsIntoPensionsAgentView, PaymentsIntoPensionsView}

import scala.concurrent.Future

class PaymentsIntoPensionsControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val paymentsIntoPensionsRoute = routes.PaymentsIntoPensionsController.onPageLoad(NormalMode, taxYear).url

  val formProvider = new PaymentsIntoPensionsFormProvider()
  val form = formProvider(isAgent = false)
  val agentForm = formProvider(isAgent = true)

  "PaymentsIntoPensions Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, paymentsIntoPensionsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PaymentsIntoPensionsView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(form, NormalMode, taxYear)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET as an agent" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, paymentsIntoPensionsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PaymentsIntoPensionsAgentView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(agentForm, NormalMode, taxYear)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(mtdItId, taxYear).set(PaymentsIntoPensionsPage, PaymentsIntoPensions.values.toSet).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, paymentsIntoPensionsRoute)

        val view = application.injector.instanceOf[PaymentsIntoPensionsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(PaymentsIntoPensions.values.toSet), NormalMode, taxYear)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered for an agent" in {

      val userAnswers = UserAnswers(mtdItId, taxYear).set(PaymentsIntoPensionsPage, PaymentsIntoPensions.values.toSet).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, paymentsIntoPensionsRoute)

        val view = application.injector.instanceOf[PaymentsIntoPensionsAgentView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(agentForm.fill(PaymentsIntoPensions.values.toSet), NormalMode, taxYear)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockUserDataService = mock[UserDataService]

      when(mockUserDataService.set(any())(any())) thenReturn Future.successful(Done)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[UserDataService].toInstance(mockUserDataService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, paymentsIntoPensionsRoute)
            .withFormUrlEncodedBody(("value[0]", PaymentsIntoPensions.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, paymentsIntoPensionsRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[PaymentsIntoPensionsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, taxYear)(request, messages(application)).toString
      }
    }

    "must return a Bad Request and errors when invalid data is submitted for an agent" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()

      running(application) {
        val request =
          FakeRequest(POST, paymentsIntoPensionsRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = agentForm.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[PaymentsIntoPensionsAgentView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, taxYear)(request, messages(application)).toString
      }
    }
  }
}
