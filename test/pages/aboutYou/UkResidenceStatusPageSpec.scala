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

package pages.aboutYou

import models.UserAnswers
import models.aboutyou.{UkResidenceStatus, YourResidenceStatus}
import org.scalacheck.Arbitrary.arbitrary
import pages.aboutyou.{UkResidenceStatusPage, YourResidenceStatusPage}
import pages.behaviours.PageBehaviours

class UkResidenceStatusPageSpec extends PageBehaviours {

  "UkResidenceStatusPage" must {

    beRetrievable[UkResidenceStatus](UkResidenceStatusPage)

    beSettable[UkResidenceStatus](UkResidenceStatusPage)

  }

  "remove relevant data when UkResidenceStatus is set to Uk" in {

    forAll(arbitrary[UserAnswers]) {
      initial =>

        val answers = initial.set(YourResidenceStatusPage, YourResidenceStatus.NonResident).success.value

        val result = answers.set(UkResidenceStatusPage, UkResidenceStatus.Uk).success.value

        result.get(YourResidenceStatusPage) mustNot be (defined)
    }

  }

  "remove relevant data when UkResidenceStatus is set to Domiciled" in {

    forAll(arbitrary[UserAnswers]) {
      initial =>

        val answers = initial.set(YourResidenceStatusPage, YourResidenceStatus.NonResident).success.value

        val result = answers.set(UkResidenceStatusPage, UkResidenceStatus.Domiciled).success.value

        result.get(YourResidenceStatusPage) mustNot be(defined)
    }

  }

}
