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

import config.FrontendAppConfig
import connectors.ConnectorFailureLogger.FromResultToConnectorFailureLogger
import connectors.httpParsers.SessionDataHttpParser.{SessionDataResponse, SessionDataResponseReads}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait SessionDataConnector {
  def getSessionData(implicit hc: HeaderCarrier): Future[SessionDataResponse]
}

@Singleton
class SessionDataConnectorImpl @Inject()(config: FrontendAppConfig, httpClient: HttpClientV2)(implicit ec: ExecutionContext)
  extends SessionDataConnector {

  def getSessionData(implicit hc: HeaderCarrier): Future[SessionDataResponse] = {
    val url = s"${config.vcSessionServiceBaseUrl}/income-tax-session-data/"
    httpClient
      .get(url"$url")
      .execute[SessionDataResponse]
      .logFailureReason(connectorName = "UserAnswersConnector on get")

  }
}