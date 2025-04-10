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

package controllers.actions

import base.SpecBase
import models.requests.DataRequest
import play.api.Application
import play.api.http.{HeaderNames, Status}
import play.api.mvc._
import play.api.test.{DefaultAwaitTimeout, FakeRequest, ResultExtractors}

import scala.concurrent.Future

class OverrideRequestActionSpec extends SpecBase
  with Results
  with HeaderNames
  with Status
  with ResultExtractors
  with DefaultAwaitTimeout {

  trait Test {
    val application: Application = applicationBuilder(userAnswers = None, anAgent).build()
    implicit val parser: BodyParsers.Default = application.injector.instanceOf[BodyParsers.Default]

    def testAction(req: DataRequest[_]) = new OverrideRequestActionImpl(req)
  }

  "OverrideRequestAction" -> {
    "should invoke block with overriden request" in new Test {
      val overrideRequest: DataRequest[AnyContentAsEmpty.type] = DataRequest(
        request = FakeRequest(),
        sessionData = dummySessionData,
        userAnswers = emptyUserAnswers,
        isAgent = true
      )

      val result: Future[Result] = testAction(overrideRequest).async(
        request => Future.successful(Ok(request.toString()))
      )(FakeRequest())

      contentAsString(result) mustBe overrideRequest.toString()
    }
  }

}
