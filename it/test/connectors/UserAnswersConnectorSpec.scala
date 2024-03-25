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

package test.connectors

import com.github.tomakehurst.wiremock.client.WireMock._
import connectors.UserAnswersConnector
import generators.ModelGenerators
import models.UserAnswers
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{EitherValues, OptionValues}
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier

import java.time.Instant

class UserAnswersConnectorSpec
  extends AnyFreeSpec
    with WireMockHelper
    with ScalaFutures
    with Matchers
    with IntegrationPatience
    with EitherValues
    with OptionValues
    with MockitoSugar
    with ModelGenerators {

  implicit private lazy val hc: HeaderCarrier = HeaderCarrier()
  private val taxYear: Int = 2024
  private val mtditId: String = "mtdItId"

  private lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(
        "microservice.services.income-tax-tailor-return.port" -> server.port,
      )
      .build()

  private val testUrl = "/income-tax-tailor-return/data"
  private val keepAliveUrl = "/income-tax-tailor-return/keep-alive"
  private lazy val connector = app.injector.instanceOf[UserAnswersConnector]

  private val answers = UserAnswers(mtditId, taxYear, lastUpdated = Instant.ofEpochSecond(1))

  ".get" - {

    "must return user answers when the server returns them" in {

      server.stubFor(
        get(urlEqualTo(s"$testUrl/$taxYear"))
          .willReturn(ok(Json.toJson(answers).toString))
      )

      val result = connector.get(mtditId, taxYear).futureValue

      result.value mustEqual answers
    }

    "must return None when the server returns NOT_FOUND" in {

      server.stubFor(
        get(urlEqualTo(s"$testUrl/$taxYear"))
          .willReturn(notFound())
      )

      val result = connector.get(mtditId, taxYear).futureValue

      result must not be defined
    }

    "must return a failed future when the server returns an error" in {

      server.stubFor(
        get(urlEqualTo(s"$testUrl/$taxYear"))
          .willReturn(serverError())
      )

      connector.get(mtditId, taxYear).failed.futureValue
    }
  }

  ".set" - {

    "must post user answers to the server" in {

      server.stubFor(
        post(urlEqualTo(testUrl))
          .withRequestBody(equalTo(Json.toJson(answers).toString))
          .willReturn(noContent())
      )

      connector.set(answers).futureValue
    }

    "must return a failed future when the server returns an unexpected response code" in {

      server.stubFor(
        post(urlEqualTo(testUrl))
          .withRequestBody(equalTo(Json.toJson(answers).toString))
          .willReturn(ok())
      )

      connector.set(answers).failed.futureValue
    }
  }

  ".keepAlive" - {

    "must post to the server" in {

      server.stubFor(
        post(urlEqualTo(s"$keepAliveUrl/$taxYear"))
          .willReturn(noContent())
      )

      connector.keepAlive(mtditId, taxYear).futureValue
    }

    "must return a failed future when the server returns an unexpected response code" in {

      server.stubFor(
        post(urlEqualTo(s"$keepAliveUrl/$taxYear"))
          .willReturn(ok())
      )

      connector.keepAlive(mtditId, taxYear).failed.futureValue
    }
  }

  ".clear" - {

    "must send a delete request to the server" in {

      server.stubFor(
        delete(urlEqualTo(s"$testUrl/$taxYear"))
          .willReturn(noContent())
      )

      connector.clear(mtditId, taxYear).futureValue
    }

    "must return a failed future when the server returns an unexpected response code" in {

      server.stubFor(
        delete(urlEqualTo(s"$testUrl/$taxYear"))
          .willReturn(ok())
      )

      connector.clear(mtditId, taxYear).failed.futureValue
    }
  }

}
