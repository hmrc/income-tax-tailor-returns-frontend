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

  case object FromUkBanks extends WithName("fromUkBanks") with UkInterest

  case object FromUkTrustFunds extends WithName("fromUkTrustFunds") with UkInterest

  case object FromGiltEdged extends WithName("fromGiltEdged") with UkInterest

  case object Divider extends UkInterest

  case object NoInterest extends WithName("noInterest") with UkInterest

  val values: Seq[UkInterest] = Seq(
    FromUkBanks,
    FromUkTrustFunds,
    FromGiltEdged,
    Divider,
    NoInterest
  )

  private def getCheckboxItems(contentPrefix: String)(implicit messages: Messages): Seq[CheckboxItem] = {
    values.zipWithIndex.map {
      case (value, index) =>
        value match {
          case Divider => CheckboxItemViewModel(
            fieldId = "value",
            index = index,
            value = value.toString,
            divider = messages(s"site.or")
          )
          case NoInterest => CheckboxItemViewModel(
            content = Text(messages(s"$contentPrefix.${value.toString}")),
            fieldId = "value",
            index = index,
            value = value.toString,
            behaviour = Some(ExclusiveCheckbox)
          )
          case _ => CheckboxItemViewModel(
            content = Text(messages(s"$contentPrefix.${value.toString}")),
            fieldId = "value",
            index = index,
            value = value.toString
          )
        }
    }
  }

  def checkboxItems(implicit messages: Messages): Seq[CheckboxItem] = getCheckboxItems("ukInterest")

  def agentCheckboxItems(implicit messages: Messages): Seq[CheckboxItem] =
    getCheckboxItems("ukInterest.agent")


  implicit val enumerable: Enumerable[UkInterest] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
