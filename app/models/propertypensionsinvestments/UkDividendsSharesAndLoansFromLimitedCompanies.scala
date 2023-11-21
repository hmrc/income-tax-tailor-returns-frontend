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
import uk.gov.hmrc.govukfrontend.views.Aliases.Hint
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.{CheckboxItem, ExclusiveCheckbox}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import viewmodels.govuk.checkbox._

sealed trait UkDividendsSharesAndLoansFromLimitedCompanies

object UkDividendsSharesAndLoansFromLimitedCompanies extends Enumerable.Implicits {

  case object CashDividendsFromUkStocksAndShares
    extends WithName("cashDividendsUkStocksAndShares") with UkDividendsSharesAndLoansFromLimitedCompanies
  case object StockDividendsFromUkCompanies
    extends WithName("stockDividendsUkCompanies") with UkDividendsSharesAndLoansFromLimitedCompanies
  case object DividendsUnitTrustsInvestmentCompanies
    extends WithName("dividendsUnitTrustsInvestmentCompanies") with UkDividendsSharesAndLoansFromLimitedCompanies
  case object FreeOrRedeemableShares
    extends WithName("freeOrRedeemableShares") with UkDividendsSharesAndLoansFromLimitedCompanies
  case object CloseCompanyLoansWrittenOffReleased
    extends WithName("closeCompanyLoansWrittenOffReleased") with UkDividendsSharesAndLoansFromLimitedCompanies
  case object Divider
    extends UkDividendsSharesAndLoansFromLimitedCompanies
  case object NoUkDividendsSharesOrLoans
    extends WithName("noUkDividendsSharesOrLoans") with UkDividendsSharesAndLoansFromLimitedCompanies

  val values: Seq[UkDividendsSharesAndLoansFromLimitedCompanies] = Seq(
    CashDividendsFromUkStocksAndShares,
    StockDividendsFromUkCompanies,
    DividendsUnitTrustsInvestmentCompanies,
    FreeOrRedeemableShares,
    CloseCompanyLoansWrittenOffReleased,
    Divider,
    NoUkDividendsSharesOrLoans
  )

  def checkboxItems(implicit messages: Messages): Seq[CheckboxItem] =
    values.zipWithIndex.map {
      case (value, index) =>
        value match {
          case Divider => CheckboxItemViewModel(
            fieldId = "value",
            index = index,
            value = value.toString,
            divider = messages("site.or")
          )
          case CashDividendsFromUkStocksAndShares => CheckboxItemViewModel(
            content = Text(messages(s"ukDividendsSharesAndLoansFromLimitedCompanies.${value.toString}")),
            fieldId = "value",
            index = index,
            value = value.toString
          ).withHint(Hint(content = Text(messages(s"ukDividendsSharesAndLoansFromLimitedCompanies.dividends.hint"))))
          case StockDividendsFromUkCompanies => CheckboxItemViewModel(
            content = Text(messages(s"ukDividendsSharesAndLoansFromLimitedCompanies.${value.toString}")),
            fieldId = "value",
            index = index,
            value = value.toString
          ).withHint(Hint(content = Text(messages(s"ukDividendsSharesAndLoansFromLimitedCompanies.dividends.hint"))))
          case NoUkDividendsSharesOrLoans => CheckboxItemViewModel(
            content = Text(messages(s"ukDividendsSharesAndLoansFromLimitedCompanies.${value.toString}")),
            fieldId = "value",
            index = index,
            value = value.toString,
            behaviour = Some(ExclusiveCheckbox)
          )
          case _ => CheckboxItemViewModel(
            content = Text (messages(s"ukDividendsSharesAndLoansFromLimitedCompanies.${value.toString}")),
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
          case Divider => CheckboxItemViewModel(
            fieldId = "value",
            index = index,
            value = value.toString,
            divider = messages("site.or")
          )
          case CashDividendsFromUkStocksAndShares => CheckboxItemViewModel(
            content = Text(messages(s"ukDividendsSharesAndLoansFromLimitedCompanies.agent.${value.toString}")),
            fieldId = "value",
            index = index,
            value = value.toString
          ).withHint(Hint(content = Text(messages(s"ukDividendsSharesAndLoansFromLimitedCompanies.agent.dividends.hint"))))
          case StockDividendsFromUkCompanies => CheckboxItemViewModel(
            content = Text(messages(s"ukDividendsSharesAndLoansFromLimitedCompanies.agent.${value.toString}")),
            fieldId = "value",
            index = index,
            value = value.toString
          ).withHint(Hint(content = Text(messages(s"ukDividendsSharesAndLoansFromLimitedCompanies.agent.dividends.hint"))))
          case NoUkDividendsSharesOrLoans => CheckboxItemViewModel(
            content = Text(messages(s"ukDividendsSharesAndLoansFromLimitedCompanies.agent.${value.toString}")),
            fieldId = "value",
            index = index,
            value = value.toString,
            behaviour = Some(ExclusiveCheckbox)
          )
          case _ => CheckboxItemViewModel(
            content = Text(messages(s"ukDividendsSharesAndLoansFromLimitedCompanies.agent.${value.toString}")),
            fieldId = "value",
            index = index,
            value = value.toString
          )
        }
    }

  implicit val enumerable: Enumerable[UkDividendsSharesAndLoansFromLimitedCompanies] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
