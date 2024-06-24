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

import models.Enumerable
import models.tasklist.taskItemTitles._

trait TaskTitle extends Enumerable.Implicits

object TaskTitle extends TaskTitle {

  val aboutYouItemTitles: AboutYouItemTitles.type = AboutYouItemTitles
  val charitableDonationsTitles: CharitableDonationsTitles.type = CharitableDonationsTitles
  val employmentTitles: EmploymentTitles.type = EmploymentTitles
  val selfEmploymentTitles: SelfEmploymentTitles.type = SelfEmploymentTitles
  val esaTitles: EsaTitles.type = EsaTitles
  val jsaTitles: JsaTitles.type = JsaTitles
  val pensionsTitles: PensionsTitles.type = PensionsTitles
  val paymentsIntoPensionsTitles: PaymentsIntoPensionsTitles.type = PaymentsIntoPensionsTitles
  val ukInterestTitles: UkInterestTitles.type = UkInterestTitles
  val ukDividendsTitles: UkDividendsTitles.type = UkDividendsTitles
  val ukInsuranceGainsTitles: UkInsuranceGainsTitles.type = UkInsuranceGainsTitles

  val values: Seq[TaskTitle] = Seq(
    aboutYouItemTitles.UkResidenceStatus(),
    aboutYouItemTitles.FosterCarer(),
    charitableDonationsTitles.DonationsUsingGiftAid(),
    charitableDonationsTitles.GiftsOfLandOrProperty(),
    charitableDonationsTitles.GiftsOfShares(),
    employmentTitles.PayeEmployment(),
    selfEmploymentTitles.CIS(),
    esaTitles.ESA(),
    jsaTitles.JSA(),
    pensionsTitles.StatePension(),
    pensionsTitles.OtherUkPensions(),
    pensionsTitles.IncomeFromOverseas(),
    pensionsTitles.UnauthorisedPayments(),
    pensionsTitles.ShortServiceRefunds(),
    ukInsuranceGainsTitles.LifeInsurance(),
    ukInsuranceGainsTitles.LifeAnnuity(),
    ukInsuranceGainsTitles.CapitalRedemption(),
    ukInsuranceGainsTitles.VoidedISA(),
    paymentsIntoPensionsTitles.PaymentsIntoUk(),
    paymentsIntoPensionsTitles.AnnualAllowances(),
    paymentsIntoPensionsTitles.PaymentsIntoOverseas(),
    paymentsIntoPensionsTitles.OverseasTransfer(),
    ukInterestTitles.BanksAndBuilding(),
    ukInterestTitles.TrustFundBond(),
    ukInterestTitles.GiltEdged(),
    ukDividendsTitles.CashDividends(),
    ukDividendsTitles.StockDividends(),
    ukDividendsTitles.DividendsFromUnitTrusts(),
    ukDividendsTitles.FreeRedeemableShares(),
    ukDividendsTitles.CloseCompanyLoans()
  )

  implicit val enumerable: Enumerable[TaskTitle] =
    Enumerable(values.map(v => v.toString -> v): _*)

}