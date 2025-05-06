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

package utils

import base.SpecBase
import uk.gov.hmrc.auth.core.{EnrolmentIdentifier, Enrolments, Enrolment => HMRCEnrolment}

class EnrolmentHelperSpec extends SpecBase {

  "getEnrolmentValueOpt" - {
    "should return the expected value when a key exists within the given enrolments" in {
      val testEnrolments: Enrolments = Enrolments(
        Set(HMRCEnrolment("dummyKey", Seq(EnrolmentIdentifier("dummyIdentifier", "value")), "activated"))
      )

      EnrolmentHelper.getEnrolmentValueOpt("dummyKey", "dummyIdentifier", testEnrolments) mustBe Some("value")
    }

    "should return the expected value when multiple keys exist within the given enrolments" in {
      val testEnrolments: Enrolments = Enrolments(
        Set(
          HMRCEnrolment("dummyKey", Seq(EnrolmentIdentifier("dummyIdentifier", "value")), "activated"),
          HMRCEnrolment("dummyKey", Seq(EnrolmentIdentifier("dummyIdentifier2", "value2")), "activated")
        )
      )

      EnrolmentHelper.getEnrolmentValueOpt("dummyKey", "dummyIdentifier", testEnrolments) mustBe Some("value")
    }

    "should return none when a key doesn't exist within the given enrolment" in {
      val testEnrolments: Enrolments = Enrolments(Set())
      EnrolmentHelper.getEnrolmentValueOpt("dummyKey", "dummyIdentifier", testEnrolments) mustBe None
    }
  }
}
