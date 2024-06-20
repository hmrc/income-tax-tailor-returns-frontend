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

import models.errors.HttpError
import models.errors.HttpErrorBody.{MultiErrorsBody, SingleErrorBody}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HttpResponse

trait HttpParserBehaviours extends AnyFreeSpec with Matchers {

  object FakeParser extends HttpParser {
    val parserName: String = "TestParser"
  }

  val serviceUnavailableReason = "Dependent systems are currently not responding."
  val serverErrorReason        = "CONNECTOR is currently experiencing problems that require live service intervention."
  val parsingErrorReason       = "Error parsing response from CONNECTOR"

  val svcUnavailJs    = s"""{"code":"SERVICE_UNAVAILABLE", "reason":"$serviceUnavailableReason"}"""
  val svrErrJs        = s"""{"code":"SERVER_ERROR", "reason":"$serverErrorReason"}"""
  val multiErrJs      = s"""{"failures":[$svcUnavailJs, $svrErrJs], "reason":""}""".stripMargin
  val nonValidatingJs = s"""{"code":"SERVER_ERROR", "reasons":"$serverErrorReason"}"""

  def failureHttpResponse(json: JsValue): HttpResponse =
    HttpResponse(INTERNAL_SERVER_ERROR, json, Map("CorrelationId" -> Seq("1234645654645")))

  def returnParsingErrors(): Unit =
    "return a parsing error" - {
      returnJsonValidationError()
      handleNonSingleOrMultiErrorResponseError()
      handleNonJsonResponseBodyError()
    }

  def logHttpResponse(): Unit =
    "log the correct message" in {
      val result = FakeParser.logMessage(failureHttpResponse(Json.parse(multiErrJs)))
      result mustBe // note*: the spacings in the String below are important and the test will fail if altered
        s"""[TestParser][read] Received 500 from ${FakeParser.parserName}. Body:{
           |  "failures" : [ {
           |    "code" : "SERVICE_UNAVAILABLE",
           |    "reason" : "$serviceUnavailableReason"
           |  }, {
           |    "code" : "SERVER_ERROR",
           |    "reason" : "$serverErrorReason"
           |  } ],
           |  "reason" : ""
           |}  CorrelationId: 1234645654645""".stripMargin
    }

  def handleSingleError(): Unit =
    "handle a single error" in {
      val result = FakeParser.handleHttpError(failureHttpResponse(Json.parse(svrErrJs)))
      result mustBe HttpError(INTERNAL_SERVER_ERROR, SingleErrorBody("SERVER_ERROR", serverErrorReason))
    }

  def handleMultpleError(): Unit =
    "handle a multiple error" in {
      val result = FakeParser.handleHttpError(failureHttpResponse(Json.parse(multiErrJs)))
      result mustBe
        HttpError(
          INTERNAL_SERVER_ERROR,
          MultiErrorsBody(
            Seq(
              SingleErrorBody("SERVICE_UNAVAILABLE", serviceUnavailableReason),
              SingleErrorBody("SERVER_ERROR", serverErrorReason)
            ))
        )
    }

  def returnJsonValidationError(): Unit =
    "return a non model validating json error" in {
      val result = FakeParser.handleHttpError(failureHttpResponse(Json.parse(nonValidatingJs)))
      result mustBe HttpError(INTERNAL_SERVER_ERROR, SingleErrorBody("PARSING_ERROR", parsingErrorReason))
    }

  def handleNonSingleOrMultiErrorResponseError(): Unit =
    "handling a response that is neither a single or a multiple error" in {
      val result = FakeParser.handleHttpError(failureHttpResponse(Json.obj()))
      result mustBe HttpError(INTERNAL_SERVER_ERROR, SingleErrorBody("PARSING_ERROR", parsingErrorReason))
    }

  def handleNonJsonResponseBodyError(): Unit =
    "handling a response where the response body is not json" in {
      val result = FakeParser.handleHttpError(HttpResponse(INTERNAL_SERVER_ERROR, "", Map("CorrelationId" -> Seq("1234645654645"))))
      result mustBe HttpError(INTERNAL_SERVER_ERROR, SingleErrorBody("PARSING_ERROR", parsingErrorReason))
    }

}
