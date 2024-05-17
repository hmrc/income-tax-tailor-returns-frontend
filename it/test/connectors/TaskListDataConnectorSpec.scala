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

import com.github.tomakehurst.wiremock.client.WireMock._
import generators.ModelGenerators
import models.UserAnswers
import models.tasklist.TaskListData
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{EitherValues, OptionValues}
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import test.connectors.WireMockHelper
import uk.gov.hmrc.http.HeaderCarrier

import java.time.Instant

class TaskListDataConnectorSpec
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

  private val testUrl = "/income-tax-tailor-return/task-list/data"
  private val keepAliveUrl = "/income-tax-tailor-return/task-list/keep-alive"
  private lazy val connector = app.injector.instanceOf[TaskListDataConnector]

  private val answers = TaskListData(mtditId, taxYear, lastUpdated = Instant.ofEpochSecond(1))

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
}
