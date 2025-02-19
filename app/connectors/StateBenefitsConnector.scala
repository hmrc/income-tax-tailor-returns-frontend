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

package connectors

import config.FrontendAppConfig
import connectors.httpParsers.StandardGetHttpParser
import models.prePopulation.StateBenefitsPrePopulationResponse
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import utils.Logging

import java.net.URL
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class StateBenefitsConnector @Inject()(config: FrontendAppConfig, httpClient: HttpClientV2)
                                      (implicit ec: ExecutionContext)
  extends StandardGetHttpParser[StateBenefitsPrePopulationResponse]
  with Logging {
  val classLoggingContext: String = "StateBenefitsConnector"

  def getPrePopulation(nino: String, taxYear: Int)
                      (implicit hc: HeaderCarrier): ConnectorResponse[StateBenefitsPrePopulationResponse] = {
    val prePopulationUrl: URL = url"${config.stateBenefitsBaseUrl}/pre-population/$nino/$taxYear"

    logger.underlying.error(hc.authorization.get.toString)

    logger.info(
      methodContext = "[getPrePopulation]",
      message = "Attempting to retrieve user's pre-pop data for state benefits",
      dataLog = dataLogString(nino = nino, taxYear = taxYear)
    )

    httpClient
      .get(prePopulationUrl)
      .execute[HttpResult[StateBenefitsPrePopulationResponse]]
  }
}
