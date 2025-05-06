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
import models.workandbenefits.JobseekersAllowance.{Esa, Jsa}
import org.scalatest.Assertion

class EsaJsaPrePopulationResponseSpec extends SpecBase {

  "hasPrePop" - {
    "should return true when either ESA or JSA has pre-pop" in {
      EsaJsaPrePopulationResponse(hasEsaPrePop = true, hasJsaPrePop = false).hasPrePop mustBe true
    }

    "should return false when neither ESA or JSA has pre-pop" in {
      EsaJsaPrePopulationResponse(hasEsaPrePop = false, hasJsaPrePop = false).hasPrePop mustBe false
    }
  }

  "toPageModel" - {
    "when ESA exists should return ESA model" in {
      EsaJsaPrePopulationResponse(hasEsaPrePop = true, hasJsaPrePop = false).toPageModel mustBe Set(Esa)
    }

    "when JSA exists should return JSA model" in {
      EsaJsaPrePopulationResponse(hasEsaPrePop = false, hasJsaPrePop = true).toPageModel mustBe Set(Jsa)
    }

    "when both ESA & JSA exists should return ESA & JSA models" in {
      EsaJsaPrePopulationResponse(hasEsaPrePop = true, hasJsaPrePop = true).toPageModel mustBe Set(Esa, Jsa)
    }

    "when neither ESA nor JSA exists should return empty set" in {
      EsaJsaPrePopulationResponse(hasEsaPrePop = false, hasJsaPrePop = false).toPageModel mustBe Set()
    }
  }

  "toMessageString" - {
    val testVals = Seq(
      (true, true, true, Some("agent.both")),
      (true, true, false, Some("agent.esa")),
      (true, false, true, Some("agent.jsa")),
      (true, false, false, None),
      (false, true, true, Some("both")),
      (false, true, false, Some("esa")),
      (false, false, true, Some("jsa")),
      (false, false, false, None)
    )

    def testScenario(isAgent: Boolean, hasEsa: Boolean, hasJsa: Boolean, resultString: Option[String]): Assertion =
      EsaJsaPrePopulationResponse(
        hasEsaPrePop = hasEsa,
        hasJsaPrePop = hasJsa
      ).toMessageString(isAgent) mustBe resultString.fold("")(str => s"jobseekersAllowance.insetText.$str")

    "should work as expected" in {
      testVals.map(testVal => testScenario(testVal._1, testVal._2, testVal._3, testVal._4))
    }
  }

}
