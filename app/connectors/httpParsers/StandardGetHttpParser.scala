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
import utils.Logging

import scala.util.{Failure, Success, Try}

/**
 * @param reads$O$0 Reads[O]. An instance of Play JSON Reads defined for the type O.
 * @tparam O A generic type representing the expected HTTP response body format. This is typically a data model.
 *           Any type O must have defined an instance of Reads[O] for this class to be extended.
 */
abstract class StandardGetHttpParser[O: Reads] {_: Logging =>

  private val interfaceLoggingContext: String = "StandardGetHttpParser"

  private def logErrorWithContext(message: String,
                                  extraContext: Option[String],
                                  dataLog: String): Unit = logger.error(
    secondaryContext = interfaceLoggingContext,
    message = message,
    dataLog = dataLog,
    extraContext = extraContext
  )

  /**
   * This implicit object implements HttpReads for the type Either[SimpleErrorWrapper, O].
   * This provides generic functionality to parse HTTP responses to either an error, or an instance of
   * the data model O depending on some validation rules. HTTP Responses for which the status code is not OK, or where
   * the response body does not match the expected JSON format are returned as errors using the SimpleErrorWrapper model.
   * Otherwise, an instance of the data model O is returned.
   */
  implicit object StandardGetHttpReads extends HttpReads[HttpResult[O]] {
    override def read(method: String, url: String, response: HttpResponse): HttpResult[O] = {
      val dataLog = s" for request with url: $url, and method: $method"
      val methodLogContext: String = "read"

      response.status match {
        case OK =>
          logger.info(
            secondaryContext = interfaceLoggingContext,
            message = s"Received a success response with the expected status code: $OK. Validating response body format",
            extraContext = Some(methodLogContext),
            dataLog = dataLog
          )
          parseToDataModel(response.body, dataLog)
        case errorStatus =>
          logErrorWithContext(
            message = s"Received an error response with the unexpected status code: $errorStatus. Returning an error",
            extraContext = Some(methodLogContext),
            dataLog = dataLog + s". Error body: ${response.body}" // We may need to add sanitisation here
          )
          Left(SimpleErrorWrapper(errorStatus))
      }
    }
  }

  /**
   * This method will validate a raw HTTP response body string to ensure that it is both valid JSON, and that it
   * conforms to the expected format of the data model O.
   * @param rawBodyString A raw HTTP response body expressed as a string
   * @param dataLog A string used for logging. Contains details about the request URL and HTTP method in a log friendly format
   * @return A result of type Either[SimpleErrorWrapper, O]. If the provided HTTP response body string is valid JSON,
   *         and conforms to the expected format of the data model O an instance of Right[O] will be returned.
   *         Otherwise, an internal server error is returned expressed as an instance of Left[SimpleErrorWrapper]
   */
  protected[httpParsers] def parseToDataModel(rawBodyString: String, dataLog: String = ""): HttpResult[O] = {
    val methodLogContext: String = "parseToDataModel"
    val infoLogger: String => Unit = infoLog(interfaceLoggingContext, dataLog, Some(methodLogContext))
    infoLogger("Attempting to parse response body to JSON")

    Try(Json.parse(rawBodyString)) match {
      case Success(json: JsValue) =>
        infoLogger("Response body successfully parsed to JSON. Attempting to parse JSON to data model")
        json.validate[O] match {
          case JsSuccess(value, _) =>
            infoLogger("Response body JSON successfully parsed to data model. Returning data model")
            Right(value)
          case JsError(err) =>
            logErrorWithContext(
              message = s"Response body JSON failed to parse to data model. Returning an internal server error",
              extraContext = Some(methodLogContext),
              dataLog = dataLog + ". Failure reason: " + err
            )
            Left(SimpleErrorWrapper(INTERNAL_SERVER_ERROR))
        }
      case Failure(ex) =>
        logger.errorWithException(
          secondaryContext = "StandardGetHttpParser",
          message = "Response body could not be parsed to JSON. Returning an internal server error",
          ex = ex,
          dataLog = dataLog,
          extraContext = Some("parseToDataModel")
        )
        Left(SimpleErrorWrapper(INTERNAL_SERVER_ERROR))
    }
  }

}
