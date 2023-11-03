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

import models.{HighIncomeChildBenefitCharge, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import pages.aboutyou.{ChildBenefitIncomePage, ChildBenefitPage, HighIncomeChildBenefitChargePage, UkResidenceStatusPage, YourResidenceStatusPage}
import pages.behaviours.PageBehaviours

class ChildBenefitPageSpec extends PageBehaviours {

  "ChildBenefitPage" must {

    beRetrievable[Boolean](ChildBenefitPage)

    beSettable[Boolean](ChildBenefitPage)

  }

  "remove relevant data when ChildBenefit is set to false" in {

    forAll(arbitrary[UserAnswers]) {
      initial =>

        val answers = initial.set(ChildBenefitIncomePage, true)
          .flatMap(_.set(HighIncomeChildBenefitChargePage, HighIncomeChildBenefitCharge.SelfIncome)).success.value

        val result = answers.set(ChildBenefitPage, false).success.value

        result.get(ChildBenefitIncomePage) mustNot be (defined)
        result.get(HighIncomeChildBenefitChargePage) mustNot be (defined)
    }

  }

}
