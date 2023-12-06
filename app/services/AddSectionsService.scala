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
import models.{IncomeFromWorkDependentStates, SectionState, TagStatus, UserAnswers}
import pages.aboutyou.{FosterCarerPage, TaxAvoidanceSchemesPage}
import pages.pensions.PaymentsIntoPensionsPage
import pages.propertypensionsinvestments.UkDividendsSharesLoansPage
import pages.workandbenefits.{AboutYourWorkPage, JobseekersAllowancePage}

trait AddSectionsService {
  def deriveState[A](userAnswers: Option[UserAnswers], isPrivateBeta: Boolean): SectionState = {
    userAnswers match {
      case None => SectionState(NotStarted, CannotStartYet, CannotStartYet, NotStarted)
      case Some(ua) =>
        val aboutYou: TagStatus = if ((isPrivateBeta && ua.get(FosterCarerPage).isDefined) || ua.get(TaxAvoidanceSchemesPage).isDefined) {
          Completed
        } else {
          NotStarted
        }

        val incomeFromWorkStates: IncomeFromWorkDependentStates = IncomeFromWorkDependentStates(
          aboutYou.isCompleted,
          ua.get(AboutYourWorkPage).isDefined,
          ua.get(JobseekersAllowancePage).isDefined
        )

        val incomeFromProperty: TagStatus =
          ua.get(UkDividendsSharesLoansPage) match {
            case Some(_) => Completed
            case None => if (incomeFromWorkStates.getStatus.isCompleted) {
              NotStarted
            } else {
              CannotStartYet
            }
          }

        val pensions: TagStatus = if (ua.get(PaymentsIntoPensionsPage).isDefined) {
          Completed
        } else {
          NotStarted
        }

        SectionState(aboutYou, incomeFromWorkStates.getStatus, incomeFromProperty, pensions)
    }
  }

  def getState(userAnswers: Option[UserAnswers]): SectionState

}
