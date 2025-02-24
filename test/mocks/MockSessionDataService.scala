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

package mocks

import org.scalamock.handlers.CallHandler4
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import play.api.mvc.Request
import services.SessionDataService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait MockSessionDataService extends MockFactory {this: TestSuite =>
  val mockSessionDataService: SessionDataService = mock[SessionDataService]

  private type MockType = CallHandler4[String, Request[_], HeaderCarrier, ExecutionContext, Future[Either[Unit, String]]]

  def mockGetNino(result: Either[Unit, String]): MockType  =
    (mockSessionDataService
      .getNino(_: String)(_: Request[_], _: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *, *)
      .returning(Future.successful(result))

  def mockGetNinoException(err: Throwable): MockType =
    (mockSessionDataService
      .getNino(_: String)(_: Request[_], _: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *, *)
      .returning(Future.failed(err))
}

