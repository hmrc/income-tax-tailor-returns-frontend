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

import connectors.{ConnectorResponse, HttpResult, StateBenefitsConnector}
import models.prePopulation.StateBenefitsPrePopulationResponse
import org.scalamock.handlers.CallHandler3
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

trait MockStateBenefitsConnector extends MockFactory { this: TestSuite =>
  val mockStateBenefitsConnector: StateBenefitsConnector = mock[StateBenefitsConnector]

  type MockType = CallHandler3[String, Int, HeaderCarrier, ConnectorResponse[StateBenefitsPrePopulationResponse]]

  def mockGetPrePopulation(nino: String,
              taxYear: Int,
              response: HttpResult[StateBenefitsPrePopulationResponse]): MockType =
    (mockStateBenefitsConnector
      .getPrePopulation(_: String, _: Int)(_: HeaderCarrier))
      .expects(nino, taxYear, *)
      .returning(Future.successful(response))

  def mockGetPrePopulationException(nino: String,
                                    taxYear: Int,
                                    ex: Throwable): MockType =
    (mockStateBenefitsConnector
      .getPrePopulation(_: String, _: Int)(_: HeaderCarrier))
      .expects(nino, taxYear, *)
      .returning(Future.failed(ex))

}
