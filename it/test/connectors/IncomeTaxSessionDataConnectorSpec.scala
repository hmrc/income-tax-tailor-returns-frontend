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
import models.{APIErrorBodyModel, APIErrorModel}
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
import play.api.mvc.Results.{InternalServerError, NotFound}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext

class IncomeTaxSessionDataConnectorSpec
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

  implicit private lazy val hc: HeaderCarrier = HeaderCarrier()
  private val taxYear: Int = 2024
  private val mtdItId: String = "1234567890"
  private lazy val connector = app.injector.instanceOf[IncomeTaxSessionDataConnector]
  private val sessionId = "test-session-id"
  private val testUrl = s"/income-tax-session-data/$sessionId"
  private val sessionDataResponse = SessionData.empty.copy(mtditid = mtdItId)

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
      val result: SessionDataResponse = connector.getSessionData(sessionId)(hc).futureValue
      result shouldBe Right(Some((sessionDataResponse)))
    }


    "log failure when getSessionData fails with Internal Error" in {
      stubGet(testUrl, INTERNAL_SERVER_ERROR,
        InternalServerError(s"Failed to retrieve session with id: $sessionId").toString())
      val result: SessionDataResponse = connector.getSessionData(sessionId)(hc).futureValue
      result shouldBe Left(APIErrorModel(500, APIErrorBodyModel("PARSING_ERROR", "Error parsing response from API")))
    }

//    "log failure when getSessionData returns No Data" in {
//      stubGet(testUrl, NOT_FOUND,
//        NotFound("No session data found").toString())
//      val result: SessionDataResponse = connector.getSessionData(sessionId)(hc).futureValue
//      result shouldBe Left(APIErrorModel(404, APIErrorBodyModel("PARSING_ERROR", "NotFound")))
//    }
  }
}
