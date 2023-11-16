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

package viewmodels

import models.SectionState
import models.TagStatus._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import play.twirl.api.Html

class TaxReturnNotReadyViewModelSpec extends AnyFreeSpec with Matchers {

  private implicit val messages: Messages = stubMessages()

  private val state: SectionState = SectionState(NotStarted, CannotStartYet, CannotStartYet, CannotStartYet)

  private val prefix: String = "taxReturnNotReady"
  private val aboutYouKey: String = s"$prefix.aboutYou"
  private val incomeFromWorkKey: String = s"$prefix.incomeFromWork"
  private val incomeFromPropertyKey: String = s"$prefix.incomeFromProperty"
  private val pensionsKey: String = s"$prefix.pensions"


  ".getBulletItems" - {

    "must return a collection of Html elements with keys for all sections when no sections are complete" in {

      val result = TaxReturnNotReadyViewModel(state, prefix).getBulletItems()

      val expected = Seq[Html](
        Html(messages(aboutYouKey)),
        Html(messages(incomeFromWorkKey)),
        Html(messages(incomeFromPropertyKey)),
        Html(messages(pensionsKey))
      )

      result mustBe expected
    }

    "must return a collection of Html elements with keys for only incomeFromWork, incomeFromProperty and pensions when aboutYou is complete" in {

      val result = TaxReturnNotReadyViewModel(state.copy(aboutYou = Completed), prefix).getBulletItems()

      val expected = Seq[Html](
        Html(messages(incomeFromWorkKey)),
        Html(messages(incomeFromPropertyKey)),
        Html(messages(pensionsKey))
      )

      result mustBe expected
    }

    "must return a collection of Html elements with keys for only incomeFromProperty and pensions when aboutYou and incomeFromWork are complete" in {

      val result = TaxReturnNotReadyViewModel(state.copy(aboutYou = Completed, incomeFromWork = Completed), prefix).getBulletItems()

      val expected = Seq[Html](
        Html(messages(incomeFromPropertyKey)),
        Html(messages(pensionsKey))
      )

      result mustBe expected
    }

    "must return a collection of Html elements with the key for only pensions when aboutYou, incomeFromWork and incomeFromProperty are complete" in {

      val result = TaxReturnNotReadyViewModel(
        state.copy(aboutYou = Completed, incomeFromWork = Completed, incomeFromProperty = Completed),
        prefix
      ).getBulletItems()

      val expected = Seq[Html](
        Html(messages(pensionsKey))
      )

      result mustBe expected
    }

  }

}
