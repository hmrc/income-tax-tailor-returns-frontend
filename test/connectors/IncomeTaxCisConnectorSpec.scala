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

package connectors

import base.SpecBase
import mocks.{MockAppConfig, MockHttpClientV2}
import models.errors.SimpleErrorWrapper
import models.prePopulation.IncomeTaxCisPrePopulationResponse
import play.api.http.Status.{IM_A_TEAPOT, INTERNAL_SERVER_ERROR}
import play.api.test.DefaultAwaitTimeout
import play.api.test.Helpers.await
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, StringContextOps}

class IncomeTaxCisConnectorSpec extends SpecBase
  with MockAppConfig
  with MockHttpClientV2
  with DefaultAwaitTimeout {

  trait Test {
    implicit val hc: HeaderCarrier = HeaderCarrier()

    val nino: String = "AA111111A"
    val mtdItId: String = "111111"
    val testConnector = new IncomeTaxCisConnector(
      config = mockAppConfig,
      httpClient = mockHttpClientV2
    )

    implicit val httpReads: HttpReads[HttpResult[IncomeTaxCisPrePopulationResponse]] =
      testConnector.StandardGetHttpReads

    val dummyResponse: IncomeTaxCisPrePopulationResponse = IncomeTaxCisPrePopulationResponse(
      hasCis = true
    )

    val baseUrl = "http://test-BaseUrl"
    mockIncomeTaxCisBaseUrl(response = baseUrl)
    mockHttpClientV2Get(url"$baseUrl/pre-population/$nino/$taxYear")
  }

  "getPrePopulation" -> {
    "should return a success when a success response is received from income tax cis backend" in new Test {
      mockHttpClientV2SetHeader()
      mockHttpClientV2Execute[HttpResult[IncomeTaxCisPrePopulationResponse]](Right(dummyResponse))

      val result: Either[SimpleErrorWrapper, IncomeTaxCisPrePopulationResponse] =
        await(testConnector.getPrePopulation(nino, taxYear, mtdItId))

      result mustBe a[Right[_, _]]
      result.getOrElse(IncomeTaxCisPrePopulationResponse.empty) mustBe dummyResponse
    }

    "should return an error when a success response is received from income tax cis backend" in new Test {
      mockHttpClientV2SetHeader()
      mockHttpClientV2Execute[HttpResult[IncomeTaxCisPrePopulationResponse]](
        Left(SimpleErrorWrapper(INTERNAL_SERVER_ERROR))
      )

      val result: Either[SimpleErrorWrapper, IncomeTaxCisPrePopulationResponse] =
        await(testConnector.getPrePopulation(nino, taxYear, mtdItId))

      result mustBe a[Left[_, _]]
      result.swap.getOrElse(SimpleErrorWrapper(IM_A_TEAPOT)).status mustBe INTERNAL_SERVER_ERROR
    }

    "should throw an exception when an exception occurs during call to income tax cis backend" in new Test {
      mockHttpClientV2SetHeader()
      mockHttpClientV2ExecuteException[HttpResult[IncomeTaxCisPrePopulationResponse]](
        new RuntimeException()
      )

      def result: Either[SimpleErrorWrapper, IncomeTaxCisPrePopulationResponse] =
        await(testConnector.getPrePopulation(nino, taxYear, mtdItId))

      assertThrows[RuntimeException](result)
    }
  }
}
