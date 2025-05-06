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

package connectors

import base.SpecBase
import connectors.httpParsers.SessionDataHttpParser.{SessionDataResponse, SessionDataResponseReads}
import mocks.{MockAppConfig, MockHttpClientV2}
import models.errors.{APIErrorBodyModel, APIErrorModel}
import models.session.SessionData
import play.api.http.Status.{IM_A_TEAPOT, INTERNAL_SERVER_ERROR}
import play.api.test.DefaultAwaitTimeout
import play.api.test.Helpers.await
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, StringContextOps}

class SessionDataConnectorSpec extends SpecBase
  with MockAppConfig
  with MockHttpClientV2
  with DefaultAwaitTimeout {

  trait Test {
    implicit val hc: HeaderCarrier = HeaderCarrier()

    val testConnector = new SessionDataConnectorImpl(
      config = mockAppConfig,
      httpClient = mockHttpClientV2
    )

    val baseUrl = "http://test-BaseUrl"
    mockSessionServiceBaseUrl(response = baseUrl)
    mockHttpClientV2Get(url"$baseUrl/income-tax-session-data/")

    val dummyResponse: Option[SessionData] = Some(SessionData(
      mtditid = mtdItId, nino = nino, sessionId = sessionId
    ))

    val dummyError: APIErrorModel = APIErrorModel(IM_A_TEAPOT, APIErrorBodyModel("", ""))

    implicit val reads: HttpReads[SessionDataResponse] = SessionDataResponseReads
  }

  "getSessionData" - {
    "should return a success when a success response is received from session data service" in new Test {
      mockHttpClientV2Execute[SessionDataResponse](Right(dummyResponse))

      val result: SessionDataResponse = await(testConnector.getSessionData)

      result mustBe a[Right[_, _]]
      result.getOrElse(None) mustBe dummyResponse
    }

    "should return an error when a success response is received from session data service" in new Test {
      mockHttpClientV2Execute[SessionDataResponse](
        Left(APIErrorModel(INTERNAL_SERVER_ERROR, APIErrorBodyModel("error", "reason")))
      )

      val result: SessionDataResponse = await(testConnector.getSessionData)

      result mustBe a[Left[_, _]]
      result.swap.getOrElse(dummyError).status mustBe INTERNAL_SERVER_ERROR
    }

    "should throw an exception when an exception occurs during call to session data service" in new Test {
      mockHttpClientV2ExecuteException[SessionDataResponse](
        new RuntimeException()
      )

      def result: SessionDataResponse = await(testConnector.getSessionData)

      assertThrows[RuntimeException](result)
    }
  }
}
