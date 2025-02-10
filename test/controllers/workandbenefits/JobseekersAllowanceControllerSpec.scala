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
import forms.workandbenefits.JobseekersAllowanceFormProvider
import models.workandbenefits.JobseekersAllowance
import models.{Done, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.workandbenefits.JobseekersAllowancePage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.UserDataService
import views.html.workandbenefits.{JobseekersAllowanceAgentView, JobseekersAllowanceView}

import scala.concurrent.Future

class JobseekersAllowanceControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  private val prePopEnabled = Map("feature-switch.isPrePopEnabled" -> "true")

  lazy val jobseekersAllowanceRoute = controllers.workandbenefits.routes.JobseekersAllowanceController.onPageLoad(NormalMode, taxYear).url

  val formProvider = new JobseekersAllowanceFormProvider()
  val form = formProvider(isAgent = false)
  val agentForm = formProvider(isAgent = true)

  val expectedConditionalIndividual = s"HMRC hold information that you received Jobseeker’s Allowance between 6 April ${taxYear-1} and 5 April $taxYear. This will appear on your Income Tax Return, where you can remove this."
  val expectedConditionalAgent = s"HMRC hold information that your client received Jobseeker’s Allowance between 6 April ${taxYear-1} and 5 April $taxYear. This will appear on their Income Tax Return, where you can remove this."

  "JobseekersAllowance Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, jobseekersAllowanceRoute).withSession(validTaxYears)

        val result = route(application, request).value

        val view = application.injector.instanceOf[JobseekersAllowanceView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(form, NormalMode, taxYear, prePopData = false)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET and isPrePopEnabled" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .configure(prePopEnabled)
        .build()

      running(application) {
        val request = FakeRequest(GET, jobseekersAllowanceRoute).withSession(validTaxYears)

        val result = route(application, request).value

        val view = application.injector.instanceOf[JobseekersAllowanceView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(form, NormalMode, taxYear, prePopData = false)(request, messages(application)).toString
        contentAsString(result) mustNot include(expectedConditionalIndividual)
      }
    }

    "must return OK and the correct view for a GET as an agent" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, jobseekersAllowanceRoute).withSession(validTaxYears)

        val result = route(application, request).value

        val view = application.injector.instanceOf[JobseekersAllowanceAgentView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(agentForm, NormalMode, taxYear, prePopData = false)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET as an agent and isPrePopEnabled" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true)
        .configure(prePopEnabled)
        .build()

      running(application) {
        val request = FakeRequest(GET, jobseekersAllowanceRoute).withSession(validTaxYears)

        val result = route(application, request).value

        val view = application.injector.instanceOf[JobseekersAllowanceAgentView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(agentForm, NormalMode, taxYear, prePopData = false)(request, messages(application)).toString
        contentAsString(result) mustNot include(expectedConditionalAgent)
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(mtdItId, taxYear).set(JobseekersAllowancePage, JobseekersAllowance.values.toSet).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, jobseekersAllowanceRoute).withSession(validTaxYears)

        val view = application.injector.instanceOf[JobseekersAllowanceView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(JobseekersAllowance.values.toSet), NormalMode, taxYear, prePopData = false)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered and isPrePopEnabled" in {

      val userAnswers = UserAnswers(mtdItId, taxYear).set(JobseekersAllowancePage, JobseekersAllowance.values.toSet).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .configure(prePopEnabled)
        .build()

      running(application) {
        val request = FakeRequest(GET, jobseekersAllowanceRoute).withSession(validTaxYears)

        val view = application.injector.instanceOf[JobseekersAllowanceView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(JobseekersAllowance.values.toSet), NormalMode, taxYear, prePopData = true)(request, messages(application)).toString
        contentAsString(result) must include(expectedConditionalIndividual)
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered for an agent" in {

      val userAnswers = UserAnswers(mtdItId, taxYear).set(JobseekersAllowancePage, JobseekersAllowance.values.toSet).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, jobseekersAllowanceRoute).withSession(validTaxYears)

        val view = application.injector.instanceOf[JobseekersAllowanceAgentView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(agentForm.fill(JobseekersAllowance.values.toSet), NormalMode, taxYear, prePopData = false)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered for an agent and isPrePopEnabled" in {

      val userAnswers = UserAnswers(mtdItId, taxYear).set(JobseekersAllowancePage, JobseekersAllowance.values.toSet).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = true)
        .configure(prePopEnabled)
        .build()

      running(application) {
        val request = FakeRequest(GET, jobseekersAllowanceRoute).withSession(validTaxYears)

        val view = application.injector.instanceOf[JobseekersAllowanceAgentView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(agentForm.fill(JobseekersAllowance.values.toSet), NormalMode, taxYear, prePopData = true)(request, messages(application)).toString
        contentAsString(result) must include(expectedConditionalAgent)
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockUserDataService = mock[UserDataService]

      when(mockUserDataService.set(any(), any())(any())) thenReturn Future.successful(Done)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[UserDataService].toInstance(mockUserDataService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, jobseekersAllowanceRoute)
            .withFormUrlEncodedBody(("value[0]", JobseekersAllowance.values.head.toString))
            .withSession(validTaxYears)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, jobseekersAllowanceRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))
            .withSession(validTaxYears)

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[JobseekersAllowanceView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, taxYear, prePopData = false)(request, messages(application)).toString
      }
    }

    "must return a Bad Request and errors when invalid data is submitted for an agent" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()

      running(application) {
        val request =
          FakeRequest(POST, jobseekersAllowanceRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))
            .withSession(validTaxYears)

        val boundForm = agentForm.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[JobseekersAllowanceAgentView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, taxYear, prePopData = false)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, jobseekersAllowanceRoute).withSession(validTaxYears)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad(taxYear = taxYear).url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, jobseekersAllowanceRoute)
            .withFormUrlEncodedBody(("value[0]", JobseekersAllowance.values.head.toString))
            .withSession(validTaxYears)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad(taxYear = taxYear).url
      }
    }
  }
}
