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

package services

import base.SpecBase
import mocks.{MockAppConfig, MockSessionDataConnector}
import models.errors.{APIErrorBodyModel, APIErrorModel, MissingAgentClientDetails}
import models.session.SessionData
import play.api.http.{HeaderNames, Status}
import play.api.mvc.{AnyContentAsEmpty, Result, Results}
import play.api.test.Helpers.await
import play.api.test.{DefaultAwaitTimeout, FakeRequest, ResultExtractors}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class SessionDataServiceSpec extends SpecBase
  with ResultExtractors
  with HeaderNames
  with Status
  with Results
  with MockSessionDataConnector
  with MockAppConfig
  with DefaultAwaitTimeout {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val testService: SessionDataService = new SessionDataService(
    sessionDataConnector = mockSessionDataConnector,
    config = mockAppConfig
  )

  val dummyResponse: SessionData = SessionData(
    mtditid = "111111", nino = "AA111111A", utr = Some("123456"), sessionId = sessionId
  )

  val dummyError: APIErrorModel = APIErrorModel(IM_A_TEAPOT, APIErrorBodyModel("", ""))

  "getSessionDataBlock" -> {
    "when call to retrieve session data fails" -> {

      "should return an error when fallback returns no data" in {
        mockSessionServiceEnabled(true)
        mockGetSessionDataFromSessionStore(Left(dummyError))

        implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

        val result: MissingAgentClientDetails = intercept[MissingAgentClientDetails](await(testService.getSessionData(sessionId)))
        result.message mustBe "Session Data service and Session Cookie both returned empty data"
      }

      "should return data when fallback is enabled and succeeds" in {
        mockSessionServiceEnabled(true)
        mockGetSessionDataFromSessionStore(Left(dummyError))

        implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
          .withSession(
            ("ClientNino", "AA111111A"),
            ("ClientMTDID", "12345678")
          )

        val result: SessionData = await(testService.getSessionData(sessionId))
        result mustBe SessionData(sessionId = sessionId, mtditid = "12345678", nino = "AA111111A")
      }
    }

    "should return data when session data retrieval is successful" in {
      mockSessionServiceEnabled(true)
      mockGetSessionDataFromSessionStore(Right(Some(dummyResponse)))

      implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

      val result: SessionData = await(testService.getSessionData(sessionId))
      result mustBe dummyResponse
    }
  }

  "getFallbackSessionData" -> {

    "should return an error when data is not present in request session" in {
      implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
      val result: Option[SessionData] = testService.getFallbackSessionData(sessionId)

      result mustBe None
    }

    "should return data when data is present in request session" in {
      implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withSession(
        ("ClientNino", "AA111111A"),
        ("ClientMTDID", "12345678")
      )

      val result: Option[SessionData] = testService.getFallbackSessionData(sessionId)
      result.getOrElse(dummySessionData) mustBe SessionData(sessionId = sessionId, mtditid = "12345678", nino = "AA111111A")
    }
  }

  "getSessionData" -> {
    "if session cookie service is enabled" -> {
      "should return session data when it is returned from the session cookie service" in {
        mockSessionServiceEnabled(true)
        mockGetSessionDataFromSessionStore(Right(Some(dummyResponse)))

        val result = await(testService.getSessionDataFromSessionStore())
        result mustBe Some(dummyResponse)
      }

      "should return an error when no session data is returned from the session cookie service" in {
        mockSessionServiceEnabled(true)
        mockGetSessionDataFromSessionStore(Right(None))

        val result = await(testService.getSessionDataFromSessionStore())
        result mustBe None
      }

      "should return an error when an error is returned from the session cookie service" in {
        mockSessionServiceEnabled(true)
        mockGetSessionDataFromSessionStore(Left(dummyError))

        val result = await(testService.getSessionDataFromSessionStore())
        result mustBe None
      }
    }

    "if session cookie service is disabled" -> {
      "should return None" in {
        mockSessionServiceEnabled(false)

        val result = await(testService.getSessionDataFromSessionStore())
        result mustBe None
      }
    }
  }
}

