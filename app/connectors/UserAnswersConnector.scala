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
import models.{Done, UserAnswers}
import play.api.Configuration
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps, UpstreamErrorResponse}
import uk.gov.hmrc.http.client.HttpClientV2
import ConnectorFailureLogger._
import play.api.http.Status.NO_CONTENT
import play.api.libs.json.Json

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UserAnswersConnector @Inject()(config:Configuration, httpClient:HttpClientV2)(implicit ec:ExecutionContext) {
  private val baseUrl = config.get[Service]("microservice.services.income-tax-tailor-return")
  private val userAnswersUrl = url"$baseUrl/tailor-return/data"

  def get()(implicit hc: HeaderCarrier): Future[Option[UserAnswers]] =
    httpClient
      .get(userAnswersUrl)
      .setHeader(("MTDITID","1234567890"))
      .execute[Option[UserAnswers]]
      .logFailureReason(connectorName = "UserAnswersConnector on get")

  def set(answers: UserAnswers)(implicit hc: HeaderCarrier): Future[Done] =
    httpClient
      .post(userAnswersUrl)
      .setHeader(("MTDITID","1234567890"))
      .withBody(Json.toJson(answers))
      .execute[HttpResponse]
      .logFailureReason(connectorName = "UserAnswersConnector on set")
      .flatMap { response =>
        if (response.status == NO_CONTENT) {
          Future.successful(Done)
        } else {
          Future.failed(UpstreamErrorResponse("", response.status))
        }
      }

  def clear()(implicit hc: HeaderCarrier): Future[Done] =
    httpClient
      .delete(userAnswersUrl)
      .setHeader(("MTDITID","1234567890"))
      .execute[HttpResponse]
      .logFailureReason(connectorName = "UserAnswersConnector on clear")
      .flatMap { response =>
        if (response.status == NO_CONTENT) {
          Future.successful(Done)
        } else {
          Future.failed(UpstreamErrorResponse("", response.status))
        }
      }
}
