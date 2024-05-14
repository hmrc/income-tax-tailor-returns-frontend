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
import play.api.i18n.Messages
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.tasklist.{TaskList, TaskListItem, TaskListItemStatus, TaskListItemTitle}
import uk.gov.hmrc.govukfrontend.views.viewmodels.tag.Tag

case class AddSectionsViewModel(state: SectionState, taxYear: Int, prefix: String) (implicit messages: Messages) {

  val sections: Seq[TaskListItem] = List(
    TaskListItem(
      title = TaskListItemTitle(HtmlContent(messages(s"$prefix.${AboutYou.toString}"))),
      status = TaskListItemStatus(Some(Tag(HtmlContent(messages(s"${state.aboutYou.toString}"))))),
      href = Some(controllers.aboutyou.routes.UkResidenceStatusController.onPageLoad(NormalMode, taxYear).url)
    ),
    TaskListItem(
      title = TaskListItemTitle(HtmlContent(messages(s"$prefix.${IncomeFromWork.toString}"))),
      status = TaskListItemStatus(Some(Tag(HtmlContent(messages(s"${state.incomeFromWork.toString}"))))),
      href = Some(controllers.workandbenefits.routes.AboutYourWorkController.onPageLoad(NormalMode, taxYear).url)
    ),
    TaskListItem(
      title = TaskListItemTitle(HtmlContent(messages(s"$prefix.${IncomeFromProperty.toString}"))),
      status = TaskListItemStatus(Some(Tag(HtmlContent(messages(s"${state.incomeFromProperty.toString}"))))),
      href = Some(controllers.propertypensionsinvestments.routes.RentalIncomeController.onPageLoad(NormalMode, taxYear).url)
    ),
    TaskListItem(
      title = TaskListItemTitle(HtmlContent(messages(s"$prefix.${Pensions.toString}"))),
      status = TaskListItemStatus(Some(Tag(HtmlContent(messages(s"${state.pensions.toString}"))))),
      href = Some(controllers.pensions.routes.PaymentsIntoPensionsController.onPageLoad(NormalMode, taxYear).url)
    )
  )


  //TO DO - figure out how to get this to work
 // val completedCount: Int = sections.map(_.tag).count(_.isCompleted)
 val completedCount: Int = 4

  val isComplete: Boolean = completedCount >= sections.size

  val continueLink: Call = routes.AddSectionsController.onSubmit(taxYear)

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
