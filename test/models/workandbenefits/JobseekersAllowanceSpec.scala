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

package models.workandbenefits

import generators.ModelGenerators
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsString, Json}

class JobseekersAllowanceSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues with ModelGenerators {

  "JobseekersAllowance" - {

    "must deserialise valid values" in {

      val gen = arbitrary[JobseekersAllowance]

      forAll(gen) {
        jobseekersAllowance =>

          JsString(jobseekersAllowance.toString).validate[JobseekersAllowance].asOpt.value mustEqual jobseekersAllowance
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!JobseekersAllowance.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[JobseekersAllowance] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = arbitrary[JobseekersAllowance]

      forAll(gen) {
        jobseekersAllowance =>

          Json.toJson(jobseekersAllowance) mustEqual JsString(jobseekersAllowance.toString)
      }
    }
  }
}
