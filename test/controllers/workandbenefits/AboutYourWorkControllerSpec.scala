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

import controllers.{ControllerWithPrePopSpecBase, routes}
import forms.workandbenefits.{AboutYourWorkFormProvider, AboutYourWorkRadioPageFormProvider}
import models.workandbenefits.AboutYourWork
import models.{Done, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.aboutyou.FosterCarerPage
import pages.workandbenefits.{AboutYourWorkPage, AboutYourWorkRadioPage}
import play.api.Application
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.UserDataService
import views.html.workandbenefits.{AboutYourWorkAgentView, AboutYourWorkRadioPageAgentView, AboutYourWorkRadioPageView, AboutYourWorkView}

import scala.concurrent.Future

class AboutYourWorkControllerSpec extends
  ControllerWithPrePopSpecBase[AboutYourWorkView, AboutYourWorkAgentView, Set[AboutYourWork]] {

  override def formProvider: AboutYourWorkFormProvider = new AboutYourWorkFormProvider()

  override val viewProvider: Application => AboutYourWorkView =
    (application: Application) => application.injector.instanceOf[AboutYourWorkView]

  override val agentViewProvider: Application => AboutYourWorkAgentView =
    (application: Application) => application.injector.instanceOf[AboutYourWorkAgentView]

  override val requestRoute: String =
    controllers
      .workandbenefits
      .routes
      .AboutYourWorkBaseController
      .onPageLoad(NormalMode, taxYear).url


//  def onwardRoute: Call = Call("GET", "/foo")

//  private def prePopEnabled(isEnabled: Boolean): Map[String, String] =
//    Map("feature-switch.isPrePopEnabled" -> isEnabled.toString)

  lazy val aboutYourWorkRoute: String = controllers.workandbenefits.routes.AboutYourWorkBaseController.onPageLoad(NormalMode, taxYear).url

//  val formProvider = new AboutYourWorkFormProvider()
  val form: Form[Set[AboutYourWork]] = formProvider(isAgent = false)
  val agentForm: Form[Set[AboutYourWork]] = formProvider(isAgent = true)

  val radioFormProvider = new AboutYourWorkRadioPageFormProvider()
  val radioForm: Form[Boolean] = radioFormProvider(isAgent = false)
  val radioAgentForm: Form[Boolean] = radioFormProvider(isAgent = true)

  val expectedConditionalIndividual = s"HMRC hold information that you were employed between 6 April ${taxYear-1} and 5 April $taxYear."
  val expectedConditionalAgent = s"HMRC hold information that your client was employed between 6 April ${taxYear-1} and 5 April $taxYear."

  val userAnswersWithFosterCarer: UserAnswers = UserAnswers(mtdItId, taxYear).set(FosterCarerPage, true).success.value

  "AboutYourWork Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .build()

      running(application) {

        val request = FakeRequest(GET, aboutYourWorkRoute).withSession(validTaxYears)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AboutYourWorkView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(form, NormalMode, taxYear)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET as an agent" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true)
        .build()

      running(application) {
        val request = FakeRequest(GET, aboutYourWorkRoute).withSession(validTaxYears)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AboutYourWorkAgentView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(agentForm, NormalMode, taxYear)(request, messages(application)).toString
      }
    }

    "must return OK when no override is provided on GET" in {
      val application = applicationBuilder(Some(emptyUserAnswers)).build()

      running(application) {
        val controller = application.injector.instanceOf[AboutYourWorkController]

        val request = FakeRequest(GET, aboutYourWorkRoute).withSession(validTaxYears)
        val result = controller.onPageLoad(NormalMode, taxYear, None).apply(request)

        status(result) mustEqual OK
      }
    }

    "must return OK when no override is provided on POST" in {
      val mockUserDataService = mock[UserDataService]
      when(mockUserDataService.set(any(), any())(any())) thenReturn Future.successful(Done)

      val application = applicationBuilder(Some(emptyUserAnswers))
        .overrides(
          bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
          bind[UserDataService].toInstance(mockUserDataService)
        ).build()

      running(application) {
        val controller = application.injector.instanceOf[AboutYourWorkController]

        val request = FakeRequest(POST, aboutYourWorkRoute)
          .withFormUrlEncodedBody("value[0]" -> AboutYourWork.values.head.toString)
          .withSession(validTaxYears)

        val result = controller.onSubmit(NormalMode, taxYear, None).apply(request)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(mtdItId, taxYear).set(AboutYourWorkPage, AboutYourWork.values.toSet).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .build()

      running(application) {
        val request = FakeRequest(GET, aboutYourWorkRoute).withSession(validTaxYears)

        val view = application.injector.instanceOf[AboutYourWorkView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(AboutYourWork.values.toSet), NormalMode, taxYear)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered for an agent" in {

      val userAnswers = UserAnswers(mtdItId, taxYear).set(AboutYourWorkPage, AboutYourWork.values.toSet).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = true)
        .build()

      running(application) {
        val request = FakeRequest(GET, aboutYourWorkRoute).withSession(validTaxYears)

        val view = application.injector.instanceOf[AboutYourWorkAgentView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(agentForm.fill(AboutYourWork.values.toSet), NormalMode, taxYear)(request, messages(application)).toString
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
          FakeRequest(POST, aboutYourWorkRoute)
            .withFormUrlEncodedBody(("value[0]", AboutYourWork.values.head.toString))
            .withSession(validTaxYears)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .build()

      running(application) {
        val request =
          FakeRequest(POST, aboutYourWorkRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))
            .withSession(validTaxYears)

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[AboutYourWorkView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, taxYear)(request, messages(application)).toString
      }
    }

    "must return a Bad Request and errors when invalid data is submitted for an agent" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true)
        .build()

      running(application) {
        val request =
          FakeRequest(POST, aboutYourWorkRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))
            .withSession(validTaxYears)

        val boundForm = agentForm.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[AboutYourWorkAgentView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, taxYear)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None)
        .build()

      running(application) {
        val request = FakeRequest(GET, aboutYourWorkRoute).withSession(validTaxYears)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad(taxYear = taxYear).url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None)
        .build()

      running(application) {
        val request =
          FakeRequest(POST, aboutYourWorkRoute)
            .withFormUrlEncodedBody(("value[0]", AboutYourWork.values.head.toString))
            .withSession(validTaxYears)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad(taxYear = taxYear).url
      }
    }
  }

  "AboutYourWork Controller when Foster Carer is true" - {

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, aboutYourWorkRoute).withSession(validTaxYears)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad(taxYear = taxYear).url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, aboutYourWorkRoute)
            .withFormUrlEncodedBody(("value", "true"))
            .withSession(validTaxYears)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad(taxYear = taxYear).url
      }
    }
  }
}
