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
import models.errors.{APIErrorBodyModel, APIErrorModel}
import models.session.SessionData
import play.api.http.{HeaderNames, Status}
import play.api.mvc.{AnyContentAsEmpty, Results}
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
    mtditid = "111111", nino = "AA111111A", utr = "123456", sessionId = "xxxxxxx"
  )

  val dummyError: APIErrorModel = APIErrorModel(IM_A_TEAPOT, APIErrorBodyModel("", ""))

  "getFallbackSessionData" -> {
    "should return an error when request session fallback is disabled" in {
      mockFallbackEnabled(false)

      implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
      val result: Either[Unit, SessionData] = testService.getFallbackSessionData(None)

      result mustBe a[Left[_, _]]
    }

    "should return an error when fallback is enabled but data is not present in request session" in {
      mockFallbackEnabled(true)

      implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
      val result: Either[Unit, SessionData] = testService.getFallbackSessionData(None)

      result mustBe a[Left[_, _]]
    }

    "should return data when fallback is enabled and data is present in request session" in {
      mockFallbackEnabled(true)

      implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withSession(
        ("ClientNino", "AA111111A"),
        ("ClientMTDID", "12345678")
      )

      val result: Either[Unit, SessionData] = testService.getFallbackSessionData(None)

      result mustBe a[Right[_, _]]
      result.getOrElse(dummySessionData) mustBe SessionData("12345678", "AA111111A", "", "")
    }
  }

  "getSessionData" -> {
    "if session cookie service is enabled" -> {
      "should return session data when it is returned from the session cookie service" in {
        mockSessionServiceEnabled(true)
        mockGetSessionData(Right(Some(dummyResponse)))

        val result = await(testService.getSessionData())
        result mustBe a[Right[_, _]]
        result.getOrElse(SessionData.empty) mustBe dummyResponse
      }

      "should return an error when no session data is returned from the session cookie service" in {
        mockSessionServiceEnabled(true)
        mockGetSessionData(Right(None))

        val result = await(testService.getSessionData())
        result mustBe a[Left[_, _]]
      }

      "should return an error when an error is returned from the session cookie service" in {
        mockSessionServiceEnabled(true)
        mockGetSessionData(Left(dummyError))

        val result = await(testService.getSessionData())
        result mustBe a[Left[_, _]]
      }
    }

    "if session cookie service is disabled" -> {
      "should return an error" in {
        mockSessionServiceEnabled(false)

        val result = await(testService.getSessionData())
        result mustBe a[Left[_, _]]
      }
    }
  }

  "getSessionDataBlock" -> {
    "when call to retrieve session data fails" -> {
      "should return an error when fallback is disabled" in {
        mockSessionServiceEnabled(true)
        mockFallbackEnabled(false)
        mockGetSessionData(Left(dummyError))

        implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

        val result = testService.getSessionDataBlock(
          () => Future.successful(ImATeapot("This is the error action"))
        )(
          _ => Future.successful(ImATeapot("This is the success action"))
        )

        status(result) mustBe IM_A_TEAPOT
        contentAsString(result) mustBe "This is the error action"
      }

      "should return an error when fallback is enabled but fails" in {
        mockSessionServiceEnabled(true)
        mockFallbackEnabled(true)
        mockGetSessionData(Left(dummyError))

        implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

        val result = testService.getSessionDataBlock(
          () => Future.successful(ImATeapot("This is the error action"))
        )(
          _ => Future.successful(ImATeapot("This is the success action"))
        )

        status(result) mustBe IM_A_TEAPOT
        contentAsString(result) mustBe "This is the error action"
      }

      "should return data when fallback is enabled and succeeds" in {
        mockSessionServiceEnabled(true)
        mockFallbackEnabled(true)
        mockGetSessionData(Left(dummyError))

        implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
          .withSession(
            ("ClientNino", "AA111111A"),
            ("ClientMTDID", "12345678")
          )

        val result = testService.getSessionDataBlock(
          () => Future.successful(ImATeapot("This is the error action"))
        )(
          _ => Future.successful(ImATeapot("This is the success action"))
        )

        status(result) mustBe IM_A_TEAPOT
        contentAsString(result) mustBe "This is the success action"
      }
    }

    "should return data when session data retrieval is successful" in {
      mockSessionServiceEnabled(true)
      mockGetSessionData(Right(Some(dummyResponse)))
      implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

      val result = testService.getSessionDataBlock(
        () => Future.successful(ImATeapot("This is the error action"))
      )(
        _ => Future.successful(ImATeapot("This is the success action"))
      )

      status(result) mustBe IM_A_TEAPOT
      contentAsString(result) mustBe "This is the success action"
    }
  }
}

