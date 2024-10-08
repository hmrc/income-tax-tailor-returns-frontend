/*
 * Copyright 2024 HM Revenue & Customs
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

import config.Service
import connectors.ConnectorFailureLogger.FromResultToConnectorFailureLogger
import connectors.httpParsers.SessionDataHttpParser.{SessionDataResponse, SessionDataResponseReads}
import play.api.Configuration
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import uk.gov.hmrc.http.client.HttpClientV2

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait IncomeTaxSessionDataConnector {
  def getSessionData(implicit hc: HeaderCarrier): Future[SessionDataResponse]
}

@Singleton
class IncomeTaxSessionDataConnectorImpl @Inject()(config: Configuration, httpClient: HttpClientV2)(implicit ec: ExecutionContext)
  extends IncomeTaxSessionDataConnector{

  def getSessionData(implicit hc: HeaderCarrier): Future[SessionDataResponse] = {

    val vcSessionServiceBaseUrl = config.get[Service]("microservice.services.income-tax-session-data")
    val url = s"$vcSessionServiceBaseUrl/income-tax-session-data/"
    httpClient
      .get(url"$url")
      .execute[SessionDataResponse]
      .logFailureReason(connectorName = "UserAnswersConnector on get")

  }
}