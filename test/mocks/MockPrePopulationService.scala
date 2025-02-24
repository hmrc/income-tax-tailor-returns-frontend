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

import connectors.{ConnectorResponse, HttpResult}
import models.prePopulation.EsaJsaPrePopulationResponse
import org.scalamock.handlers.CallHandler5
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import services.PrePopulationService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait MockPrePopulationService extends MockFactory { this: TestSuite =>
  val mockPrePopulationService: PrePopulationService = mock[PrePopulationService]

  type MockType = CallHandler5[String, Int, String, HeaderCarrier, ExecutionContext,
    ConnectorResponse[EsaJsaPrePopulationResponse]]

  def mockGetEsaJsa(nino: String,
                    taxYear: Int,
                    mtdItId: String,
                    response: HttpResult[EsaJsaPrePopulationResponse]): MockType =
    (mockPrePopulationService
      .getEsaJsa(_: String, _: Int, _: String)(_: HeaderCarrier, _: ExecutionContext))
      .expects(nino, taxYear, mtdItId, *, *)
      .returning(Future.successful(response))

  def mockGetEsaJsaException(nino: String,
                             taxYear: Int,
                             mtdItId: String,
                             ex: Throwable): MockType =
    (mockPrePopulationService
      .getEsaJsa(_: String, _: Int, _: String)(_: HeaderCarrier, _: ExecutionContext))
      .expects(nino, taxYear, mtdItId, *, *)
      .returning(Future.failed(ex))



}
