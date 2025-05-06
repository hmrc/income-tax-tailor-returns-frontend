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
import models.propertypensionsinvestments.RentalIncome
import org.scalatest.Assertion

class PropertyPrePopulationResponseSpec extends SpecBase {

  "hasPrePop" - {
    "should return true when UkProperty and ForeignProperty pre-pop is true" in {
      PropertyPrePopulationResponse(
        hasUkPropertyPrePop = true,
        hasForeignPropertyPrePop = true
      ).hasPrePop mustBe true
    }

    "should return true when only UkProperty pre-pop is true" in {
      PropertyPrePopulationResponse(
        hasUkPropertyPrePop = true,
        hasForeignPropertyPrePop = false
      ).hasPrePop mustBe true
    }

    "should return true when only ForeignProperty pre-pop is true" in {
      PropertyPrePopulationResponse(
        hasUkPropertyPrePop = false,
        hasForeignPropertyPrePop = true
      ).hasPrePop mustBe true
    }

    "should return false when all pre-pop is false" in {
      PropertyPrePopulationResponse(
        hasUkPropertyPrePop = false,
        hasForeignPropertyPrePop = false
      ).hasPrePop mustBe false
    }
  }

  "toPageModel" - {
    "when UkProperty and ForeignProperty pre-pop is true" in {
      PropertyPrePopulationResponse(
        hasUkPropertyPrePop = true,
        hasForeignPropertyPrePop = true
      ).toPageModel mustBe Set(RentalIncome.Uk, RentalIncome.NonUk)
    }

    "when UkProperty pre-pop is true" in {
      PropertyPrePopulationResponse(
        hasUkPropertyPrePop = true,
        hasForeignPropertyPrePop = false
      ).toPageModel mustBe Set(RentalIncome.Uk)
    }

    "when ForeignProperty pre-pop is true" in {
      PropertyPrePopulationResponse(
        hasUkPropertyPrePop = false,
        hasForeignPropertyPrePop = true
      ).toPageModel mustBe Set(RentalIncome.NonUk)
    }

    "when all pre-pop is false" in {
      PropertyPrePopulationResponse(
        hasUkPropertyPrePop = false,
        hasForeignPropertyPrePop = false
      ).toPageModel mustBe Set()
    }
  }

  "toMessageString" - {
    val testVals = Seq(
      (true, true, true),
      (true, true, false),
      (true, false, true),
      (true, false, false),
      (false, true, true),
      (false, true, false),
      (false, false, true),
      (false, false, false)
    )

    def testScenario(isAgent: Boolean, hasUk: Boolean, hasForeign: Boolean): Assertion =
      PropertyPrePopulationResponse(
        hasUkPropertyPrePop = hasUk,
        hasForeignPropertyPrePop = hasForeign
      ).toMessageString(isAgent) mustBe ""

    "should work as expected" in {
      testVals.map(testVal => testScenario(testVal._1, testVal._2, testVal._3))
    }
  }

}
