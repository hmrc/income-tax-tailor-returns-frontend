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

package connectors.httpParser

import models.errors.HttpErrorBody.{MultiErrorsBody, SingleErrorBody}
import models.errors.{HttpError, HttpErrorBody}
import play.api.http.Status._
import uk.gov.hmrc.http.HttpResponse
import utils.PagerDutyHelper.PagerDutyKeys._
import utils.PagerDutyHelper.{getCorrelationId, pagerDutyLog}

import scala.util.{Failure, Success, Try}

trait HttpParser {
  val parserName: String

  def logMessage(response: HttpResponse): String =
    s"[$parserName][read] Received ${response.status} from $parserName. Body:${response.body} ${getCorrelationId(response)}"

  def nonModelValidatingJsonFromAPI: HttpError = {
    pagerDutyLog(BAD_SUCCESS_JSON_FROM_CONNECTOR, s"[$parserName][read] Invalid Json from $parserName")
    HttpError(INTERNAL_SERVER_ERROR, HttpErrorBody.parsingError)
  }

  def handleHttpError(response: HttpResponse, statusOverride: Option[Int] = None): HttpError = {
    val status = statusOverride.getOrElse(response.status)
    Try {
      response.json.asOpt[SingleErrorBody] match {
        case Some(singleError) => HttpError(status, singleError)
        case None              => HttpError(status, response.json.as[MultiErrorsBody])
      }
    } match {
      case Success(leftError) => leftError
      case Failure(t) =>
        pagerDutyLog(UNEXPECTED_RESPONSE_FROM_CONNECTOR, s"[$parserName][read] Unexpected Json error: ${t.getMessage} from $parserName.")
        HttpError(status, HttpErrorBody.parsingError)
    }

  }

  def pagerDutyError(response: HttpResponse): HttpError =
    response.status match {
      case BAD_REQUEST =>
        pagerDutyLog(FOURXX_RESPONSE_FROM_CONNECTOR, logMessage(response))
        handleHttpError(response)
      case NOT_FOUND =>
        pagerDutyLog(FOURXX_RESPONSE_FROM_CONNECTOR, logMessage(response))
        handleHttpError(response)
      case INTERNAL_SERVER_ERROR =>
        pagerDutyLog(INTERNAL_SERVER_ERROR_FROM_CONNECTOR, logMessage(response))
        handleHttpError(response)
      case SERVICE_UNAVAILABLE =>
        pagerDutyLog(SERVICE_UNAVAILABLE_FROM_CONNECTOR, logMessage(response))
        handleHttpError(response)
      case _ =>
        pagerDutyLog(UNEXPECTED_RESPONSE_FROM_CONNECTOR, logMessage(response))
        handleHttpError(response, Some(INTERNAL_SERVER_ERROR))
    }
}
