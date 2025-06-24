/*
 * Copyright 2023 HM Revenue & Customs
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
import connectors.ConnectorFailureLogger._
import models.Done
import models.tasklist.TaskListData
import play.api.Configuration
import play.api.http.Status.NO_CONTENT
import play.api.libs.json.Json
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReadsInstances, HttpResponse, StringContextOps, UpstreamErrorResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TaskListDataConnector @Inject()(config: Configuration, httpClient: HttpClientV2)
                                     (implicit ec: ExecutionContext) extends HttpReadsInstances {
  private val baseUrl = config.get[Service]("microservice.services.income-tax-tailor-return")
  private val taskListDataUrl = url"$baseUrl/income-tax-tailor-return/task-list/data"
  private val keepAliveUrl = url"$baseUrl/income-tax-tailor-return/task-list/keep-alive"

  def set(taskListData: TaskListData)(implicit hc: HeaderCarrier): Future[Done] = {
    httpClient
      .post(taskListDataUrl)
      .setHeader(("MTDITID", taskListData.mtdItId))
      .withBody(Json.toJson(taskListData))
      .execute[HttpResponse]
      .logFailureReason(connectorName = "TaskListDataConnector on set")
      .flatMap { response =>
        if (response.status == NO_CONTENT) {
          Future.successful(Done)
        } else {
          Future.failed(UpstreamErrorResponse("", response.status))
        }
      }
  }

  def keepAlive(mtdItId: String, taxYear: Int)(implicit hc: HeaderCarrier): Future[Done] =
    httpClient
      .post(url"$keepAliveUrl/$taxYear")
      .setHeader(("MTDITID", mtdItId))
      .execute[HttpResponse]
      .logFailureReason(connectorName = "TaskListDataConnector on keepAlive")
      .flatMap { response =>
        if (response.status == NO_CONTENT) {
          Future.successful(Done)
        } else {
          Future.failed(UpstreamErrorResponse("", response.status))
        }
      }
}
