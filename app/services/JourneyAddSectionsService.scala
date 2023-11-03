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

package services

import models.TagStatus.{CannotStartYet, Completed, NotStarted}
import models.{SectionState, TagStatus, UserAnswers}
import pages.aboutyou.TaxAvoidanceSchemesPage

class JourneyAddSectionsService extends AddSectionsService {
  def getState(userAnswers: Option[UserAnswers]): SectionState = {

    userAnswers match {
      case None => SectionState(NotStarted, CannotStartYet, CannotStartYet, CannotStartYet)
      case Some(ua) =>
        val aboutYou: TagStatus = if (ua.get(TaxAvoidanceSchemesPage).isDefined) {
          Completed
        } else {
          NotStarted
        }

        val incomeFromWork: TagStatus = if (aboutYou.isCompleted) {
          NotStarted
        } else {
          CannotStartYet
        }

        SectionState(aboutYou, incomeFromWork, CannotStartYet, CannotStartYet)
    }
  }
}
