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
import org.scalatest.Assertion

class CisPrePopulationResponseSpec extends SpecBase {

  "hasPrePop" - {
    "should return true when pre-pop is true" in {
      CisPrePopulationResponse(hasCis = true).hasCis mustBe true
    }

    "should return false when pre-pop is false" in {
      CisPrePopulationResponse(hasCis = false).hasCis mustBe false
    }
  }

  "toPageModel" - {
    "when hasCis true" in {
      CisPrePopulationResponse(hasCis = true).toPageModel mustBe true
    }

    "when hasCis false" in {
      CisPrePopulationResponse(hasCis = false).toPageModel mustBe false
    }
  }

  "toMessageString" - {
    val testVals = Seq(
      (true, true),
      (true, false),
      (false, true),
      (false, false)
    )

    def testScenario(isAgent: Boolean, hasCis: Boolean): Assertion =
      CisPrePopulationResponse(
        hasCis = hasCis
      ).toMessageString(isAgent) mustBe ""

    "should work as expected" in {
      testVals.map(testVal => testScenario(testVal._1, testVal._2))
    }
  }

}
