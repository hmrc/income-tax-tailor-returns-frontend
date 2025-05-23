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

import connectors.{ConnectorResponse, CisConnector, PropertyConnector, StateBenefitsConnector, EmploymentConnector}
import models.prePopulation.{EmploymentPrePopulationResponse, EsaJsaPrePopulationResponse, CisPrePopulationResponse, PropertyPrePopulationResponse}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class PrePopulationService @Inject()(
                                      stateBenefitsConnector: StateBenefitsConnector,
                                      cisConnector: CisConnector,
                                      employmentConnector: EmploymentConnector,
                                      propertyConnector: PropertyConnector
                                    ) {

  def getEsaJsa(nino: String, taxYear: Int, mtdItId: String)
               (implicit hc: HeaderCarrier, ec: ExecutionContext): ConnectorResponse[EsaJsaPrePopulationResponse] =
    for {
      result <- stateBenefitsConnector.getPrePopulation(nino, taxYear, mtdItId)
      esaJsaResult = result.map(_.toEsaJsaModel)
    } yield esaJsaResult


  def getCis(nino: String, taxYear: Int, mtdItId: String)
            (implicit hc: HeaderCarrier): ConnectorResponse[CisPrePopulationResponse] = {
    cisConnector.getPrePopulation(nino, taxYear, mtdItId)
  }

  def getEmployment(nino: String, taxYear: Int, mtdItId: String)
                   (implicit hc: HeaderCarrier): ConnectorResponse[EmploymentPrePopulationResponse] = {
    employmentConnector.getPrePopulation(nino, taxYear, mtdItId)
  }

  def getProperty(nino: String, taxYear: Int, mtdItId: String)(implicit hc: HeaderCarrier): ConnectorResponse[PropertyPrePopulationResponse] = {
    propertyConnector.getPrePopulation(nino, taxYear, mtdItId)
  }
}
