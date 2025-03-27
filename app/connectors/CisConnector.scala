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
import models.prePopulation.CisPrePopulationResponse
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import utils.Logging

import java.net.URL
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CisConnector @Inject()(config: FrontendAppConfig, httpClient: HttpClientV2)
                                     (implicit ec: ExecutionContext)
  extends StandardGetHttpParser[CisPrePopulationResponse]
  with Logging {
  val primaryContext: String = classOf[CisConnector].getSimpleName

  def getPrePopulation(nino: String, taxYear: Int, mtdItId:String)
                      (implicit hc: HeaderCarrier): ConnectorResponse[CisPrePopulationResponse] = {
    val prePopulationUrl: URL = url"${config.cisBaseUrl}/pre-population/$nino/$taxYear"

    logger.info(
      secondaryContext = "[getPrePopulation]",
      message = "Attempting to retrieve user's pre-pop data for CIS",
      dataLog = dataLogString(nino = nino, taxYear = taxYear)
    )

    httpClient
      .get(prePopulationUrl)
      .setHeader(("mtditid", mtdItId))
      .execute[HttpResult[CisPrePopulationResponse]]
  }
}
