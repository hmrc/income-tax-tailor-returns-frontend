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

package controllers.workandbenefits

import base.SpecBase
import controllers.routes
import forms.workandbenefits.AboutYourWorkRadioPageFormProvider
import models.{Done, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.aboutyou.FosterCarerPage
import pages.workandbenefits.AboutYourWorkRadioPage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.UserDataService
import views.html.workandbenefits.{AboutYourWorkRadioPageAgentView, AboutYourWorkRadioPageView}

import scala.concurrent.Future

class AboutYourWorkRadioPageControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/foo")

  private def prePopEnabled(isEnabled: Boolean): Map[String, String] =
    Map("feature-switch.isPrePopEnabled" -> isEnabled.toString)

  val formProvider = new AboutYourWorkRadioPageFormProvider()
  val form: Form[Boolean] = formProvider(isAgent = false)
  val agentForm: Form[Boolean] = formProvider(isAgent = true)

  val expectedConditionalIndividual = s"HMRC hold information that you were employed between 6 April ${taxYear-1} and 5 April $taxYear."
  val expectedConditionalAgent = s"HMRC hold information that your client was employed between 6 April ${taxYear-1} and 5 April $taxYear."

  val userAnswersWithFosterCarer: UserAnswers = UserAnswers(mtdItId, taxYear).set(FosterCarerPage, true).success.value

  lazy val aboutYourWorkRadioPageRoute: String = controllers.workandbenefits.routes.AboutYourWorkController.onPageLoad(NormalMode, taxYear).url

  "AboutYourWorkRadioPage Controller" - {
    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersWithFosterCarer))
        .configure(prePopEnabled(false))
        .build()

      running(application) {
        val request = FakeRequest(GET, aboutYourWorkRadioPageRoute).withSession(validTaxYears)
        val result = route(application, request).value
        val view = application.injector.instanceOf[AboutYourWorkRadioPageView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, taxYear, prePopData = false)(request, messages(application)).toString
        contentAsString(result) mustNot include(expectedConditionalIndividual)
      }
    }
    "must return OK and the correct view for a GET and isPrePopEnabled true" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersWithFosterCarer))
        .configure(prePopEnabled(true))
        .build()

      running(application) {
        val request = FakeRequest(GET, aboutYourWorkRadioPageRoute).withSession(validTaxYears)
        val result = route(application, request).value
        val view = application.injector.instanceOf[AboutYourWorkRadioPageView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, taxYear, prePopData = false)(request, messages(application)).toString
        contentAsString(result) mustNot include(expectedConditionalIndividual)
      }
    }

    "must return OK and the correct view for a GET for an agent" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersWithFosterCarer), isAgent = true)
        .configure(prePopEnabled(false))
        .build()

      running(application) {
        val request = FakeRequest(GET, aboutYourWorkRadioPageRoute).withSession(validTaxYears)
        val result = route(application, request).value
        val view = application.injector.instanceOf[AboutYourWorkRadioPageAgentView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(agentForm, NormalMode, taxYear, prePopData = false)(request, messages(application)).toString
        contentAsString(result) mustNot include(expectedConditionalAgent)
      }
    }
    "must return OK and the correct view for a GET for an agent and isPrePopEnabled true" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersWithFosterCarer), isAgent = true)
        .configure(prePopEnabled(true))
        .build()

      running(application) {
        val request = FakeRequest(GET, aboutYourWorkRadioPageRoute).withSession(validTaxYears)
        val result = route(application, request).value
        val view = application.injector.instanceOf[AboutYourWorkRadioPageAgentView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(agentForm, NormalMode, taxYear, prePopData = false)(request, messages(application)).toString
        contentAsString(result) mustNot include(expectedConditionalAgent)
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = UserAnswers(mtdItId, taxYear)
        .set(FosterCarerPage, true).flatMap(_.set(AboutYourWorkRadioPage, true))
        .success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .configure(prePopEnabled(false))
        .build()

      running(application) {
        val request = FakeRequest(GET, aboutYourWorkRadioPageRoute).withSession(validTaxYears)
        val view = application.injector.instanceOf[AboutYourWorkRadioPageView]
        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode, taxYear, prePopData = false)(request, messages(application)).toString
        contentAsString(result) mustNot include(expectedConditionalIndividual)
      }
    }
    "must populate the view correctly on a GET when the question has previously been answered and isPrePopEnabled true" in {

      val userAnswers = UserAnswers(mtdItId, taxYear)
        .set(FosterCarerPage, true).flatMap(_.set(AboutYourWorkRadioPage, true))
        .success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .configure(prePopEnabled(true))
        .build()

      running(application) {
        val request = FakeRequest(GET, aboutYourWorkRadioPageRoute).withSession(validTaxYears)
        val view = application.injector.instanceOf[AboutYourWorkRadioPageView]
        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode, taxYear, prePopData = true)(request, messages(application)).toString
        contentAsString(result) must include(expectedConditionalIndividual)
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered for an agent" in {
      val userAnswers = UserAnswers(mtdItId, taxYear)
        .set(FosterCarerPage, true).flatMap(_.set(AboutYourWorkRadioPage, true))
        .success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = true)
        .configure(prePopEnabled(false))
        .build()

      running(application) {
        val request = FakeRequest(GET, aboutYourWorkRadioPageRoute).withSession(validTaxYears)
        val view = application.injector.instanceOf[AboutYourWorkRadioPageAgentView]
        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(agentForm.fill(true), NormalMode, taxYear, prePopData = false)(request, messages(application)).toString
        contentAsString(result) mustNot include(expectedConditionalAgent)
      }
    }
    "must populate the view correctly on a GET when the question has previously been answered for an agent and isPrePopEnabled true" in {

      val userAnswers = UserAnswers(mtdItId, taxYear)
        .set(FosterCarerPage, true).flatMap(_.set(AboutYourWorkRadioPage, true))
        .success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = true)
        .configure(prePopEnabled(true))
        .build()

      running(application) {
        val request = FakeRequest(GET, aboutYourWorkRadioPageRoute).withSession(validTaxYears)
        val view = application.injector.instanceOf[AboutYourWorkRadioPageAgentView]
        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(agentForm.fill(true), NormalMode, taxYear, prePopData = true)(request, messages(application)).toString
        contentAsString(result) must include(expectedConditionalAgent)
      }
    }

    "must redirect to the next page when true is submitted" in {
      val mockUserDataService = mock[UserDataService]

      when(mockUserDataService.set(any(), any())(any())) thenReturn Future.successful(Done)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithFosterCarer))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[UserDataService].toInstance(mockUserDataService)
          )
          .configure(prePopEnabled(false))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, aboutYourWorkRadioPageRoute)
            .withFormUrlEncodedBody(("value", "true"))
            .withSession(validTaxYears)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to the next page when false is submitted" in {
      val mockUserDataService = mock[UserDataService]

      when(mockUserDataService.set(any(), any())(any())) thenReturn Future.successful(Done)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithFosterCarer))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[UserDataService].toInstance(mockUserDataService)
          )
          .configure(prePopEnabled(false))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, aboutYourWorkRadioPageRoute)
            .withFormUrlEncodedBody(("value", "false"))
            .withSession(validTaxYears)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersWithFosterCarer))
        .configure(prePopEnabled(false))
        .build()

      running(application) {
        val request =
          FakeRequest(POST, aboutYourWorkRadioPageRoute)
            .withFormUrlEncodedBody(("value", ""))
            .withSession(validTaxYears)

        val boundForm = form.bind(Map("value" -> ""))
        val view = application.injector.instanceOf[AboutYourWorkRadioPageView]
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, taxYear, prePopData = false)(request, messages(application)).toString
      }
    }

    "must return a Bad Request and errors when invalid data is submitted for an agent" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersWithFosterCarer), isAgent = true)
        .configure(prePopEnabled(false))
        .build()

      running(application) {
        val request =
          FakeRequest(POST, aboutYourWorkRadioPageRoute)
            .withFormUrlEncodedBody(("value", ""))
            .withSession(validTaxYears)

        val boundForm = agentForm.bind(Map("value" -> ""))
        val view = application.injector.instanceOf[AboutYourWorkRadioPageAgentView]
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, taxYear, prePopData = false)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None)
        .configure(prePopEnabled(false))
        .build()

      running(application) {
        val request = FakeRequest(GET, aboutYourWorkRadioPageRoute).withSession(validTaxYears)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad(taxYear = taxYear).url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None)
        .configure(prePopEnabled(false))
        .build()

      running(application) {
        val request =
          FakeRequest(POST, aboutYourWorkRadioPageRoute)
            .withFormUrlEncodedBody(("value", "true"))
            .withSession(validTaxYears)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad(taxYear = taxYear).url
      }
    }
  }
}
