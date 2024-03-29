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
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait HighIncomeChildBenefitCharge

object HighIncomeChildBenefitCharge extends Enumerable.Implicits {

  case object SelfIncome extends WithName("selfIncome") with HighIncomeChildBenefitCharge
  case object PartnerIncome extends WithName("partnerIncome") with HighIncomeChildBenefitCharge
  case object NoPartner extends WithName("noPartner") with HighIncomeChildBenefitCharge

  val values: Seq[HighIncomeChildBenefitCharge] = Seq(
    SelfIncome, PartnerIncome, NoPartner
  )

  private def getRadioItems(contentPrefix: String)(implicit messages: Messages): Seq[RadioItem] = {
    values.zipWithIndex.map {
      case (value, index) =>
        RadioItem(
          content = Text(messages(s"$contentPrefix.${value.toString}")),
          value = Some(value.toString),
          id = Some(s"value_$index")
        )
    }
  }

  def options(implicit messages: Messages): Seq[RadioItem] = getRadioItems("highIncomeChildBenefitCharge")

  def agentOptions(implicit messages: Messages): Seq[RadioItem] = getRadioItems("highIncomeChildBenefitCharge.agent")

  implicit val enumerable: Enumerable[HighIncomeChildBenefitCharge] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
