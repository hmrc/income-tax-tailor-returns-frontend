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

package models

import models.TagStatus.{CannotStartYet, Completed, NotStarted}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class IncomeFromWorkDependentStatesSpec extends AnyFreeSpec with Matchers {

  "IncomeFromWorkDependentStates" - {

    "must contain all true values" in {
      val result = IncomeFromWorkDependentStates(aboutYouSection = true, aboutYourWorkPage = true, jsaEsaPage = true)
      result mustEqual IncomeFromWorkDependentStates(aboutYouSection = true, aboutYourWorkPage = true, jsaEsaPage = true)
    }

    "must contain all false values" in {
      val result = IncomeFromWorkDependentStates(aboutYouSection = false, aboutYourWorkPage = false, jsaEsaPage = false)
      result mustEqual IncomeFromWorkDependentStates(aboutYouSection = false, aboutYourWorkPage = false, jsaEsaPage = false)
    }

  }

  ".getState" - {

    "must return Completed when all values are true" in {
      val result = IncomeFromWorkDependentStates(aboutYouSection = true, aboutYourWorkPage = true, jsaEsaPage = true).getStatus
      result mustEqual Completed
    }

    "must return NotStarted when aboutYourWork and jsaEsa are false" in {
      val result = IncomeFromWorkDependentStates(aboutYouSection = true, aboutYourWorkPage = false, jsaEsaPage = false).getStatus
      result mustEqual NotStarted
    }

    "must return NotStarted when only aboutYourWork is false" in {
      val result = IncomeFromWorkDependentStates(aboutYouSection = true, aboutYourWorkPage = false, jsaEsaPage = true).getStatus
      result mustEqual NotStarted
    }

    "must return CannotStartYet when all values are false" in {
      val result = IncomeFromWorkDependentStates(aboutYouSection = false, aboutYourWorkPage = false, jsaEsaPage = false).getStatus
      result mustEqual CannotStartYet
    }

  }
}

