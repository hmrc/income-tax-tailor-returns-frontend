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
import connectors.httpParsers.SessionDataHttpParser.SessionDataResponse
import connectors.{ConnectorResponse, SessionDataConnector, StateBenefitsConnector}
import forms.workandbenefits.JobseekersAllowanceFormProvider
import handlers.ErrorHandler
import models.errors.{APIErrorBodyModel, APIErrorModel, SimpleErrorWrapper}
import models.prePopulation.{EsaJsaPrePopulationResponse, StateBenefitsPrePopulationResponse}
import models.session.SessionData
import models.workandbenefits.JobseekersAllowance
import models.workandbenefits.JobseekersAllowance.{Esa, Jsa}
import models.{Done, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito.when
import org.mockito.stubbing.OngoingStubbing
import org.scalatestplus.mockito.MockitoSugar
import pages.workandbenefits.JobseekersAllowancePage
import play.api.data.Form
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded, Call, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Application, inject}
import play.twirl.api.Html
import services.{PrePopulationService, SessionDataService, UserDataService}
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

  val expectedConditionalIndividual = s"HMRC hold information that you received Jobseeker’s Allowance between 6 April ${taxYear - 1} and 5 April $taxYear. This will appear on your Income Tax Return, where you can remove this."
  val expectedConditionalAgent = s"HMRC hold information that your client received Jobseeker’s Allowance between 6 April ${taxYear - 1} and 5 April $taxYear. This will appear on their Income Tax Return, where you can remove this."

  trait Test {
    val nino: String = "AA111111A"
    val taxYear: Int = 2024
    val mtdItId: String = "someMtdItId"

    def isAgent: Boolean
    def application: Application
    def view: JobseekersAllowanceView = application.injector.instanceOf[JobseekersAllowanceView]
    def agentView: JobseekersAllowanceAgentView = application.injector.instanceOf[JobseekersAllowanceAgentView]

    val userAnswers: Option[UserAnswers] = Some(emptyUserAnswers)

    val dummySessionData: SessionData = SessionData(
      mtditid = mtdItId,
      nino = nino,
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
    val mockStateBenefitsConnector: StateBenefitsConnector = mock[StateBenefitsConnector]
    val mockErrorHandler: ErrorHandler = mock[ErrorHandler]

    val sessionDataService = new SessionDataService(
      sessionDataConnector = mockSessionDataConnector,
      config = mockAppConfig
    )

    val prePopulationService = new PrePopulationService(
      stateBenefitsConnector = mockStateBenefitsConnector
    )

    val errorView: String = "This is some dummy error page"

    when(mockErrorHandler.internalServerErrorTemplate(ArgumentMatchers.any[Request[_]]))
      .thenReturn(Html("This is some dummy error page"))

    def mockSessionCookieServiceEnabledConfig(isEnabled: Boolean): OngoingStubbing[Boolean] =
      when(mockAppConfig.sessionCookieServiceEnabled)
        .thenReturn(isEnabled)

    def mockSessionDataConnectorGet(result: Future[SessionDataResponse]): OngoingStubbing[Future[SessionDataResponse]] =
      when(mockSessionDataConnector.getSessionData(any[HeaderCarrier]))
        .thenReturn(result)

    def mockStateBenefitsConnectorGet(
                                       result: ConnectorResponse[StateBenefitsPrePopulationResponse]
                                     ): OngoingStubbing[ConnectorResponse[StateBenefitsPrePopulationResponse]] =
      when(
        mockStateBenefitsConnector.getPrePopulation(nino = any, taxYear = any, mtdItId = any)(any[HeaderCarrier])
      ).thenReturn(result)

    def application: Application = applicationBuilder(userAnswers, isAgent)
      .configure(isPrePopEnabled(true))
      .overrides(
        inject.bind[ErrorHandler].toInstance(mockErrorHandler)
      )
      .bindings(
        inject.bind[SessionDataService].toInstance(sessionDataService),
        inject.bind[PrePopulationService].toInstance(prePopulationService),
      )
      .build()
  }

  trait NonAgentUser {
    val isAgent: Boolean = false
  }

  trait AgentUser {
    val isAgent: Boolean = true
  }

  trait GetRequest {
    def application: Application
    def session: (String, String) = validTaxYears
    def request: Request[AnyContentAsEmpty.type] = FakeRequest(GET, jobseekersAllowanceRoute).withSession(session)
    def result: Future[Result] = route(application, request).value
  }

  trait SubmitRequest {
    def application: Application
    def session: (String, String) = validTaxYears

    def request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, jobseekersAllowanceRoute)
      .withFormUrlEncodedBody(("value[0]", JobseekersAllowance.values.head.toString))
      .withSession(session)

    def result: Future[Result] = route(application, request).value
  }

  "JobseekersAllowance Controller" - {
    "when trying to retrieve the view with a GET" -> {
      "when pre-population is disabled" -> {
        trait GetWithNoPrePopTest extends PrePopDisabledTest with GetRequest with NonAgentUser
        trait GetWithNoPrePopAgentTest extends PrePopDisabledTest with GetRequest with AgentUser

        "[GET] should return expected view when no user answers exist" in new GetWithNoPrePopTest {
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

        "[GET] should return expected view when no user answers exist for an agent" in new GetWithNoPrePopAgentTest {
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

        "[GET] should return expected view when user answers exist" in new GetWithNoPrePopTest {
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

        "[GET] should return expected view when user answers exist for an agent" in new GetWithNoPrePopAgentTest {
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
        trait GetWithPrePopTest extends PrePopEnabledTest with GetRequest with NonAgentUser
        trait GetWithPrePopAgentTest extends PrePopEnabledTest with GetRequest with AgentUser

        // Currently failing
        "handle errors during NINO retrieval" -> {
          "[GET] should show error page - SessionDataService: disabled, Nino in session: false" in new GetWithPrePopTest {
            mockSessionCookieServiceEnabledConfig(false)

            running(application) {
              status(result) mustEqual INTERNAL_SERVER_ERROR
              contentAsString(result) mustEqual errorView
            }
          }

          "[GET] should show error page - SessionDataService: returns error, Nino in session: false" in new GetWithPrePopTest {
            mockSessionCookieServiceEnabledConfig(true)

            mockSessionDataConnectorGet(Future.successful(Left(
              APIErrorModel(IM_A_TEAPOT, APIErrorBodyModel("", "")))
            ))

            running(application) {
              status(result) mustEqual INTERNAL_SERVER_ERROR
              contentAsString(result) mustEqual errorView
            }
          }

          "[GET] should show error page - SessionDataService: returns None, Nino in session: false" in new GetWithPrePopTest {
            mockSessionCookieServiceEnabledConfig(true)
            mockSessionDataConnectorGet(Future.successful(Right(None)))

            running(application) {
              status(result) mustEqual INTERNAL_SERVER_ERROR
              contentAsString(result) mustEqual errorView
            }
          }
        }

        // Currently failing
        "[GET] should return an error page when pre-pop retrieval fails" in new GetWithPrePopTest {
          mockSessionCookieServiceEnabledConfig(true)
          mockSessionDataConnectorGet(Future.successful(Right(Some(dummySessionData))))

          mockStateBenefitsConnectorGet(
            result = Future.successful(Left(SimpleErrorWrapper(IM_A_TEAPOT)))
          )

          running(application) {
            status(result) mustEqual INTERNAL_SERVER_ERROR
            contentAsString(result) mustEqual errorView
          }
        }

        "[GET] should return the expected view when user answers and pre-pop exists" in new GetWithPrePopTest {
          mockSessionCookieServiceEnabledConfig(true)
          mockSessionDataConnectorGet(Future.successful(Right(Some(dummySessionData))))

          mockStateBenefitsConnectorGet(
            result = Future.successful(Right(StateBenefitsPrePopulationResponse(
              hasEsaPrePop = true,
              hasJsaPrePop = false,
              hasPensionsPrePop = false,
              hasPensionLumpSumsPrePop = false
            )))
          )

          override val userAnswers: Option[UserAnswers] = Some(
            emptyUserAnswers.set(
              JobseekersAllowancePage,
              Set[JobseekersAllowance](Jsa)
            ).get
          )

          val filledForm: Form[Set[JobseekersAllowance]] = form.fill(Set[JobseekersAllowance](Jsa))

          running(application) {
            status(result) mustEqual OK

            contentAsString(result) mustEqual
              view(
                form = filledForm,
                mode = NormalMode,
                taxYear = taxYear,
                prePopData = EsaJsaPrePopulationResponse(hasJsaPrePop = false, hasEsaPrePop = true)
              )(request, messages(application)).toString
          }
        }

        "[GET] should return the expected view when user answers and pre-pop exists for an agent" in new GetWithPrePopAgentTest {
          mockSessionCookieServiceEnabledConfig(true)
          mockSessionDataConnectorGet(Future.successful(Right(Some(dummySessionData))))

          mockStateBenefitsConnectorGet(
            result = Future.successful(Right(StateBenefitsPrePopulationResponse(
              hasEsaPrePop = true,
              hasJsaPrePop = false,
              hasPensionsPrePop = false,
              hasPensionLumpSumsPrePop = false
            )))
          )

          override val userAnswers: Option[UserAnswers] = Some(
            emptyUserAnswers.set(
              JobseekersAllowancePage,
              Set[JobseekersAllowance](Jsa)
            ).get
          )

          val filledForm: Form[Set[JobseekersAllowance]] = form.fill(Set[JobseekersAllowance](Jsa))

          running(application) {
            status(result) mustEqual OK

            contentAsString(result) mustEqual
              agentView(
                form = filledForm,
                mode = NormalMode,
                taxYear = taxYear,
                prePopData = EsaJsaPrePopulationResponse(hasJsaPrePop = false, hasEsaPrePop = true)
              )(request, messages(application)).toString
          }
        }

        "[GET] should return the expected view when only pre-pop exists" in new GetWithPrePopTest {
          mockSessionCookieServiceEnabledConfig(true)
          mockSessionDataConnectorGet(Future.successful(Right(Some(dummySessionData))))

          mockStateBenefitsConnectorGet(
            result = Future.successful(Right(StateBenefitsPrePopulationResponse(
              hasEsaPrePop = true,
              hasJsaPrePop = false,
              hasPensionsPrePop = false,
              hasPensionLumpSumsPrePop = false
            )))
          )

          val filledForm: Form[Set[JobseekersAllowance]] = form.fill(Set[JobseekersAllowance](Esa))

          running(application) {
            status(result) mustEqual OK

            contentAsString(result) mustEqual
              view(
                form = filledForm,
                mode = NormalMode,
                taxYear = taxYear,
                prePopData = EsaJsaPrePopulationResponse(hasJsaPrePop = false, hasEsaPrePop = true)
              )(request, messages(application)).toString
          }
        }

        "[GET] should return the expected view when only pre-pop exists for an agent" in new GetWithPrePopAgentTest {
          mockSessionCookieServiceEnabledConfig(true)
          mockSessionDataConnectorGet(Future.successful(Right(Some(dummySessionData))))

          mockStateBenefitsConnectorGet(
            result = Future.successful(Right(StateBenefitsPrePopulationResponse(
              hasEsaPrePop = true,
              hasJsaPrePop = false,
              hasPensionsPrePop = false,
              hasPensionLumpSumsPrePop = false
            )))
          )

          val filledForm: Form[Set[JobseekersAllowance]] = form.fill(Set[JobseekersAllowance](Esa))

          running(application) {
            status(result) mustEqual OK

            contentAsString(result) mustEqual
              agentView(
                form = filledForm,
                mode = NormalMode,
                taxYear = taxYear,
                prePopData = EsaJsaPrePopulationResponse(hasJsaPrePop = false, hasEsaPrePop = true)
              )(request, messages(application)).toString
          }
        }

        "[GET] should return the expected view when user answers and pre-pop don't exist" in new GetWithPrePopTest {
          mockSessionCookieServiceEnabledConfig(true)
          mockSessionDataConnectorGet(Future.successful(Right(Some(dummySessionData))))

          mockStateBenefitsConnectorGet(
            result = Future.successful(Right(StateBenefitsPrePopulationResponse(
              hasEsaPrePop = false,
              hasJsaPrePop = false,
              hasPensionsPrePop = false,
              hasPensionLumpSumsPrePop = false
            )))
          )

          running(application) {
            status(result) mustEqual OK

            contentAsString(result) mustEqual
              view(
                form = form,
                mode = NormalMode,
                taxYear = taxYear,
                prePopData = EsaJsaPrePopulationResponse(hasJsaPrePop = false, hasEsaPrePop = false)
              )(request, messages(application)).toString
          }
        }

        "[GET] should return the expected view when user answers and pre-pop don't exist for an agent" in new GetWithPrePopAgentTest {
          mockSessionCookieServiceEnabledConfig(true)
          mockSessionDataConnectorGet(Future.successful(Right(Some(dummySessionData))))

          mockStateBenefitsConnectorGet(
            result = Future.successful(Right(StateBenefitsPrePopulationResponse(
              hasEsaPrePop = false,
              hasJsaPrePop = false,
              hasPensionsPrePop = false,
              hasPensionLumpSumsPrePop = false
            )))
          )

          running(application) {
            status(result) mustEqual OK

            contentAsString(result) mustEqual
              agentView(
                form = form,
                mode = NormalMode,
                taxYear = taxYear,
                prePopData = EsaJsaPrePopulationResponse(hasJsaPrePop = false, hasEsaPrePop = false)
              )(request, messages(application)).toString
          }
        }
      }
    }

    "when trying to submit answers with a POST" -> {
      trait SubmitWithNoPrePopTest extends PrePopDisabledTest with SubmitRequest with NonAgentUser
      trait SubmitWithNoPrePopAgentTest extends PrePopDisabledTest with SubmitRequest with AgentUser

      "[POST] for a valid request should redirect to the next page" in new SubmitWithNoPrePopTest {
        val mockUserDataService: UserDataService = mock[UserDataService]
        when(mockUserDataService.set(any(), any())(any())) thenReturn Future.successful(Done)

        override val application: Application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              inject.bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              inject.bind[UserDataService].toInstance(mockUserDataService)
            )
            .configure(isPrePopEnabled(false))
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

      "for a request with form errors" -> {
        "[POST] should return expected view" in new SubmitWithNoPrePopTest {
          override def request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, jobseekersAllowanceRoute)
              .withFormUrlEncodedBody(("value", "invalid value"))
              .withSession(validTaxYears)

          val boundForm: Form[Set[JobseekersAllowance]] = form.bind(Map("value" -> "invalid value"))

          running(application) {
            status(result) mustEqual BAD_REQUEST

            contentAsString(result) mustEqual
              view(
                form = boundForm,
                mode = NormalMode,
                taxYear = taxYear,
                prePopData = EsaJsaPrePopulationResponse.empty
              )(request, messages(application)).toString
          }
        }

        "[POST] should return expected view for an agent" in new SubmitWithNoPrePopAgentTest {
          override def request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, jobseekersAllowanceRoute)
              .withFormUrlEncodedBody(("value", "invalid value"))
              .withSession(validTaxYears)

          val boundForm: Form[Set[JobseekersAllowance]] = agentForm.bind(Map("value" -> "invalid value"))

          running(application) {
            status(result) mustEqual BAD_REQUEST

            contentAsString(result) mustEqual
              agentView(
                form = boundForm,
                mode = NormalMode,
                taxYear = taxYear,
                prePopData = EsaJsaPrePopulationResponse.empty
              )(request, messages(application)).toString
          }
        }
      }

      "[POST] must redirect to Journey Recovery if no existing data is found" in new SubmitWithNoPrePopTest {
        override val userAnswers: Option[UserAnswers] = None

        running(application) {
          val request =
            FakeRequest(POST, jobseekersAllowanceRoute)
              .withFormUrlEncodedBody(("value[0]", JobseekersAllowance.values.head.toString))
              .withSession(validTaxYears)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.routes.JourneyRecoveryController.onPageLoad(taxYear = taxYear).url
        }
      }
    }
  }
}
