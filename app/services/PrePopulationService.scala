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

import connectors.{ConnectorResponse, StateBenefitsConnector, EmploymentConnector}
import models.prePopulation.{StateBenefitsPrePopulationResponse, EmploymentPrePopulationResponse}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}

@Singleton
class PrePopulationService @Inject()(stateBenefitsConnector: StateBenefitsConnector, employmentConnector: EmploymentConnector) {

  def getStateBenefits(nino: String, taxYear: Int, mtdItId: String)
                      (implicit hc: HeaderCarrier): ConnectorResponse[StateBenefitsPrePopulationResponse] = {
    stateBenefitsConnector.getPrePopulation(nino, taxYear, mtdItId)
  }

  def getEmployment(nino: String, taxYear: Int, mtdItId: String)
                   (implicit hc: HeaderCarrier): ConnectorResponse[EmploymentPrePopulationResponse] = {
    employmentConnector.getPrePopulation(nino, taxYear, mtdItId)
  }
}
