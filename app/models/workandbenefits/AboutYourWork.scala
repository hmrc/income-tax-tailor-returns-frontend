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

package models.workandbenefits

import models.{Enumerable, WithName}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.{CheckboxItem, ExclusiveCheckbox}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.hint.Hint
import viewmodels.govuk.checkbox._

sealed trait AboutYourWork

object AboutYourWork extends Enumerable.Implicits {

  case object Employed extends WithName("employed") with AboutYourWork
  case object SelfEmployed extends WithName("selfEmployed") with AboutYourWork

  case object Divider extends AboutYourWork
  case object No extends WithName("none") with AboutYourWork

  val values: Seq[AboutYourWork] = Seq(
    Employed,
    SelfEmployed,
    Divider,
    No
  )

  def checkboxItems(implicit messages: Messages): Seq[CheckboxItem] =
    values.zipWithIndex.map {
      case (value, index) =>
        value match {
          case Divider => CheckboxItemViewModel(
            fieldId = "value",
            index = index,
            value = value.toString,
            divider = messages(s"site.or")
          )
          case Employed => CheckboxItemViewModel(
            content = Text(messages(s"aboutYourWork.${value.toString}")),
            fieldId = "value",
            index = index,
            value = value.toString
          ).withHint(Hint(content = Text(messages(s"aboutYourWork.employed.hint"))))
          case SelfEmployed => CheckboxItemViewModel(
            content = Text(messages(s"aboutYourWork.${value.toString}")),
            fieldId = "value",
            index = index,
            value = value.toString
          ).withHint(Hint(content = Text(messages(s"aboutYourWork.selfEmployed.hint"))))
          case No => CheckboxItemViewModel(
            content = Text(messages(s"aboutYourWork.${value.toString}")),
            fieldId = "value",
            index = index,
            value = value.toString,
            behaviour = Some(ExclusiveCheckbox)
          )
        }
    }

  def agentCheckboxItems(implicit messages: Messages): Seq[CheckboxItem] =
    values.zipWithIndex.map {
      case (value, index) =>
        value match {
          case Divider => CheckboxItemViewModel(
            fieldId = "value",
            index = index,
            value = value.toString,
            divider = messages(s"site.or")
          )
          case Employed => CheckboxItemViewModel(
            content = Text(messages(s"aboutYourWork.${value.toString}")),
            fieldId = "value",
            index = index,
            value = value.toString
          ).withHint(Hint(content = Text(messages(s"aboutYourWork.employed.agent.hint"))))
          case SelfEmployed => CheckboxItemViewModel(
            content = Text(messages(s"aboutYourWork.${value.toString}")),
            fieldId = "value",
            index = index,
            value = value.toString
          ).withHint(Hint(content = Text(messages(s"aboutYourWork.selfEmployed.agent.hint"))))
          case No => CheckboxItemViewModel(
            content = Text(messages(s"aboutYourWork.${value.toString}")),
            fieldId = "value",
            index = index,
            value = value.toString,
            behaviour = Some(ExclusiveCheckbox)
          )
        }
    }

  implicit val enumerable: Enumerable[AboutYourWork] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
