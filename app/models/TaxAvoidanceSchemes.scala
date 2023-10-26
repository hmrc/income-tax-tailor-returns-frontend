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

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.CheckboxItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import viewmodels.govuk.checkbox._

sealed trait TaxAvoidanceSchemes

object TaxAvoidanceSchemes extends Enumerable.Implicits {

  case object Option1 extends WithName("option1") with TaxAvoidanceSchemes
  case object Option2 extends WithName("option2") with TaxAvoidanceSchemes

  val values: Seq[TaxAvoidanceSchemes] = Seq(
    Option1,
    Option2
  )

  def checkboxItems(implicit messages: Messages): Seq[CheckboxItem] =
    values.zipWithIndex.map {
      case (value, index) =>
        CheckboxItemViewModel(
          content = Text(messages(s"taxAvoidanceSchemes.${value.toString}")),
          fieldId = "value",
          index   = index,
          value   = value.toString
        )
    }

  def agentCheckboxItems(implicit messages: Messages): Seq[CheckboxItem] =
    values.zipWithIndex.map {
      case (value, index) =>
        CheckboxItemViewModel(
          content = Text(messages(s"taxAvoidanceSchemes.agent.${value.toString}") ),
          fieldId = "value",
          index = index,
          value = value.toString
        )
    }

  implicit val enumerable: Enumerable[TaxAvoidanceSchemes] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
