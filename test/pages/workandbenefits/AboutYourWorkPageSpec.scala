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

package pages.workandbenefits

import models.UserAnswers
import models.workandbenefits.AboutYourWork
import models.workandbenefits.AboutYourWork.Employed
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours

class AboutYourWorkPageSpec extends PageBehaviours {

  "AboutYourWorkPage" must {

    beRetrievable[Set[AboutYourWork]](AboutYourWorkPage)

    beSettable[Set[AboutYourWork]](AboutYourWorkPage)

  }

  "remove data relating ConstructionIndustrySchemePage in the income from work and pensions section" in {

    forAll(arbitrary[UserAnswers]) {
      initial =>

        val answers = initial.set(ConstructionIndustrySchemePage, true).success.value

        val result = answers.set(AboutYourWorkPage, Set[AboutYourWork](Employed)).success.value

        result.get(ConstructionIndustrySchemePage) mustNot be (defined)
    }

  }

}
