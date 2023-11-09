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

package models.propertypensionsinvestments

import models.{Enumerable, WithName}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.{CheckboxItem, ExclusiveCheckbox}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import viewmodels.govuk.checkbox._

sealed trait UkInterest

object UkInterest extends Enumerable.Implicits {

  case object Option1 extends WithName("option1") with UkInterest
  case object Option2 extends WithName("option2") with UkInterest
  case object ExclusiveOption extends WithName("exclusive") with UkInterest

  val values: Seq[UkInterest] = Seq(
    Option1,
    Option2,
    ExclusiveOption
  )

  def checkboxItems(implicit messages: Messages): Seq[CheckboxItem] =
    values.zipWithIndex.map {
      case (value, index) =>
        value match {
          case ExclusiveOption => CheckboxItemViewModel(
            content = Text(messages(s"ukInterest.${value.toString}")),
            fieldId = "value",
            index = index,
            value = value.toString,
            behaviour = Some(ExclusiveCheckbox)
          )
          case _ => CheckboxItemViewModel(
            content = Text (messages(s"ukInterest.${value.toString}")),
            fieldId = "value",
            index = index,
            value = value.toString
          )
        }
    }

  def agentCheckboxItems(implicit messages: Messages): Seq[CheckboxItem] =
    values.zipWithIndex.map {
      case (value, index) =>
        value match {
          case ExclusiveOption => CheckboxItemViewModel(
            content = Text(messages(s"ukInterest.agent.${value.toString}")),
            fieldId = "value",
            index = index,
            value = value.toString,
            behaviour = Some(ExclusiveCheckbox)
          )
          case _ => CheckboxItemViewModel (
            content = Text (messages(s"ukInterest.agent.${value.toString}")),
            fieldId = "value",
            index = index,
            value = value.toString
          )
        }
    }

  implicit val enumerable: Enumerable[UkInterest] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
