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

class TagStatusSpec extends AnyFreeSpec with Matchers {

  "TagStatus" - {

    "must contain the correct values" in {
      TagStatus.values mustEqual Set(Completed, NotStarted, CannotStartYet)
    }

  }

  "TagStatus.isCompleted" - {

    "must return true if tag status is 'completed'" in {
      val underTest = TagStatus.Completed
      underTest.isCompleted mustEqual true
    }

    "must return false if tag status is 'notStarted'" in {
      val underTest = TagStatus.NotStarted
      underTest.isCompleted mustEqual false
    }

    "must return false if tag status is 'cannotStartYet'" in {
      val underTest = TagStatus.CannotStartYet
      underTest.isCompleted mustEqual false
    }
  }
}

