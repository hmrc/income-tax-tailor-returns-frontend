package controllers

import base.SpecBase
import forms.UkDividendsSharesAndLoansFromLimitedCompaniesFormProvider
import models.{NormalMode, UkDividendsSharesAndLoansFromLimitedCompanies, UserAnswers, Done}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.UkDividendsSharesAndLoansFromLimitedCompaniesPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.UserDataService
import views.html.UkDividendsSharesAndLoansFromLimitedCompaniesView
import views.html.UkDividendsSharesAndLoansFromLimitedCompaniesAgentView

import scala.concurrent.Future

class UkDividendsSharesAndLoansFromLimitedCompaniesControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val ukDividendsSharesAndLoansFromLimitedCompaniesRoute = routes.UkDividendsSharesAndLoansFromLimitedCompaniesController.onPageLoad(NormalMode, taxYear).url

  val formProvider = new UkDividendsSharesAndLoansFromLimitedCompaniesFormProvider()
  val form = formProvider(isAgent = false)
  val agentForm = formProvider(isAgent = true)

  "UkDividendsSharesAndLoansFromLimitedCompanies Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, ukDividendsSharesAndLoansFromLimitedCompaniesRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UkDividendsSharesAndLoansFromLimitedCompaniesView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(form, NormalMode, taxYear)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET as an agent" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, ukDividendsSharesAndLoansFromLimitedCompaniesRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UkDividendsSharesAndLoansFromLimitedCompaniesAgentView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(agentForm, NormalMode, taxYear)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(mtdItId, taxYear).set(UkDividendsSharesAndLoansFromLimitedCompaniesPage, UkDividendsSharesAndLoansFromLimitedCompanies.values.toSet).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, ukDividendsSharesAndLoansFromLimitedCompaniesRoute)

        val view = application.injector.instanceOf[UkDividendsSharesAndLoansFromLimitedCompaniesView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(UkDividendsSharesAndLoansFromLimitedCompanies.values.toSet), NormalMode, taxYear)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered for an agent" in {

      val userAnswers = UserAnswers(mtdItId, taxYear).set(UkDividendsSharesAndLoansFromLimitedCompaniesPage, UkDividendsSharesAndLoansFromLimitedCompanies.values.toSet).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, ukDividendsSharesAndLoansFromLimitedCompaniesRoute)

        val view = application.injector.instanceOf[UkDividendsSharesAndLoansFromLimitedCompaniesAgentView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(agentForm.fill(UkDividendsSharesAndLoansFromLimitedCompanies.values.toSet), NormalMode, taxYear)(request, messages(application)).toString
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
          FakeRequest(POST, ukDividendsSharesAndLoansFromLimitedCompaniesRoute)
            .withFormUrlEncodedBody(("value[0]", UkDividendsSharesAndLoansFromLimitedCompanies.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, ukDividendsSharesAndLoansFromLimitedCompaniesRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[UkDividendsSharesAndLoansFromLimitedCompaniesView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, taxYear)(request, messages(application)).toString
      }
    }

    "must return a Bad Request and errors when invalid data is submitted for an agent" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()

      running(application) {
        val request =
          FakeRequest(POST, ukDividendsSharesAndLoansFromLimitedCompaniesRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = agentForm.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[UkDividendsSharesAndLoansFromLimitedCompaniesAgentView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, taxYear)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, ukDividendsSharesAndLoansFromLimitedCompaniesRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad(taxYear = taxYear).url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, ukDividendsSharesAndLoansFromLimitedCompaniesRoute)
            .withFormUrlEncodedBody(("value[0]", UkDividendsSharesAndLoansFromLimitedCompanies.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad(taxYear = taxYear).url
      }
    }
  }
}
