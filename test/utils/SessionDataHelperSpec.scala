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

package utils

import base.SpecBase
import mocks.{MockAppConfig, MockSessionDataConnector}
import models.errors.{APIErrorBodyModel, APIErrorModel}
import models.session.SessionData
import play.api.http.{HeaderNames, Status}
import play.api.mvc.{AnyContentAsEmpty, Result, Results}
import play.api.test.{DefaultAwaitTimeout, FakeRequest, ResultExtractors}
import services.SessionDataService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class SessionDataHelperSpec extends SpecBase
  with ResultExtractors
  with HeaderNames
  with Status
  with Results
  with MockSessionDataConnector
  with MockAppConfig
  with DefaultAwaitTimeout {

  trait Test {
    implicit val hc: HeaderCarrier = HeaderCarrier()

    val testService: SessionDataService = new SessionDataService(
      sessionDataConnector = mockSessionDataConnector,
      config = mockAppConfig
    )

    case class DummyClass(sessionDataService: SessionDataService) extends TestLogging with SessionDataHelper

    val testDummyClass: DummyClass = DummyClass(testService)

    val dummyResponse: SessionData = SessionData(
      mtditid = "111111", nino = "AA111111A", utr = "123456", sessionId = "xxxxxxx"
    )

    val dummyError: APIErrorModel = APIErrorModel(IM_A_TEAPOT, APIErrorBodyModel("", ""))
  }

  "getSessionDataBlock" -> {
    "when call to retrieve session data fails" -> {
      "should return an error when fallback is disabled" in new Test {
        mockSessionServiceEnabled(true)
        mockFallbackEnabled(false)
        mockGetSessionData(Left(dummyError))

        implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

        val result: Future[Result] = testDummyClass.getSessionDataBlock(
          () => Future.successful(ImATeapot("This is the error action"))
        )(
          _ => Future.successful(ImATeapot("This is the success action"))
        )

        status(result) mustBe IM_A_TEAPOT
        contentAsString(result) mustBe "This is the error action"
      }

      "should return an error when fallback is enabled but fails" in new Test {
        mockSessionServiceEnabled(true)
        mockFallbackEnabled(true)
        mockGetSessionData(Left(dummyError))

        implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

        val result: Future[Result] = testDummyClass.getSessionDataBlock(
          () => Future.successful(ImATeapot("This is the error action"))
        )(
          _ => Future.successful(ImATeapot("This is the success action"))
        )

        status(result) mustBe IM_A_TEAPOT
        contentAsString(result) mustBe "This is the error action"
      }

      "should return data when fallback is enabled and succeeds" in new Test {
        mockSessionServiceEnabled(true)
        mockFallbackEnabled(true)
        mockGetSessionData(Left(dummyError))

        implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
          .withSession(
            ("ClientNino", "AA111111A"),
            ("ClientMTDID", "12345678")
          )

        val result: Future[Result] = testDummyClass.getSessionDataBlock(
          () => Future.successful(ImATeapot("This is the error action"))
        )(
          _ => Future.successful(ImATeapot("This is the success action"))
        )

        status(result) mustBe IM_A_TEAPOT
        contentAsString(result) mustBe "This is the success action"
      }
    }

    "should return data when session data retrieval is successful" in new Test {
      mockSessionServiceEnabled(true)
      mockGetSessionData(Right(Some(dummyResponse)))
      implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

      val result: Future[Result] = testDummyClass.getSessionDataBlock(
        () => Future.successful(ImATeapot("This is the error action"))
      )(
        _ => Future.successful(ImATeapot("This is the success action"))
      )

      status(result) mustBe IM_A_TEAPOT
      contentAsString(result) mustBe "This is the success action"
    }
  }
}
