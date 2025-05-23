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

package models.prePopulation

import base.SpecBase
import play.api.libs.json.{JsError, JsSuccess, Json}

class StateBenefitsPrePopulationResponseSpec extends SpecBase {

  "reads" - {
    "should return a JsSuccess when provided with valid JSON" in {
      val json: String =
        """
          |{
          |   "hasEsaPrePop": true,
          |   "hasJsaPrePop": true,
          |   "hasPensionsPrePop": false,
          |   "hasPensionLumpSumsPrePop": false
          |}
        """.stripMargin

      val result = Json.parse(json).validate[StateBenefitsPrePopulationResponse]
      result mustBe a[JsSuccess[_]]
      result.get mustBe StateBenefitsPrePopulationResponse(
        hasEsaPrePop = true,
        hasJsaPrePop = true,
        hasPensionsPrePop = false,
        hasPensionLumpSumsPrePop = false
      )
    }

    "should return a JsError when provided with invalid JSON" in {
      val json: String =
        """
          |{
          |   "beep": "boop"
          |}
        """.stripMargin

      val result = Json.parse(json).validate[StateBenefitsPrePopulationResponse]
      result mustBe a[JsError]
    }
  }

  "toEsaJsaModel" - {
    "should convert successfully" in {
      StateBenefitsPrePopulationResponse.empty.toEsaJsaModel mustBe EsaJsaPrePopulationResponse(
        hasEsaPrePop = false,
        hasJsaPrePop = false
      )
    }
  }

}
