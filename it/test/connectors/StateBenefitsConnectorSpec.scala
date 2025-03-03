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

import generators.ModelGenerators
import models.errors.{APIErrorBodyModel, SimpleErrorWrapper}
import models.prePopulation.StateBenefitsPrePopulationResponse
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatest.{EitherValues, OptionValues}
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.http.Status._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{Json, OWrites}
import test.connectors.WireMockHelper
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext

class StateBenefitsConnectorSpec
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

  implicit private lazy val hc: HeaderCarrier = HeaderCarrier().withExtraHeaders("X-Session-ID"-> sessionId)

  private val mtdItId: String = "1234567890"
  private val nino: String = "AA111111A"
  private val taxYear: Int = 2024

  private lazy val connector = app.injector.instanceOf[StateBenefitsConnector]
  private val testUrl = s"/income-tax-state-benefits/pre-population/$nino/$taxYear"

  private val prePopResponse = StateBenefitsPrePopulationResponse(
    hasEsaPrePop = true,
    hasJsaPrePop = false,
    hasPensionsPrePop = true,
    hasPensionLumpSumsPrePop = true
  )

  implicit val writes: OWrites[StateBenefitsPrePopulationResponse] =
    Json.writes[StateBenefitsPrePopulationResponse]

  private lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(
        "microservice.services.income-tax-state-benefits.port" -> wireMockPort,
        "microservice.services.income-tax-state-benefits.url" -> s"http://localhost:$wireMockPort"
      )
      .build()


  "StateBenefitsConnector" - {
    "return pre population data when a success response is received" in {
      stubGet(testUrl, OK, Json.toJson(prePopResponse).toString())
      val result: ConnectorResponse[StateBenefitsPrePopulationResponse] = connector.getPrePopulation(nino, taxYear, mtdItId)
      result.futureValue shouldBe Right(prePopResponse)
    }

    "log failure when getPrePopulation returns invalid response" in {
      stubGet(testUrl, OK, Json.toJson(None).toString())
      val result: ConnectorResponse[StateBenefitsPrePopulationResponse] = connector.getPrePopulation(nino, taxYear, mtdItId)
      result.futureValue shouldBe Left(SimpleErrorWrapper(INTERNAL_SERVER_ERROR))
    }

    "log failure when getPrePopulation fails with some error" in {
      val serviceUnavailableError = APIErrorBodyModel("INTERNAL_SERVER_ERROR", "An error occurred")
      stubGet(testUrl, INTERNAL_SERVER_ERROR, Json.toJson(serviceUnavailableError).toString())
      val result: ConnectorResponse[StateBenefitsPrePopulationResponse] = connector.getPrePopulation(nino, taxYear, mtdItId)
      result.futureValue shouldBe Left(SimpleErrorWrapper(INTERNAL_SERVER_ERROR))
    }
  }
}
