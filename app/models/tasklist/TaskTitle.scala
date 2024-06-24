/*
 * Copyright 2024 HM Revenue & Customs
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

package models.tasklist

import enumeratum.{Enum, EnumEntry}
import models.PlayJsonEnum

sealed abstract class TaskTitle(override val entryName: String) extends EnumEntry {
  override def toString: String = entryName
}

object TaskTitle extends Enum[TaskTitle] with PlayJsonEnum[TaskTitle] {

  val values: IndexedSeq[TaskTitle] = findValues

  // About you
  case object UkResidenceStatus extends TaskTitle("UkResidenceStatusTitle")

  case object FosterCarer extends TaskTitle("FosterCarerTitle")

  // Charitable Donations
  case object DonationsUsingGiftAid extends TaskTitle("DonationsUsingGiftAidTitle")

  case object GiftsOfLandOrProperty extends TaskTitle("GiftsOfLandOrPropertyTitle")

  case object GiftsOfShares extends TaskTitle("GiftsOfSharesTitle")

  // Employment
  case object PayeEmployment extends TaskTitle("PayeEmploymentTitle")

  // Self-employment
  case object CIS extends TaskTitle("CISTitle")

  // Esa
  case object ESA extends TaskTitle("ESATitle")

  // Jsa
  case object JSA extends TaskTitle("JSATitle")

  // Pensions
  case object StatePension extends TaskTitle("StatePensionTitle")

  case object OtherUkPensions extends TaskTitle("OtherUkPensionsTitle")

  case object UnauthorisedPayments extends TaskTitle("UnauthorisedPaymentsTitle")

  case object ShortServiceRefunds extends TaskTitle("ShortServiceRefundsTitle")

  case object IncomeFromOverseas extends TaskTitle("IncomeFromOverseasTitle")

  // UK insurance gains
  case object LifeInsurance extends TaskTitle("LifeInsuranceTitle")

  case object LifeAnnuity extends TaskTitle("LifeAnnuityTitle")

  case object CapitalRedemption extends TaskTitle("CapitalRedemptionTitle")

  case object VoidedISA extends TaskTitle("VoidedISATitle")

  // Payments into pensions
  case object PaymentsIntoUk extends TaskTitle("PaymentsIntoUkTitle")

  case object AnnualAllowances extends TaskTitle("AnnualAllowancesTitle")

  case object PaymentsIntoOverseas extends TaskTitle("PaymentsIntoOverseasTitle")

  case object OverseasTransfer extends TaskTitle("OverseasTransferTitle")

  // UK interest
  case object BanksAndBuilding extends TaskTitle("BanksAndBuildingTitle")

  case object TrustFundBond extends TaskTitle("TrustFundBondTitle")

  case object GiltEdged extends TaskTitle("GiltEdgedTitle")

  // UK dividends
  case object CashDividends extends TaskTitle("CashDividendsTitle")

  case object StockDividends extends TaskTitle("StockDividendsTitle")

  case object DividendsFromUnitTrusts extends TaskTitle("DividendsFromUnitTrustsTitle")

  case object FreeRedeemableShares extends TaskTitle("FreeRedeemableSharesTitle")

  case object CloseCompanyLoans extends TaskTitle("CloseCompanyLoansTitle")
}