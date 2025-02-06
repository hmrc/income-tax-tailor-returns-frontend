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

package connectors.httpParsers

import connectors.HttpResult
import models.errors.SimpleErrorWrapper
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json, Reads}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import utils.LoggerWithContext

import scala.util.{Failure, Success, Try}

abstract class StandardGetHttpParser[O: Reads] {
  val logger: LoggerWithContext

  private def logInfoWithContext(message: String,
                                 extraContext: Option[String],
                                 dataLog: String): Unit = logger.info(
    methodContext = "StandardGetHttpParser",
    message = message,
    dataLog = dataLog,
    extraContext = extraContext
  )

  private def logErrorWithContext(message: String,
                                  extraContext: Option[String],
                                  dataLog: String): Unit = logger.error(
    methodContext = "StandardGetHttpParser",
    message = message,
    dataLog = dataLog,
    extraContext = extraContext
  )

  /**
   * This HTTP reads object accepts an HttpResponse and returns a result of the type Either[SimpleErrorWrapper, O].
   * The type 'O' represents the expected data output. It must have an implicit JSON reads method defined in scope.
   * SimpleErrorWrapper wraps only the HTTP status associated with an error response.
   *
   * N.B - Any error response bodies will not be retained.
   *       This is because error bodies are generally not returned from ITSASS backend microservices.
   *       Take a look at APIParser & SessionDataHttpParser to see how error bodies may be handled.
   */
  implicit object StandardGetHttpReads extends HttpReads[HttpResult[O]] {
    override def read(method: String, url: String, response: HttpResponse): HttpResult[O] = {
      val dataLog = s" for request with url: $url, and method: $method"

      response.status match {
        case OK =>
          logInfoWithContext(
            message = "Received a 200 - OK success response status. Attempting to parse response body to JSON",
            extraContext = Some("read"),
            dataLog = dataLog
          )
          parseToDataModel(response.body, dataLog)
        case errorStatus =>
          logErrorWithContext(
            message = s"Received an unexpected $errorStatus response status. Returning error",
            extraContext = Some("read"),
            dataLog = dataLog + s". Error body: ${response.body}" // We may need to add sanitisation here
          )
          Left(SimpleErrorWrapper(errorStatus))
      }
    }
  }

  protected[httpParsers] def parseToDataModel(jsonString: String, dataLog: String): HttpResult[O] =
    Try(Json.parse(jsonString)) match {
      case Success(json: JsValue) =>
        logInfoWithContext(
          message = "Response body successfully parsed to JSON. Attempting to parse JSON to data model",
          extraContext = Some("parseToDataModel"),
          dataLog = dataLog
        )
        json.validate[O] match {
          case JsSuccess(value, _) =>
            logInfoWithContext(
              "Response body JSON successfully parsed to data model. Returning data",
              extraContext = Some("parseToDataModel"),
              dataLog = dataLog
            )
            Right(value)
          case JsError(err) =>
            logErrorWithContext(
              message = s"Response body JSON failed to parse to data model. Returning an internal server error",
              extraContext = Some("parseToDataModel"),
              dataLog = dataLog + ". Failure reason: " + err
            )
            Left(SimpleErrorWrapper(INTERNAL_SERVER_ERROR))
        }
      case Failure(ex) =>
        logger.errorWithException(
          methodContext = "StandardGetHttpParser",
          message = "Response body could not be parsed to JSON. Returning an internal server error",
          ex = ex,
          dataLog = dataLog,
          extraContext = Some("parseToDataModel")
        )
        Left(SimpleErrorWrapper(INTERNAL_SERVER_ERROR))
  }


}
