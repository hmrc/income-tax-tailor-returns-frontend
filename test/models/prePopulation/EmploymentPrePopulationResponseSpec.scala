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

class EmploymentPrePopulationResponseSpec extends SpecBase {

  "hasPrePop" - {
    "should return true when pre-pop is true" in {
      EmploymentPrePopulationResponse(hasEmployment = true).hasPrePop mustBe true
    }

    "should return false when pre-pop is false" in {
      EmploymentPrePopulationResponse(hasEmployment = false).hasPrePop mustBe false
    }
  }

  "toPrePopRadioModel" - {
    "when hasEmploymentPrePop is true" in {
      EmploymentPrePopulationResponse(hasEmployment = true).toPrePopRadioModel mustBe EmploymentPrePopulationResponse(true)
    }

    "when hasEmploymentPrePop is false" in {
      EmploymentPrePopulationResponse(hasEmployment = false).toPrePopRadioModel mustBe EmploymentPrePopulationResponse(false)
    }
  }

  "EmploymentRadioPrePop empty instance" - {
    "should have hasEmploymentPrePop as false" in {
      val emptyRadio = EmploymentPrePopulationResponse.EmploymentRadioPrePop.empty
      emptyRadio.hasPrePop mustBe false
      emptyRadio.toPageModel mustBe false
      emptyRadio.toMessageString(isAgent = true) mustBe ""
      emptyRadio.toMessageString(isAgent = false) mustBe ""
    }
  }

  "toMessageString" - {
    val testVals = Seq(
      (true, true, None),
      (true, false, None),
      (false, true, None),
      (false, false, None)
    )

    def testScenario(isAgent: Boolean, hasEmploymentPrePop: Boolean, expectedResult: Option[String]): Assertion = {
      val radioPrePop = new EmploymentPrePopulationResponse.EmploymentRadioPrePop(hasEmploymentPrePop)
      radioPrePop.toMessageString(isAgent) mustBe expectedResult.getOrElse("")
    }

    "should work as expected" in {
      testVals.map(testVal => testScenario(testVal._1, testVal._2, testVal._3))
    }
  }

}
