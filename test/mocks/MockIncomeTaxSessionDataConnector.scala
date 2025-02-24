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

import connectors.IncomeTaxSessionDataConnector
import connectors.httpParsers.SessionDataHttpParser.SessionDataResponse
import org.scalamock.handlers.CallHandler1
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

trait MockIncomeTaxSessionDataConnector extends MockFactory { this: TestSuite =>
  val mockIncomeTaxSessionDataConnector: IncomeTaxSessionDataConnector = mock[IncomeTaxSessionDataConnector]

  private type MockType = CallHandler1[HeaderCarrier, Future[SessionDataResponse]]

  def mockGetSessionData(resp: SessionDataResponse): MockType =
    (mockIncomeTaxSessionDataConnector
      .getSessionData(_: HeaderCarrier))
      .expects(*)
      .returning(Future.successful(resp))

  def mockGetSessionDataException(err: Throwable): MockType =
    (mockIncomeTaxSessionDataConnector
      .getSessionData(_: HeaderCarrier))
      .expects(*)
      .returning(Future.failed(err))
}

