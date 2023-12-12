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

import controllers.routes
import models.SectionNames.{AboutYou, IncomeFromProperty, IncomeFromWork, Pensions}
import models.{NormalMode, SectionState}

case class AddSectionsViewModel(state: SectionState, taxYear: Int, prefix: String) {

  val sections: List[Task] = List(
    Task(Link(AboutYou.toString, controllers.aboutyou.routes.UkResidenceStatusController.onPageLoad(NormalMode, taxYear).url), state.aboutYou, prefix),
    Task(Link(
      IncomeFromWork.toString, controllers.workandbenefits.routes.AboutYourWorkController.onPageLoad(NormalMode, taxYear).url), state.incomeFromWork, prefix),
    Task(Link(
      IncomeFromProperty.toString, controllers.propertypensionsinvestments.routes.RentalIncomeController.onPageLoad(NormalMode, taxYear).url), state.incomeFromProperty, prefix),
    Task(Link(Pensions.toString, controllers.pensions.routes.PaymentsIntoPensionsController.onPageLoad(NormalMode, taxYear).url), state.pensions, prefix)
  )

  val completedCount: Int = sections.map(_.tag).count(_.isCompleted)

  private val isComplete: Boolean = completedCount >= sections.size

  val continueLink: String = if (isComplete) {
    routes.TaskListController.onPageLoad(taxYear).url
  } else {
    routes.TaxReturnNotReadyController.onPageLoad(taxYear).url
  }

  val buttonStyle: String = if (isComplete) {
    ""
  } else {
    "govuk-button--secondary"
  }

  val statusText: String = if (isComplete) {
    "addSections.completed"
  } else {
    "addSections.incomplete"
  }

}
