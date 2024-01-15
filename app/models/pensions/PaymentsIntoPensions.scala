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

package models.pensions

import models.{Enumerable, WithName}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Hint
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.{CheckboxItem, ExclusiveCheckbox}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import viewmodels.govuk.checkbox._

sealed trait PaymentsIntoPensions

object PaymentsIntoPensions extends Enumerable.Implicits {

  case object UkPensions extends WithName("ukPensions") with PaymentsIntoPensions
  case object NonUkPensions extends WithName("nonUkPensions") with PaymentsIntoPensions

  case object Overseas extends WithName("overseas") with PaymentsIntoPensions

  case object Divider extends PaymentsIntoPensions
  case object No extends WithName("none") with PaymentsIntoPensions

  val values: Seq[PaymentsIntoPensions] = Seq(
    UkPensions,
    NonUkPensions,
    Overseas,
    Divider,
    No
  )

  private def getCheckboxItems(contentPrefix: String, hintMessage: String)(implicit messages: Messages): Seq[CheckboxItem] = {
    values.zipWithIndex.map {
      case (value, index) =>
        value match {
          case UkPensions => CheckboxItemViewModel(
            content = Text(messages(s"paymentsIntoPensions.${value.toString}")),
            fieldId = "value",
            index = index,
            value = value.toString
          ).withHint(Hint(content = Text(messages(s"paymentsIntoPensions.uk.hint"))))
          case Divider => CheckboxItemViewModel(
            fieldId = "value",
            index = index,
            value = value.toString,
            divider = messages(s"site.or")
          )
          case No => CheckboxItemViewModel(
            content = Text(messages(s"paymentsIntoPensions.${value.toString}")),
            fieldId = "value",
            index = index,
            value = value.toString,
            behaviour = Some(ExclusiveCheckbox)
          )
          case _ => CheckboxItemViewModel(
            content = Text(messages(s"paymentsIntoPensions.${value.toString}")),
            fieldId = "value",
            index = index,
            value = value.toString
          )
        }

    }
  }
  def checkboxItems(implicit messages: Messages): Seq[CheckboxItem] =
    getCheckboxItems(contentPrefix="paymentsIntoPensions",hintMessage="paymentsIntoPensions.uk.hint")

  def agentCheckboxItems(implicit messages: Messages): Seq[CheckboxItem] =
    getCheckboxItems(contentPrefix="paymentsIntoPensions.agent",hintMessage="paymentsIntoPensions.agent.uk.hint")

  implicit val enumerable: Enumerable[PaymentsIntoPensions] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
