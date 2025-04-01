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
import play.api.http.Status.{IM_A_TEAPOT, INTERNAL_SERVER_ERROR}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.Helpers.await
import play.api.test.{DefaultAwaitTimeout, FakeRequest}
import uk.gov.hmrc.http.HeaderCarrier

class SessionDataServiceSpec extends SpecBase
  with MockSessionDataConnector
  with MockAppConfig
  with DefaultAwaitTimeout {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val testService = new SessionDataService(
    sessionDataConnector = mockSessionDataConnector,
    config = mockAppConfig
  )

  val dummyResponse: SessionData = SessionData(
    mtditid = "111111", nino = "AA111111A", utr = "123456", sessionId = "xxxxxxx"
  )

  val dummyError: APIErrorModel = APIErrorModel(IM_A_TEAPOT, APIErrorBodyModel("", ""))

  "getNino" -> {
    "if session cookie service is enabled" -> {
      implicit val request: FakeRequest[AnyContentAsEmpty.type] =
        FakeRequest.apply("", "").withSession("ClientNino" -> "value")
      "should return NINO when call to session cookie service is successful" in {
        mockGetSessionData(Right(Some(dummyResponse)))
        mockSessionServiceEnabled(true)
        val result = await(testService.getNino())

        result mustBe a[Right[_, _]]
        result.getOrElse("dummy val") mustBe "AA111111A"
      }

      "should fallback to local session NINO when session cookie service returns None" in {
        mockGetSessionData(Right(None))
        mockSessionServiceEnabled(true)
        val result = await(testService.getNino())

        result mustBe a[Right[_, _]]
        result.getOrElse("dummy val") mustBe "value"
      }

      "should fallback to local session NINO when session cookie service call fails" in {
        mockGetSessionData(Left(APIErrorModel(INTERNAL_SERVER_ERROR, APIErrorBodyModel("", ""))))
        mockSessionServiceEnabled(true)
        val result = await(testService.getNino())

        result mustBe a[Right[_, _]]
        result.getOrElse("dummy val") mustBe "value"
      }

      "should return a Left when local session and session cookie service fail to return NINO" in {
        implicit val request: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest.apply("", "").withSession("NotNino" -> "value")

        mockGetSessionData(Left(APIErrorModel(INTERNAL_SERVER_ERROR, APIErrorBodyModel("", ""))))
        mockSessionServiceEnabled(true)
        val result = await(testService.getNino())

        result mustBe a[Left[_, _]]
      }

      "should return an exception when an error occurs during session cookie service call" in {
        mockGetSessionDataException(new RuntimeException)
        mockSessionServiceEnabled(true)
        assertThrows[RuntimeException](await(testService.getNino()))
      }
    }

    "if session cookie service is disabled" -> {
      "should return the NINO if it is in the session" in {
        implicit val request: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest.apply("", "").withSession("ClientNino" -> "value")

        mockSessionServiceEnabled(false)
        val result = await(testService.getNino())

        result mustBe a[Right[_, _]]
        result.getOrElse("Not nino") mustBe "value"
      }

      "should return a Left if the Nino is not in the session" in {
        implicit val request: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest.apply("", "").withSession("NotClientNino" -> "value")

        mockSessionServiceEnabled(false)
        val result = await(testService.getNino())

        result mustBe a[Left[_, _]]
      }
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
        mockGetSessionData(Left(APIErrorModel(INTERNAL_SERVER_ERROR, APIErrorBodyModel("", ""))))

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

  "sessionValOpt" -> {
    "when a given key does not exist within the session should return an error" in {
      implicit val request: FakeRequest[AnyContentAsEmpty.type] =
        FakeRequest.apply("", "").withSession("notKey" -> "value")

      val result = testService.sessionValOpt("key", "key", _ => (), _ => ())

      result mustBe a[Left[_, _]]
    }

    "when a key exists in the session should return it" in {
      implicit val request: FakeRequest[AnyContentAsEmpty.type] =
        FakeRequest.apply("", "").withSession("key" -> "value")

      val result = testService.sessionValOpt("key", "key", _ => (), _ => ())

      result mustBe a[Right[_, _]]
      result.getOrElse("dummy string") mustBe "value"
    }
  }
}

