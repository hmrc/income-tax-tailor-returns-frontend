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
import config.FrontendAppConfig
import mocks.MockAppConfig
import play.api.mvc.Results.Ok
import play.api.mvc.{AnyContent, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}

import scala.concurrent.Future

class SessionIdHelperSpec extends SpecBase
  with MockAppConfig {

  val testHelper: SessionIdHelper = new SessionIdHelper {
    override val config: FrontendAppConfig = mockAppConfig
  }

  "withSessionId" - {
    "should process block when sessionId is present in the header carrier" in {

      implicit val hc: HeaderCarrier = HeaderCarrier().copy(sessionId = Some(SessionId(sessionId)))
      implicit val request: Request[AnyContent] = FakeRequest()

      val result: Future[Result] = testHelper.withSessionId(taxYear)(
        (sessionId: String) => Future.successful(Ok(sessionId))
      )

      status(result) mustBe OK
      contentAsString(result) mustBe sessionId
    }

    "should process block when sessionId is present in the request headers" in {

      implicit val hc: HeaderCarrier = HeaderCarrier()
      implicit val request: Request[AnyContent] = FakeRequest().withHeaders(("sessionId", sessionId))

      val result: Future[Result] = testHelper.withSessionId(taxYear)(
        (sessionId: String) => Future.successful(Ok(sessionId))
      )

      status(result) mustBe OK
      contentAsString(result) mustBe sessionId
    }

    "should redirect to Auth Login when sessionId is not present" in {
      implicit val hc: HeaderCarrier = HeaderCarrier()
      implicit val request: Request[AnyContent] = FakeRequest()

      mockLoginUrl("/login")

      val result: Future[Result] = testHelper.withSessionId(taxYear)(
        (sessionId: String) => Future.successful(Ok(sessionId))
      )

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some("/login")
    }
  }
}
