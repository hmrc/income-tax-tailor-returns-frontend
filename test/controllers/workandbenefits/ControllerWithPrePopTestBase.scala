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

package controllers.workandbenefits
import base.SpecBase
import config.FrontendAppConfig
import connectors.SessionDataConnector
import connectors.httpParsers.SessionDataHttpParser.SessionDataResponse
import handlers.ErrorHandler
import models.UserAnswers
import models.session.SessionData
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.stubbing.OngoingStubbing
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.{AnyContentAsEmpty, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Application, inject}
import play.twirl.api.Html
import services.SessionDataService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future
import scala.reflect.ClassTag

trait ControllerWithPrePopTestBase[I,A] extends SpecBase with MockitoSugar{
  def url:String

  private def isPrePopEnabled(isEnabled: Boolean): Map[String, String] =
    Map("feature-switch.isPrePopEnabled" -> isEnabled.toString)


  trait Test {
    def isAgent: Boolean = true
    def application: Application
    def request: Request[AnyContentAsEmpty.type]
    def result: Future[Result] = route(application, request).value
    def view(implicit ct: ClassTag[I]): I = application.injector.instanceOf[I]
    def agentView(implicit ct: ClassTag[A]): A = application.injector.instanceOf[A]

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
    def request: Request[AnyContentAsEmpty.type] = FakeRequest(GET, url).withSession(session)
  }
}
