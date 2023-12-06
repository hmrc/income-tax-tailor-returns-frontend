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
import models.workandbenefits.AboutYourWork
import models.workandbenefits.AboutYourWork.{Employed, SelfEmployed}
import org.scalacheck.Arbitrary.arbitrary
import pages.aboutyou.FosterCarerPage
import pages.behaviours.PageBehaviours
import pages.workandbenefits.{AboutYourWorkPage, AboutYourWorkRadioPage, ConstructionIndustrySchemePage}

class FosterCarerPageSpec extends PageBehaviours {

  "FosterCarerPage" must {

    beRetrievable[Boolean](FosterCarerPage)

    beSettable[Boolean](FosterCarerPage)

  }

  "remove data relating to both versions of AboutYourWorkPage and the ConstructionIndustrySchemePage in the income from work and pensions section" in {

    forAll(arbitrary[UserAnswers]) {
      initial =>

        val answers = initial.set(AboutYourWorkPage, Set[AboutYourWork](Employed, SelfEmployed))
          .flatMap(_.set(AboutYourWorkRadioPage, true)).success.value

        val result = answers.set(FosterCarerPage, false).success.value

        result.get(AboutYourWorkPage) mustNot be (defined)
        result.get(AboutYourWorkRadioPage) mustNot be (defined)
        result.get(ConstructionIndustrySchemePage) mustNot be (defined)
    }

  }

}
