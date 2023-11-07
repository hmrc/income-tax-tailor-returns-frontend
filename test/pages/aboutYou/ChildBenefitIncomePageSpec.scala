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
import models.aboutyou.HighIncomeChildBenefitCharge
import org.scalacheck.Arbitrary.arbitrary
import pages.aboutyou.{ChildBenefitIncomePage, HighIncomeChildBenefitChargePage}
import pages.behaviours.PageBehaviours

class ChildBenefitIncomePageSpec extends PageBehaviours {

  "ChildBenefitIncomePage" must {

    beRetrievable[Boolean](ChildBenefitIncomePage)

    beSettable[Boolean](ChildBenefitIncomePage)

  }

  "remove relevant data when ChildBenefitIncome is set to false" in {

    forAll(arbitrary[UserAnswers]) {
      initial =>

        val answers = initial.set(HighIncomeChildBenefitChargePage, HighIncomeChildBenefitCharge.SelfIncome).success.value

        val result = answers.set(ChildBenefitIncomePage, false).success.value

        result.get(HighIncomeChildBenefitChargePage) mustNot be (defined)
    }

  }

}
