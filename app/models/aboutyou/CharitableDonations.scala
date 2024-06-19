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
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.{CheckboxItem, ExclusiveCheckbox}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.hint.Hint
import viewmodels.govuk.checkbox._

sealed trait CharitableDonations

object CharitableDonations extends Enumerable.Implicits {

  case object DonationsUsingGiftAid extends WithName("donationsUsingGiftAid") with CharitableDonations
  case object GiftsOfSharesOrSecurities extends WithName("giftsOfSharesOrSecurities") with CharitableDonations
  case object GiftsOfLandOrProperty extends WithName("giftsOfLandOrProperty") with CharitableDonations

  case object GiftsToOverseasCharities extends WithName("giftsToOverseasCharities") with CharitableDonations
  case object Divider extends CharitableDonations
  case object NoDonations extends WithName("noDonations") with CharitableDonations

  val values: Seq[CharitableDonations] = Seq(
    DonationsUsingGiftAid,
    GiftsOfSharesOrSecurities,
    GiftsOfLandOrProperty,
    GiftsToOverseasCharities,
    Divider,
    NoDonations
  )

  private def getCheckboxItem(contentPrefix:String)(implicit messages: Messages): Seq[CheckboxItem] = {

    values.zipWithIndex.map {
      case (value, index) =>
        value match {
          case Divider => CheckboxItemViewModel(
            fieldId = "value",
            index = index,
            value = value.toString,
            divider = messages(s"site.or")
          )
          case DonationsUsingGiftAid => CheckboxItemViewModel(
            content = Text(messages(s"$contentPrefix.${value.toString}")),
            fieldId = "value",
            index = index,
            value = value.toString
          ).withHint(Hint(content = Text(messages(s"$contentPrefix.donationsUsingGiftAid.hint"))))
          case NoDonations => CheckboxItemViewModel(
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

  def checkboxItems(implicit messages: Messages): Seq[CheckboxItem] =
    getCheckboxItem(s"charitableDonations")


  def agentCheckboxItems(implicit messages: Messages): Seq[CheckboxItem] =
    getCheckboxItem(s"charitableDonations.agent")

  implicit val enumerable: Enumerable[CharitableDonations] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
