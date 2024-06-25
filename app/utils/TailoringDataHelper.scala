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

package utils

import models.UserAnswers
import models.aboutyou.CharitableDonations.{DonationsUsingGiftAid, GiftsOfLandOrProperty, GiftsOfSharesOrSecurities, GiftsToOverseasCharities}
import models.aboutyou.{CharitableDonations, UkResidenceStatus}
import models.pensions.PaymentsIntoPensions
import models.pensions.PaymentsIntoPensions.{AnnualAllowances, Overseas, UkPensions}
import models.propertypensionsinvestments.Pensions.{OtherUkPensions, ShortServiceRefunds, StatePension, UnauthorisedPayments}
import models.propertypensionsinvestments.UkDividendsSharesLoans.{CashDividendsFromUkStocksAndShares, CloseCompanyLoansWrittenOffReleased, DividendsUnitTrustsInvestmentCompanies, FreeOrRedeemableShares, StockDividendsFromUkCompanies}
import models.propertypensionsinvestments.UkInsuranceGains.{CapitalRedemption, LifeAnnuity, LifeInsurance, VoidedISA}
import models.propertypensionsinvestments.UkInterest.{FromGiltEdged, FromUkBanks, FromUkTrustFunds}
import models.propertypensionsinvestments._
import models.workandbenefits.AboutYourWork.{Employed, SelfEmployed}
import models.workandbenefits.JobseekersAllowance.{Esa, Jsa}
import models.workandbenefits.{AboutYourWork, JobseekersAllowance}
import pages.aboutyou.{CharitableDonationsPage, FosterCarerPage, UkResidenceStatusPage}
import pages.pensions.PaymentsIntoPensionsPage
import pages.propertypensionsinvestments._
import pages.workandbenefits.{AboutYourWorkPage, AboutYourWorkRadioPage, ConstructionIndustrySchemePage, JobseekersAllowancePage}

object TailoringDataHelper {

  def getFullUserAnswers(mtdItId:String, taxYear:Int) : UserAnswers = UserAnswers(mtdItId, taxYear).set(UkResidenceStatusPage, UkResidenceStatus.Uk)
    .flatMap(_.set(CharitableDonationsPage, Set[CharitableDonations](DonationsUsingGiftAid, GiftsOfSharesOrSecurities, GiftsOfLandOrProperty, GiftsToOverseasCharities)))
    .flatMap(_.set(FosterCarerPage, true))
    .flatMap(_.set(AboutYourWorkRadioPage, true))
    .flatMap(_.set(AboutYourWorkPage, Set[AboutYourWork](Employed, SelfEmployed)))
    .flatMap(_.set(ConstructionIndustrySchemePage, true))
    .flatMap(_.set(JobseekersAllowancePage, Set[JobseekersAllowance](Jsa, Esa)))
    .flatMap(_.set(RentalIncomePage, Set[RentalIncome](RentalIncome.Uk, RentalIncome.NonUk)))
    .flatMap(_.set(PensionsPage, Set[Pensions](StatePension, OtherUkPensions, UnauthorisedPayments, ShortServiceRefunds, Pensions.NonUkPensions)))
    .flatMap(_.set(UkInsuranceGainsPage, Set[UkInsuranceGains](LifeInsurance, LifeAnnuity, CapitalRedemption, VoidedISA)))
    .flatMap(_.set(UkInterestPage, Set[UkInterest](FromUkBanks, FromUkTrustFunds, FromGiltEdged)))
    .flatMap(_.set(UkDividendsSharesLoansPage, Set[UkDividendsSharesLoans](
      CashDividendsFromUkStocksAndShares,
      StockDividendsFromUkCompanies,
      DividendsUnitTrustsInvestmentCompanies,
      FreeOrRedeemableShares,
      CloseCompanyLoansWrittenOffReleased
    )))
    .flatMap(_.set(PaymentsIntoPensionsPage, Set[PaymentsIntoPensions](UkPensions, PaymentsIntoPensions.NonUkPensions, AnnualAllowances, Overseas)))
    .get
}
