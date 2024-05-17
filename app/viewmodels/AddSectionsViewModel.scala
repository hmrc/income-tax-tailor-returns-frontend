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
import models.{NormalMode, SectionState, TagStatus}
import play.api.i18n.Messages
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.tasklist.{TaskListItem, TaskListItemStatus, TaskListItemTitle}
import uk.gov.hmrc.govukfrontend.views.viewmodels.tag.Tag

case class AddSectionsViewModel(state: SectionState, taxYear: Int, prefix: String) (implicit messages: Messages) {

  val sections: Seq[TaskListItem] = List(
    TaskListItem(
      title = TaskListItemTitle(HtmlContent(messages(s"$prefix.${AboutYou.toString}"))),
      status = TaskListItemStatus(Some(Tag(
        HtmlContent(messages(s"addSections.status.${state.aboutYou.toString}")), classes = statusTagColour(state.aboutYou)))),
      href = notStartedHref(controllers.aboutyou.routes.UkResidenceStatusController.onPageLoad(NormalMode, taxYear).url, state.aboutYou)
    ),
    TaskListItem(
      title = TaskListItemTitle(HtmlContent(messages(s"$prefix.${IncomeFromWork.toString}"))),
      status = TaskListItemStatus(Some(Tag(
        HtmlContent(messages(s"addSections.status.${state.incomeFromWork.toString}")), classes = statusTagColour(state.incomeFromWork)))),
      href = notStartedHref(controllers.workandbenefits.routes.AboutYourWorkController.onPageLoad(NormalMode, taxYear).url, state.incomeFromWork)
    ),
    TaskListItem(
      title = TaskListItemTitle(HtmlContent(messages(s"$prefix.${IncomeFromProperty.toString}"))),
      status = TaskListItemStatus(Some(Tag(
        HtmlContent(messages(s"addSections.status.${state.incomeFromProperty.toString}")), classes = statusTagColour(state.incomeFromProperty)))),
      href = notStartedHref(controllers.propertypensionsinvestments.routes.RentalIncomeController.onPageLoad(NormalMode, taxYear).url, state.incomeFromProperty)
    ),
    TaskListItem(
      title = TaskListItemTitle(HtmlContent(messages(s"$prefix.${Pensions.toString}"))),
      status = TaskListItemStatus(Some(Tag(
        HtmlContent(messages(s"addSections.status.${state.pensions.toString}")), classes = statusTagColour(state.pensions)
      ))),
      href = notStartedHref(controllers.pensions.routes.PaymentsIntoPensionsController.onPageLoad(NormalMode, taxYear).url, state.pensions)
    )
  )

  private def notStartedHref(hrefString: String, tagStatus: TagStatus) : Option[String] = {
    tagStatus match{
      case TagStatus.CannotStartYet => None
      case _ => Some(hrefString)
    }
  }
  private def statusTagColour(tagStatus: TagStatus) : String = {
    tagStatus match {
      case TagStatus.Completed => ""
      case TagStatus.NotStarted => "govuk-tag--grey"
      case TagStatus.CannotStartYet => "govuk-tag--grey"
    }
  }

  private val states: List[TagStatus] = List(
    state.aboutYou,
    state.incomeFromWork,
    state.incomeFromProperty,
    state.pensions
  )

  val completedCount: Int = states.map(_.isCompleted).count(b => b)

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
