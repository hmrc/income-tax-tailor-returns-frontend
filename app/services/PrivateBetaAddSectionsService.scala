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
import models.{SectionState, TagStatus, UserAnswers, IncomeFromWorkDependentStates}
import pages.aboutyou.FosterCarerPage
import pages.workandbenefits.{AboutYourWorkPage, JobseekersAllowancePage}

class PrivateBetaAddSectionsService extends AddSectionsService {
  def getState(userAnswers: Option[UserAnswers]): SectionState = {

    userAnswers match {
      case None => SectionState(NotStarted, CannotStartYet, CannotStartYet, CannotStartYet)
      case Some(ua) =>
        val aboutYou: TagStatus = if (ua.get(FosterCarerPage).isDefined) {
          Completed
        } else {
          NotStarted
        }

        val incomeFromWorkStates: IncomeFromWorkDependentStates = IncomeFromWorkDependentStates(
          aboutYou.isCompleted,
          ua.get(AboutYourWorkPage).isDefined,
          ua.get(JobseekersAllowancePage).isDefined
        )

        SectionState(aboutYou, incomeFromWorkStates.getStatus, CannotStartYet, CannotStartYet)
    }
  }
}
