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

import connectors.httpParsers.SessionDataHttpParser.SessionDataResponse
import generators.ModelGenerators
import models.errors.{APIErrorBodyModel, APIErrorModel, APIErrorsBodyModel}
import models.session.SessionData
import org.scalatest.{EitherValues, OptionValues}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import test.connectors.WireMockHelper
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext

class SessionDataConnectorSpec
  extends AnyFreeSpec
    with WireMockHelper
    with ScalaFutures
    with Matchers
    with IntegrationPatience
    with EitherValues
    with OptionValues
    with MockitoSugar
    with ModelGenerators {

  implicit val ec: ExecutionContext = ExecutionContext.global

  private val sessionId = "test-session-id"
  implicit private lazy val hc: HeaderCarrier = HeaderCarrier().withExtraHeaders("X-Session-ID"->sessionId)
  private val mtdItId: String = "1234567890"
  private val nino: String = "AA111111A"
  private lazy val connector = app.injector.instanceOf[SessionDataConnector]
  private val testUrl = s"/income-tax-session-data/"
  private val sessionDataResponse = SessionData(mtditid = mtdItId, nino = nino, sessionId = sessionId)

  private lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(
        "microservice.services.income-tax-session-data.port" -> wireMockPort,
        "microservice.services.income-tax-session-data.url" -> s"http://localhost:$wireMockPort"
      )
      .build()


  "IncomeTaxSessionDataConnector" - {

    "return session data when getSessionData is called" in {
      stubGet(testUrl, OK, Json.toJson(sessionDataResponse).toString())
      val result: SessionDataResponse = connector.getSessionData(hc).futureValue
      result shouldBe Right(Some(sessionDataResponse))
    }

    "log failure when getSessionData returns invalid response" in {
      stubGet(testUrl, OK, Json.toJson(None).toString())
      val result: SessionDataResponse = connector.getSessionData(hc).futureValue
      result shouldBe Left(APIErrorModel(INTERNAL_SERVER_ERROR, APIErrorBodyModel("PARSING_ERROR", "Error parsing response from API")))
    }

    "log failure when getSessionData fails with Internal Error" in {
      val serviceUnavailableError = APIErrorBodyModel("INTERNAL_SERVER_ERROR", s"Failed to retrieve session with id: $sessionId")
      stubGet(testUrl, INTERNAL_SERVER_ERROR,
        Json.toJson(serviceUnavailableError).toString())
      val result: SessionDataResponse = connector.getSessionData(hc).futureValue
      result shouldBe Left(APIErrorModel(INTERNAL_SERVER_ERROR, APIErrorBodyModel("INTERNAL_SERVER_ERROR", s"Failed to retrieve session with id: $sessionId")))
    }

    "log failure when getSessionData returns No Data" in {
      val noDataError = APIErrorBodyModel("NOT_FOUND", "No session data found")
      stubGet(testUrl, NOT_FOUND,
        Json.toJson(noDataError).toString())
      val result: SessionDataResponse = connector.getSessionData(hc).futureValue
      result shouldBe Left(APIErrorModel(NOT_FOUND, APIErrorBodyModel("NOT_FOUND", "No session data found")))
    }

    "log failure when getSessionData returns multiple error" in {
      val noDataError = APIErrorBodyModel("NOT_FOUND", "No session data found")
      val multipleError = APIErrorsBodyModel(Seq(noDataError))
      stubGet(testUrl, NOT_FOUND,
        Json.toJson(multipleError).toString())
      val result: SessionDataResponse = connector.getSessionData(hc).futureValue
      result shouldBe Left(APIErrorModel(NOT_FOUND,APIErrorsBodyModel(List(APIErrorBodyModel("NOT_FOUND", "No session data found")))))
    }

    "log failure when getSessionData returns SERVICE_UNAVAILABLE" in {
      val serviceUnavailableError = APIErrorBodyModel("SERVICE_UNAVAILABLE", "Internal Server error")
      stubGet(testUrl, SERVICE_UNAVAILABLE,
        Json.toJson(serviceUnavailableError).toString())
      val result: SessionDataResponse = connector.getSessionData(hc).futureValue
      result shouldBe Left(APIErrorModel(SERVICE_UNAVAILABLE, APIErrorBodyModel("SERVICE_UNAVAILABLE", "Internal Server error")))
    }

    "log failure when getSessionData fails with unknown error" in {
      val someRandomError = APIErrorBodyModel("TOO_MANY_REQUESTS", s"some random error")
      stubGet(testUrl, TOO_MANY_REQUESTS,
        Json.toJson(someRandomError).toString())
      val result: SessionDataResponse = connector.getSessionData(hc).futureValue
      result shouldBe Left(APIErrorModel(INTERNAL_SERVER_ERROR, APIErrorBodyModel("TOO_MANY_REQUESTS", s"some random error")))
    }

    "log failure when getSessionData fails when there is unexpected response format" in {
      stubGet(testUrl, TOO_MANY_REQUESTS,
        Json.toJson("unexpected response").toString())
      val result: SessionDataResponse = connector.getSessionData(hc).futureValue
      result shouldBe Left(APIErrorModel(INTERNAL_SERVER_ERROR, APIErrorBodyModel("PARSING_ERROR", "Error parsing response from API")))
    }
  }
}
