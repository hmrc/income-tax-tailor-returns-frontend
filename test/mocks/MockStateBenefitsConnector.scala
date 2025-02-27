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

import connectors.{ConnectorResponse, HttpResult, IncomeTaxCisConnector, StateBenefitsConnector}
import models.prePopulation.StateBenefitsPrePopulationResponse
import org.scalamock.handlers.CallHandler4
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

trait MockStateBenefitsConnector extends MockFactory { this: TestSuite =>
  val mockStateBenefitsConnector: StateBenefitsConnector = mock[StateBenefitsConnector]
  val mockSIncomeTaxCisConnector: IncomeTaxCisConnector = mock[IncomeTaxCisConnector]

  type MockType = CallHandler4[String, Int, String, HeaderCarrier,
    ConnectorResponse[StateBenefitsPrePopulationResponse]]

  def mockGetPrePopulation(nino: String,
                           taxYear: Int,
                           mtdItId: String,
                           response: HttpResult[StateBenefitsPrePopulationResponse]): MockType =
    (mockStateBenefitsConnector
      .getPrePopulation(_: String, _: Int, _: String)(_: HeaderCarrier))
      .expects(nino, taxYear, mtdItId, *)
      .returning(Future.successful(response))

  def mockGetPrePopulationException(nino: String,
                                    taxYear: Int,
                                    mtdItId: String,
                                    ex: Throwable): MockType =
    (mockStateBenefitsConnector
      .getPrePopulation(_: String, _: Int, _: String)(_: HeaderCarrier))
      .expects(nino, taxYear, mtdItId, *)
      .returning(Future.failed(ex))

}
