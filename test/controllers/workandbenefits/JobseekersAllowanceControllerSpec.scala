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
import config.FrontendAppConfig
import connectors.SessionDataConnector
import connectors.httpParsers.SessionDataHttpParser.SessionDataResponse
import forms.workandbenefits.JobseekersAllowanceFormProvider
import handlers.ErrorHandler
import models.errors.{APIErrorBodyModel, APIErrorModel}
import models.prePopulation.EsaJsaPrePopulationResponse
import models.session.SessionData
import models.workandbenefits.JobseekersAllowance
import models.workandbenefits.JobseekersAllowance.{Esa, Jsa}
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.stubbing.OngoingStubbing
import org.scalatestplus.mockito.MockitoSugar
import pages.workandbenefits.JobseekersAllowancePage
import play.api.{Application, inject}
import play.api.data.Form
import play.api.mvc.{AnyContentAsEmpty, Call, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import services.SessionDataService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.workandbenefits.{JobseekersAllowanceAgentView, JobseekersAllowanceView}

import scala.concurrent.Future

class JobseekersAllowanceControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/foo")

  private def isPrePopEnabled(isEnabled: Boolean): Map[String, String] =
    Map("feature-switch.isPrePopEnabled" -> isEnabled.toString)

  lazy val jobseekersAllowanceRoute: String =
    controllers.workandbenefits.routes.JobseekersAllowanceController.onPageLoad(NormalMode, taxYear).url

  val formProvider: JobseekersAllowanceFormProvider = new JobseekersAllowanceFormProvider()
  val form: Form[Set[JobseekersAllowance]] = formProvider(isAgent = false)
  val agentForm: Form[Set[JobseekersAllowance]] = formProvider(isAgent = true)

  val expectedConditionalIndividual = s"HMRC hold information that you received Jobseeker’s Allowance between 6 April ${taxYear-1} and 5 April $taxYear. This will appear on your Income Tax Return, where you can remove this."
  val expectedConditionalAgent = s"HMRC hold information that your client received Jobseeker’s Allowance between 6 April ${taxYear-1} and 5 April $taxYear. This will appear on their Income Tax Return, where you can remove this."

  trait Test {
    def isAgent: Boolean = true
    def application: Application
    def request: Request[AnyContentAsEmpty.type]
    def result: Future[Result] = route(application, request).value
    def view: JobseekersAllowanceView = application.injector.instanceOf[JobseekersAllowanceView]
    def agentView: JobseekersAllowanceAgentView = application.injector.instanceOf[JobseekersAllowanceAgentView]

    def errorView(implicit request: Request[_]): Html =
      application
        .injector
        .instanceOf[ErrorHandler]
        .internalServerErrorTemplate

    val userAnswers: Option[UserAnswers] = Some(emptyUserAnswers)

    val dummySessionData: SessionData = SessionData(
      mtditid = "someMtdItId",
      nino = "AA111111A",
      utr = "12345",
      sessionId = "id"
    )
  }

  trait PrePopDisabledTest extends Test {
    def application: Application = applicationBuilder(userAnswers, isAgent)
      .configure(isPrePopEnabled(false))
      .build()
  }

  trait PrePopEnabledTest extends Test {
    val mockSessionDataConnector: SessionDataConnector = mock[SessionDataConnector]
    val mockAppConfig: FrontendAppConfig = mock[FrontendAppConfig]

    val sessionDataService = new SessionDataService(
      sessionDataConnector = mockSessionDataConnector,
      config = mockAppConfig
    )

    def mockSessionCookieServiceEnabledConfig(isEnabled: Boolean): OngoingStubbing[Boolean] =
      when(mockAppConfig.sessionCookieServiceEnabled)
        .thenReturn(isEnabled)

    def mockSessionDataConnectorGet(result: Future[SessionDataResponse]): OngoingStubbing[Future[SessionDataResponse]] =
      when(mockSessionDataConnector.getSessionData(any[HeaderCarrier]))
        .thenReturn(result)

    def application: Application = applicationBuilder(userAnswers, isAgent)
      .configure(isPrePopEnabled(true))
      .bindings(inject.bind[SessionDataService].toInstance(sessionDataService))
      .build()
  }

  trait GetRequest {
    def session: (String, String) = validTaxYears
    def request: Request[AnyContentAsEmpty.type] = FakeRequest(GET, jobseekersAllowanceRoute).withSession(session)
  }

  "JobseekersAllowance Controller" - {
    "when trying to retrieve the view with a GET" -> {
      "when pre-population is disabled" -> {
        trait GetWithNoPrePopTest extends PrePopDisabledTest with GetRequest

        "should return expected view when no user answers exist" in new GetWithNoPrePopTest {
          running(application) {
            status(result) mustEqual OK

            contentAsString(result) mustEqual
              view(
                form = form,
                mode = NormalMode,
                taxYear = taxYear,
                prePopData = EsaJsaPrePopulationResponse.empty
              )(request, messages(application)).toString
          }
        }

        "should return expected view when no user answers exist for an agent" in new GetWithNoPrePopTest {
          override def isAgent: Boolean = true

          running(application) {
            status(result) mustEqual OK

            contentAsString(result) mustEqual
              agentView(
                form = form,
                mode = NormalMode,
                taxYear = taxYear,
                prePopData = EsaJsaPrePopulationResponse.empty
              )(request, messages(application)).toString
          }
        }

        "should return expected view when user answers exist" in new GetWithNoPrePopTest {
          override val userAnswers: Option[UserAnswers] = Some(
            emptyUserAnswers.set(
              JobseekersAllowancePage,
              Set[JobseekersAllowance](Esa, Jsa)
            ).get
          )

          val filledForm: Form[Set[JobseekersAllowance]] = form.fill(Set[JobseekersAllowance](Esa, Jsa))

          running(application) {
            status(result) mustEqual OK

            contentAsString(result) mustEqual
              view(
                form = filledForm,
                mode = NormalMode,
                taxYear = taxYear,
                prePopData = EsaJsaPrePopulationResponse.empty
              )(request, messages(application)).toString
          }
        }

        "should return expected view when user answers exist for an agent" in new GetWithNoPrePopTest {
          override val userAnswers: Option[UserAnswers] = Some(
            emptyUserAnswers.set(
              JobseekersAllowancePage,
              Set[JobseekersAllowance](Esa, Jsa)
            ).get
          )

          val filledForm: Form[Set[JobseekersAllowance]] = form.fill(Set[JobseekersAllowance](Esa, Jsa))

          override def isAgent: Boolean = true

          running(application) {
            status(result) mustEqual OK

            contentAsString(result) mustEqual
              agentView(
                form = filledForm,
                mode = NormalMode,
                taxYear = taxYear,
                prePopData = EsaJsaPrePopulationResponse.empty
              )(request, messages(application)).toString
          }
        }
      }

      "when pre-population is enabled" -> {
        trait GetWithPrePopTest extends PrePopEnabledTest with GetRequest

        "handle errors during NINO retrieval" -> {
          "should show error page - SessionDataService: disabled, Nino in session: false" in new GetWithPrePopTest {
            mockSessionCookieServiceEnabledConfig(false)

            running(application) {
              status(result) mustEqual INTERNAL_SERVER_ERROR
              contentAsString(result) mustEqual errorView(request)
            }
          }

          "should show error page - SessionDataService: returns error, Nino in session: false" in new GetWithPrePopTest {
            mockSessionCookieServiceEnabledConfig(true)

            mockSessionDataConnectorGet(Future.successful(Left(
              APIErrorModel(IM_A_TEAPOT, APIErrorBodyModel("", "")))
            ))

            running(application) {
              status(result) mustEqual INTERNAL_SERVER_ERROR
              contentAsString(result) mustEqual errorView(request)
            }
          }

          "should show error page - SessionDataService: returns None, Nino in session: false" in new GetWithPrePopTest {
            mockSessionCookieServiceEnabledConfig(true)
            mockSessionDataConnectorGet(Future.successful(Right(None)))

            running(application) {
              status(result) mustEqual INTERNAL_SERVER_ERROR
              contentAsString(result) mustEqual errorView(request)
            }
          }
        }

        "should return an error page when pre-pop retrieval fails" in new GetWithPrePopTest {
          mockSessionCookieServiceEnabledConfig(true)
          mockSessionDataConnectorGet(Future.successful(Right(Some(dummySessionData))))
          // TODO add mock for state benefits connector
        }

        "should return the expected view when user answers and pre-pop exists" in new GetWithPrePopTest {

        }

        "should return the expected view when user answers and pre-pop exists for an agent" in new GetWithPrePopTest {

        }

        "should return the expected view when only pre-pop exists" in new GetWithPrePopTest {

        }

        "should return the expected view when only pre-pop exists for an agent" in new GetWithPrePopTest {

        }

        "should return the expected view when user answers and pre-pop don't exist" in new GetWithPrePopTest {

        }

        "should return the expected view when user answers and pre-pop don't exist for an agent" in new GetWithPrePopTest {

        }

      }
    }

    "when trying to submit answers with a POST" -> {
      "when pre-population is disabled" -> {

      }

      "when pre-population is enabled" -> {

      }
    }

/*    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .configure(isPrePopEnabled(false))
        .build()

      running(application) {
        val request = FakeRequest(GET, jobseekersAllowanceRoute).withSession(validTaxYears)
        val result = route(application, request).value
        val view = application.injector.instanceOf[JobseekersAllowanceView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            form = form,
            mode = NormalMode,
            taxYear = taxYear,
            prePopData = EsaJsaPrePopulationResponse.empty
          )(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET and isPrePopEnabled" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .configure(isPrePopEnabled())
        .build()

      running(application) {
        val request = FakeRequest(GET, jobseekersAllowanceRoute).withSession(validTaxYears)

        val result = route(application, request).value

        val view = application.injector.instanceOf[JobseekersAllowanceView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(form, NormalMode, taxYear, prePopData = ???)(request, messages(application)).toString
        contentAsString(result) mustNot include(expectedConditionalIndividual)
      }
    }

    "must return OK and the correct view for a GET as an agent" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true)
        .configure(isPrePopEnabled(false))
        .build()

      running(application) {
        val request = FakeRequest(GET, jobseekersAllowanceRoute).withSession(validTaxYears)

        val result = route(application, request).value

        val view = application.injector.instanceOf[JobseekersAllowanceAgentView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(
          form = agentForm,
          mode = NormalMode,
          taxYear = taxYear,
          prePopData = EsaJsaPrePopulationResponse.empty
        )(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET as an agent and isPrePopEnabled" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true)
        .configure(isPrePopEnabled())
        .build()

      running(application) {
        val request = FakeRequest(GET, jobseekersAllowanceRoute).withSession(validTaxYears)

        val result = route(application, request).value

        val view = application.injector.instanceOf[JobseekersAllowanceAgentView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(agentForm, NormalMode, taxYear, prePopData = ???)(request, messages(application)).toString
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
        contentAsString(result) mustEqual view(form.fill(JobseekersAllowance.values.toSet), NormalMode, taxYear, prePopData = ???)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered and isPrePopEnabled" in {

      val userAnswers = UserAnswers(mtdItId, taxYear).set(JobseekersAllowancePage, JobseekersAllowance.values.toSet).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .configure(isPrePopEnabled())
        .build()

      running(application) {
        val request = FakeRequest(GET, jobseekersAllowanceRoute).withSession(validTaxYears)

        val view = application.injector.instanceOf[JobseekersAllowanceView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(JobseekersAllowance.values.toSet), NormalMode, taxYear, prePopData = ???)(request, messages(application)).toString
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
        contentAsString(result) mustEqual view(agentForm.fill(JobseekersAllowance.values.toSet), NormalMode, taxYear, prePopData = ???)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered for an agent and isPrePopEnabled" in {

      val userAnswers = UserAnswers(mtdItId, taxYear).set(JobseekersAllowancePage, JobseekersAllowance.values.toSet).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = true)
        .configure(isPrePopEnabled())
        .build()

      running(application) {
        val request = FakeRequest(GET, jobseekersAllowanceRoute).withSession(validTaxYears)

        val view = application.injector.instanceOf[JobseekersAllowanceAgentView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(agentForm.fill(JobseekersAllowance.values.toSet), NormalMode, taxYear, prePopData = ???)(request, messages(application)).toString
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
        contentAsString(result) mustEqual view(boundForm, NormalMode, taxYear, prePopData = ???)(request, messages(application)).toString
      }
    }

    "must return a Bad Request and errors when invalid data is submitted for an agent" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true)
        .configure(isPrePopEnabled(false))
        .build()

      running(application) {
        val request =
          FakeRequest(POST, jobseekersAllowanceRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))
            .withSession(validTaxYears)

        val boundForm = agentForm.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[JobseekersAllowanceAgentView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, taxYear, prePopData = ???)(request, messages(application)).toString
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
    }*/
  }
}
