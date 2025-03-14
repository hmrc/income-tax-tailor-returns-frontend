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

package services

import base.SpecBase
import mocks.MockStateBenefitsConnector
import models.errors.SimpleErrorWrapper
import models.prePopulation.{EsaJsaPrePopulationResponse, StateBenefitsPrePopulationResponse}
import play.api.http.Status.{IM_A_TEAPOT, INTERNAL_SERVER_ERROR}
import play.api.test.DefaultAwaitTimeout
import play.api.test.Helpers.await
import uk.gov.hmrc.http.HeaderCarrier

class PrePopulationServiceSpec extends SpecBase
  with MockStateBenefitsConnector
  with DefaultAwaitTimeout {

  val nino: String = "AA111111A"
  override val taxYear: Int = 2025
  override val mtdItId = "111111"

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val testService = new PrePopulationService(
    stateBenefitsConnector = mockStateBenefitsConnector,
    cisConnector =  mockSIncomeTaxCisConnector
  )

  val dummyResponse: StateBenefitsPrePopulationResponse = StateBenefitsPrePopulationResponse(
    hasEsaPrePop = true,
    hasJsaPrePop = true,
    hasPensionsPrePop = false,
    hasPensionLumpSumsPrePop = false
  )
  val dummyEsaJsaResponse: EsaJsaPrePopulationResponse = dummyResponse.toEsaJsaModel

  val emptyPrePopResponse: EsaJsaPrePopulationResponse = EsaJsaPrePopulationResponse(
    hasEsaPrePop = false,
    hasJsaPrePop = false
  )

  "getEsaJsa" -> {
    "should return a success response when connector returns success response" in {
      mockGetPrePopulation(nino, taxYear, mtdItId, Right(dummyResponse))
      val result = await(testService.getEsaJsa(nino, taxYear, mtdItId))
      result mustBe a[Right[_, _]]
      result.getOrElse(emptyPrePopResponse) mustBe dummyEsaJsaResponse
    }

    "should return an error response when connector returns error response" in {
      mockGetPrePopulation(nino, taxYear, mtdItId, Left(SimpleErrorWrapper(INTERNAL_SERVER_ERROR)))
      val result = await(testService.getEsaJsa(nino, taxYear, mtdItId))
      result mustBe a[Left[_, _]]
      result.swap.getOrElse(SimpleErrorWrapper(IM_A_TEAPOT)).status mustBe INTERNAL_SERVER_ERROR
    }

    "should throw an exception when an exception is thrown by the connector" in {
      mockGetPrePopulationException(nino, taxYear, mtdItId, new RuntimeException())
      assertThrows[RuntimeException](
        await(testService.getEsaJsa(nino, taxYear, mtdItId))
      )
    }
  }
}

