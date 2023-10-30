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

package models.aboutyou

import models.{Enumerable, WithName}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.CheckboxItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{Empty, Text}
import viewmodels.govuk.checkbox._

sealed trait TaxAvoidanceSchemes

object TaxAvoidanceSchemes extends Enumerable.Implicits {

  case object TaxAvoidance extends WithName("taxAvoidance") with TaxAvoidanceSchemes

  case object DisguisedRemuneration extends WithName("disguisedRemuneration") with TaxAvoidanceSchemes

  case object NoAvoidance extends WithName("noAvoidance") with TaxAvoidanceSchemes

  case object Divider extends TaxAvoidanceSchemes

  val values: Seq[TaxAvoidanceSchemes] = Seq(
    TaxAvoidance,
    DisguisedRemuneration,
    Divider,
    NoAvoidance
  )

  def checkboxItems(implicit messages: Messages): Seq[CheckboxItem] =
    values.zipWithIndex.map {
      case (value, index) =>
        value match {
          case Divider =>
            CheckboxItemViewModel(
              content = Empty,
              fieldId = "value",
              index = index,
              value = value.toString,
              divider = Some("or")
            )
          case _ =>
            CheckboxItemViewModel(
              content = Text(messages(s"taxAvoidanceSchemes.${value.toString}")),
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
          case Divider =>
            CheckboxItemViewModel(
              content = Empty,
              fieldId = "value",
              index = index,
              value = value.toString,
              divider = Some("or")
            )
          case _ =>
            CheckboxItemViewModel(
              content = Text(messages(s"taxAvoidanceSchemes.agent.${value.toString}")),
              fieldId = "value",
              index = index,
              value = value.toString
            )
        }
    }

  implicit val enumerable: Enumerable[TaxAvoidanceSchemes] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
