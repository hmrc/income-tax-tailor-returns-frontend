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
import config.FrontendAppConfig
import connectors.httpParsers.SessionDataHttpParser.SessionDataResponse
import connectors.{ConnectorResponse, EmploymentConnector, IncomeTaxCisConnector, SessionDataConnector, StateBenefitsConnector}
import forms.FormProvider
import handlers.ErrorHandler
import models.UserAnswers
import models.prePopulation.StateBenefitsPrePopulationResponse
import models.session.SessionData
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.stubbing.OngoingStubbing
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.inject.guice.GuiceableModule
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Application, inject}
import play.twirl.api.Html
import services.{PrePopulationService, SessionDataService}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

trait ControllerWithPrePopSpecBase[View, AgentView, FormType] extends SpecBase with MockitoSugar {

  def formProvider: FormProvider[FormType]
  def viewProvider: Application => View
  def agentViewProvider: Application => AgentView
  def requestRoute: String

  def journeyRecoveryUrl(taxYear: Int): String =
    controllers
      .routes
      .JourneyRecoveryController
      .onPageLoad(taxYear = taxYear)
      .url

  def onwardRoute: Call = Call("GET", "/foo")

  sealed trait ControllerWithPrePopTest {
    val nino: String = "AA111111A"
    val taxYear: Int = 2024
    val mtdItId: String = "someMtdItId"

    val userAnswers: Option[UserAnswers] = Some(emptyUserAnswers)

    val dummySessionData: SessionData = SessionData(
      mtditid = mtdItId,
      nino = nino,
      utr = "12345",
      sessionId = "id"
    )

    def defaultSession: Seq[(String, String)] = Seq(validTaxYears)

    def isPrePopEnabled(isEnabled: Boolean): Map[String, String] =
      Map("feature-switch.isPrePopEnabled" -> isEnabled.toString)

    def isAgent: Boolean
    def prePopEnabled: Boolean
    def applicationBindings: Seq[GuiceableModule] = Nil
    def applicationOverrides: Seq[GuiceableModule] = Nil

    def application: Application = applicationBuilder(userAnswers, isAgent)
      .configure(isPrePopEnabled(prePopEnabled))
      .bindings(applicationBindings :_*)
      .overrides(applicationOverrides :_*)
      .build()

    def view: View = viewProvider(application)
    def agentView: AgentView = agentViewProvider(application)
    def form: Form[FormType] = formProvider(isAgent = false)
    def agentForm: Form[FormType] = formProvider(isAgent = true)
  }

  trait PrePopDisabledTest extends ControllerWithPrePopTest {
    override def prePopEnabled: Boolean = false
  }

  trait PrePopEnabledTest extends ControllerWithPrePopTest {
    override def prePopEnabled: Boolean = true

    val mockSessionDataConnector: SessionDataConnector = mock[SessionDataConnector]
    val mockAppConfig: FrontendAppConfig = mock[FrontendAppConfig]
    val mockStateBenefitsConnector: StateBenefitsConnector = mock[StateBenefitsConnector]
    val mockIncomeTaxCisConnector: IncomeTaxCisConnector = mock[IncomeTaxCisConnector]
    val mockEmploymentConnector: EmploymentConnector = mock[EmploymentConnector]
    val mockErrorHandler: ErrorHandler = mock[ErrorHandler]

    val mockSessionDataService = new SessionDataService(
      sessionDataConnector = mockSessionDataConnector,
      config = mockAppConfig
    )

    val mockPrePopulationService = new PrePopulationService(
      stateBenefitsConnector = mockStateBenefitsConnector,
      cisConnector = mockIncomeTaxCisConnector,
      employmentConnector = mockEmploymentConnector
    )

    val mockErrorView: String = "This is some dummy error page"

    when(mockErrorHandler.internalServerErrorTemplate(ArgumentMatchers.any[Request[_]]))
      .thenReturn(Html(mockErrorView))

    def sessionCookieServiceEnabled: Boolean = true

    when(mockAppConfig.sessionCookieServiceEnabled)
      .thenReturn(sessionCookieServiceEnabled)

    def mockSessionDataConnectorGet(result: Future[SessionDataResponse]): OngoingStubbing[Future[SessionDataResponse]] =
      when(mockSessionDataConnector.getSessionData(any[HeaderCarrier]))
        .thenReturn(result)

    def mockStateBenefitsConnectorGet(
                                       result: ConnectorResponse[StateBenefitsPrePopulationResponse]
                                     ): OngoingStubbing[ConnectorResponse[StateBenefitsPrePopulationResponse]] =
      when(
        mockStateBenefitsConnector.getPrePopulation(nino = any, taxYear = any, mtdItId = any)(any[HeaderCarrier])
      ).thenReturn(result)

    override def applicationOverrides: Seq[GuiceableModule] = Seq(
      inject.bind[ErrorHandler].toInstance(mockErrorHandler)
    )

    override def applicationBindings: Seq[GuiceableModule] = Seq(
      inject.bind[SessionDataService].toInstance(mockSessionDataService),
      inject.bind[PrePopulationService].toInstance(mockPrePopulationService)
    )
  }

  // This should really be type bound [O: Writeable] but Scala 2 does not allow type bound traits
  sealed trait BaseRequest[O] {_: ControllerWithPrePopTest =>
    def request: Request[O]
    def result: Future[Result]
  }

  trait GetRequest extends BaseRequest[AnyContentAsEmpty.type] {_: ControllerWithPrePopTest =>
    def filledForm(f: FormType): Form[FormType] = formProvider(isAgent).fill(f)

    override def request: Request[AnyContentAsEmpty.type] = FakeRequest(GET, requestRoute).withSession(defaultSession :_*)
    override def result: Future[Result] = route(application, request).value
  }

  trait SubmitRequest extends BaseRequest[AnyContentAsFormUrlEncoded] {_: ControllerWithPrePopTest =>
    def formUrlEncodedBody: (String, String)
    def boundForm: Form[FormType] = formProvider(isAgent).bind(Map(formUrlEncodedBody))

    override def request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, requestRoute)
      .withFormUrlEncodedBody(formUrlEncodedBody)
      .withSession(defaultSession :_*)

    override def result: Future[Result] = route(application, request).value
  }

  trait NonAgentUser { _: ControllerWithPrePopTest =>
    override val isAgent: Boolean = false
  }

  trait AgentUser { _: ControllerWithPrePopTest =>
    override val isAgent: Boolean = true
  }

  trait GetWithPrePopTest extends PrePopEnabledTest with GetRequest with NonAgentUser
  trait GetWithPrePopAgentTest extends PrePopEnabledTest with GetRequest with AgentUser
  trait GetWithNoPrePopTest extends PrePopDisabledTest with GetRequest with NonAgentUser
  trait GetWithNoPrePopAgentTest extends PrePopDisabledTest with GetRequest with AgentUser

  trait SubmitWithNoPrePopTest extends PrePopDisabledTest with SubmitRequest with NonAgentUser
  trait SubmitWithNoPrePopAgentTest extends PrePopDisabledTest with SubmitRequest with AgentUser
}
