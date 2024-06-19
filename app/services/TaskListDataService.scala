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

package services

import config.FrontendAppConfig
import connectors.TaskListDataConnector
import models.aboutyou.CharitableDonations._
import models.pensions.PaymentsIntoPensions._
import models.propertypensionsinvestments.Pensions.{OtherUkPensions, ShortServiceRefunds, StatePension, UnauthorisedPayments}
import models.propertypensionsinvestments.UkDividendsSharesLoans._
import models.propertypensionsinvestments.UkInsuranceGains.{CapitalRedemption, LifeAnnuity, LifeInsurance, VoidedISA}
import models.propertypensionsinvestments.UkInterest.{FromGiltEdged, FromUkBanks, FromUkTrustFunds}
import models.propertypensionsinvestments.{Pensions, UkDividendsSharesLoans, UkInsuranceGains, UkInterest}
import models.tasklist.SectionTitle._
import models.tasklist._
import models.workandbenefits.AboutYourWork.{Employed, SelfEmployed}
import models.workandbenefits.JobseekersAllowance.Esa
import models.{Done, UserAnswers}
import pages.aboutyou.{CharitableDonationsPage, FosterCarerPage, UkResidenceStatusPage}
import pages.pensions.PaymentsIntoPensionsPage
import pages.propertypensionsinvestments.{PensionsPage, UkDividendsSharesLoansPage, UkInsuranceGainsPage, UkInterestPage}
import pages.workandbenefits.{AboutYourWorkPage, JobseekersAllowancePage}
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.Future

class TaskListDataService @Inject()(connector: TaskListDataConnector,
                                    appConfig: FrontendAppConfig) extends TaskListService {

  override def set(ua: UserAnswers)(implicit hc: HeaderCarrier): Future[Done] = {

    val taskListData: TaskListModel = getTaskList(ua)

    connector.set(TaskListData(ua.mtdItId, ua.taxYear, Json.toJsObject(taskListData)))
  }


  private def getTaskList: UserAnswers => TaskListModel = { implicit ua =>

    TaskListModel(Seq[TaskListSection](
      aboutYouSection,
      charitableDonationsSection,
      employmentSection,
      selfEmploymentSection,
      esaSection,
      jsaSection,
      pensionsSection,
      ukInsuranceGainsSection,
      paymentsIntoPensionsSection,
      interestSection,
      dividendsSection
    ).filter(_.taskItems.isDefined))
  }

  private def aboutYouSection()(implicit ua: UserAnswers): TaskListSection = {

    val residenceStatusUrl: String = appConfig.tailoringUkResidenceUrl(ua.taxYear)

    val fosterCarerUrl: String = appConfig.tailoringFosterCarerUrl(ua.taxYear)

    def ukResidence: Option[Seq[TaskListSectionItem]] =
      ua.get(UkResidenceStatusPage) match {
        case Some(_) =>
          Some(Seq(TaskListSectionItem(TaskTitle.UkResidenceStatus, TaskStatus.Completed, Some(residenceStatusUrl))))
        case _ => None
      }

    def fosterCarer: Option[Seq[TaskListSectionItem]] = {
      ua.get(FosterCarerPage) match {
        case Some(value) if value => Some(Seq(TaskListSectionItem(TaskTitle.FosterCarer, TaskStatus.Completed, Some(fosterCarerUrl))))
        case _ => None
      }
    }

    def aboutYouItems: Option[Seq[TaskListSectionItem]] = {
      val items = ukResidence.getOrElse(Seq()) ++ fosterCarer.getOrElse(Seq())
      if (items.isEmpty) None else Some(items)
    }

    TaskListSection(AboutYouTitle, aboutYouItems)
  }

  private def charitableDonationsSection()(implicit ua: UserAnswers): TaskListSection = {

    val charitableDonationsUrl: String = appConfig.charityGatewayUrl(ua.taxYear)

    def charitableDonations: Option[Seq[TaskListSectionItem]] = {

      val items = Seq(DonationsUsingGiftAid, GiftsOfSharesOrSecurities, GiftsOfLandOrProperty, GiftsToOverseasCharities)

      val taskTitles = Map[CharitableDonations, TaskTitle](
        DonationsUsingGiftAid ->  TaskTitle.DonationsUsingGiftAid,
        GiftsOfLandOrProperty ->  TaskTitle.GiftsOfLandOrProperty,
        GiftsOfSharesOrSecurities -> TaskTitle.GiftsOfShares
        DonationsUsingGiftAid -> TaskTitle.charitableDonationsTitles.DonationsUsingGiftAid(),
        GiftsOfSharesOrSecurities -> TaskTitle.charitableDonationsTitles.GiftsOfShares(),
        GiftsOfLandOrProperty -> TaskTitle.charitableDonationsTitles.GiftsOfLandOrProperty(),
        GiftsToOverseasCharities -> TaskTitle.charitableDonationsTitles.GiftsToOverseasCharities()
      )

      ua.get(CharitableDonationsPage).map(_.toList) match {
        case Some(value) if !value.contains(CharitableDonations.NoDonations) =>
          Some(items.intersect(value).map(k => TaskListSectionItem(taskTitles(k), TaskStatus.NotStarted, Some(charitableDonationsUrl))))
        case _ => None
      }
    }

    TaskListSection(CharitableDonationsTitle, charitableDonations)
  }


  private def employmentSection()(implicit ua: UserAnswers): TaskListSection = {

    val employmentUrl: String = appConfig.employmentGatewayUrl(taxYear = ua.taxYear)

    def employment: Option[Seq[TaskListSectionItem]] = {

      ua.get(AboutYourWorkPage).map(_.toSeq) match {
        case Some(value) if value.contains(Employed) =>
          Some(Seq(TaskListSectionItem(TaskTitle.PayeEmployment, TaskStatus.NotStarted, Some(employmentUrl))))
        case _ => None
      }
    }

    TaskListSection(EmploymentTitle, employment)
  }


  private def selfEmploymentSection()(implicit ua: UserAnswers): TaskListSection = {

    val cisGatewayUrl: String = appConfig.cisGatewayUrl(taxYear = ua.taxYear)

    def selfEmployment: Option[Seq[TaskListSectionItem]] = {

      ua.get(AboutYourWorkPage).map(_.toSeq) match {
        case Some(value) if value.contains(SelfEmployed) =>
          Some(Seq(TaskListSectionItem(TaskTitle.CIS, TaskStatus.NotStarted, Some(cisGatewayUrl))))
        case _ => None
      }
    }

    TaskListSection(SelfEmploymentTitle, selfEmployment)
  }


  private def esaSection()(implicit ua: UserAnswers): TaskListSection = {

    val esaUrl: String = appConfig.stateBenefitsEsaJourneyGatewayUrl(taxYear = ua.taxYear)

    def esa: Option[Seq[TaskListSectionItem]] = {

      ua.get(JobseekersAllowancePage).map(_.toSeq) match {
        case Some(value) if value.contains(Esa) =>
          Some(Seq(TaskListSectionItem(TaskTitle.ESA, TaskStatus.NotStarted, Some(esaUrl))))
        case _ => None
      }
    }

    TaskListSection(EsaTitle, esa)
  }


  private def jsaSection()(implicit ua: UserAnswers): TaskListSection = {

    val jsaUrl: String = appConfig.stateBenefitsJsaJourneyGatewayUrl(taxYear = ua.taxYear)

    def jsa: Option[Seq[TaskListSectionItem]] = {

      ua.get(JobseekersAllowancePage).map(_.toSeq) match {
        case Some(value) if value.contains(Esa) =>
          Some(Seq(TaskListSectionItem(TaskTitle.JSA, TaskStatus.NotStarted, Some(jsaUrl))))
        case _ => None
      }
    }

    TaskListSection(JsaTitle, jsa)
  }


  private def pensionsSection()(implicit ua: UserAnswers): TaskListSection = {

    val pensionsGatewayUrl = appConfig.pensionsGatewayUrl(ua.taxYear)

    def pensionsUrl: Pensions => String = {
      case Pensions.StatePension => pensionsGatewayUrl
      case Pensions.OtherUkPensions => pensionsGatewayUrl
      case Pensions.UnauthorisedPayments => pensionsGatewayUrl
      case Pensions.ShortServiceRefunds => pensionsGatewayUrl
      case Pensions.NonUkPensions => pensionsGatewayUrl
      case _ => ""
    }

    def pensions: Option[Seq[TaskListSectionItem]] = {

      val items = Seq(StatePension, OtherUkPensions, UnauthorisedPayments, ShortServiceRefunds, models.propertypensionsinvestments.Pensions.NonUkPensions)

      val taskTitles = Map[Pensions, TaskTitle](
        StatePension -> TaskTitle.StatePension,
        OtherUkPensions -> TaskTitle.OtherUkPensions,
        UnauthorisedPayments -> TaskTitle.UnauthorisedPayments,
        ShortServiceRefunds -> TaskTitle.ShortServiceRefunds,
        models.propertypensionsinvestments.Pensions.NonUkPensions -> TaskTitle.IncomeFromOverseas
      )

      ua.get(PensionsPage).map(_.toSeq) match {
        case Some(value) if !value.contains(Pensions.NoPensions) =>
          Some(items.intersect(value).map(k => TaskListSectionItem(taskTitles(k), TaskStatus.NotStarted, Some(pensionsUrl(k)))))
        case _ => None
      }
    }

    TaskListSection(PensionsTitle, pensions)
  }

  private def ukInsuranceGainsSection()(implicit ua: UserAnswers) = {

    val gainsUrl: String = appConfig.additionalInfoUrl(ua.taxYear)

    val ukInsuranceGainsUrl: UkInsuranceGains => String = {
      case UkInsuranceGains.LifeInsurance => gainsUrl
      case UkInsuranceGains.LifeAnnuity => gainsUrl
      case UkInsuranceGains.CapitalRedemption => gainsUrl
      case UkInsuranceGains.VoidedISA => gainsUrl
      case _ => ""
    }

    def ukInsuranceGains: Option[Seq[TaskListSectionItem]] = {

      val items = Seq(LifeInsurance, LifeAnnuity, CapitalRedemption, VoidedISA)

      val taskTitles = Map[UkInsuranceGains, TaskTitle](
        LifeInsurance -> TaskTitle.LifeInsurance,
        LifeAnnuity -> TaskTitle.LifeAnnuity,
        CapitalRedemption -> TaskTitle.CapitalRedemption,
        VoidedISA -> TaskTitle.VoidedISA
      )

      ua.get(UkInsuranceGainsPage).map(_.toSeq) match {
        case Some(value) if !value.contains(UkInsuranceGains.No) =>
          Some(items.intersect(value).map(k => TaskListSectionItem(taskTitles(k), TaskStatus.NotStarted, Some(ukInsuranceGainsUrl(k)))))
        case _ => None
      }
    }
    TaskListSection(InsuranceGainsTitle, ukInsuranceGains)
  }

  private def paymentsIntoPensionsSection()(implicit ua: UserAnswers): TaskListSection = {

    def paymentsIntoPensionsUrl: PaymentsIntoPensions => String = {
      case PaymentsIntoPensions.UkPensions => appConfig.paymentsIntoPensionsGatewayUrl(ua.taxYear)
      case PaymentsIntoPensions.AnnualAllowances => appConfig.annualAllowancesUrl(ua.taxYear)
      case PaymentsIntoPensions.NonUkPensions => appConfig.incomeFromOverseasGatewayUrl(ua.taxYear)
      case PaymentsIntoPensions.Overseas => appConfig.overseasTransferChargesGatewayUrl(ua.taxYear)
      case _ => ""
    }

    def paymentsIntoPensions: Option[Seq[TaskListSectionItem]] = {

      val items = Seq(UkPensions, AnnualAllowances, models.pensions.PaymentsIntoPensions.NonUkPensions, Overseas)

      val taskTitles = Map[PaymentsIntoPensions, TaskTitle](
        UkPensions -> TaskTitle.PaymentsIntoUk,
        AnnualAllowances -> TaskTitle.AnnualAllowances,
        models.pensions.PaymentsIntoPensions.NonUkPensions -> TaskTitle.PaymentsIntoOverseas,
        Overseas -> TaskTitle.OverseasTransfer
      )

      ua.get(PaymentsIntoPensionsPage).map(_.toSeq) match {
        case Some(value) if !value.contains(PaymentsIntoPensions.No) =>
          Some(items.intersect(value).map(k => TaskListSectionItem(taskTitles(k), TaskStatus.NotStarted, Some(paymentsIntoPensionsUrl(k)))))
        case _ => None
      }
    }

    TaskListSection(PaymentsIntoPensionsTitle, paymentsIntoPensions)
  }


  private def interestSection()(implicit ua: UserAnswers): TaskListSection = {

    val ukInterestGatewayUrl = appConfig.ukInterestGatewayUrl(ua.taxYear)

    def interestUrl: UkInterest => String = {
      case UkInterest.FromUkBanks => ukInterestGatewayUrl
      case UkInterest.FromUkTrustFunds => ukInterestGatewayUrl
      case UkInterest.FromGiltEdged => appConfig.giltEdgedGatewayUrl(ua.taxYear)
      case _ => ""
    }

    def interest: Option[Seq[TaskListSectionItem]] = {

      val items = Seq(FromUkBanks, FromUkTrustFunds, FromGiltEdged)

      val taskTitles = Map[UkInterest, TaskTitle](
        FromUkBanks -> TaskTitle.BanksAndBuilding,
        FromUkTrustFunds -> TaskTitle.TrustFundBond,
        FromGiltEdged -> TaskTitle.GiltEdged
      )

      ua.get(UkInterestPage).map(_.toSeq) match {
        case Some(value) if !value.contains(UkInterest.NoInterest) =>
          Some(items.intersect(value).map(k => TaskListSectionItem(taskTitles(k), TaskStatus.NotStarted, Some(interestUrl(k)))))
        case _ => None
      }
    }

    TaskListSection(InterestTitle, interest)
  }


  private def dividendsSection()(implicit ua: UserAnswers): TaskListSection = {

    val dividendsGatewayUrl = appConfig.dividendsGatewayUrl(ua.taxYear)

    def dividendsUrl: UkDividendsSharesLoans => String = {
      case UkDividendsSharesLoans.CashDividendsFromUkStocksAndShares => dividendsGatewayUrl
      case UkDividendsSharesLoans.StockDividendsFromUkCompanies => dividendsGatewayUrl
      case UkDividendsSharesLoans.DividendsUnitTrustsInvestmentCompanies => dividendsGatewayUrl
      case UkDividendsSharesLoans.FreeOrRedeemableShares => dividendsGatewayUrl
      case UkDividendsSharesLoans.CloseCompanyLoansWrittenOffReleased => dividendsGatewayUrl
      case _ => ""
    }

    def dividends: Option[Seq[TaskListSectionItem]] = {

      val items = Seq(
        CashDividendsFromUkStocksAndShares,
        StockDividendsFromUkCompanies,
        DividendsUnitTrustsInvestmentCompanies,
        FreeOrRedeemableShares,
        CloseCompanyLoansWrittenOffReleased
      )

      val taskTitles = Map[UkDividendsSharesLoans, TaskTitle](
        CashDividendsFromUkStocksAndShares -> TaskTitle.CashDividends,
        StockDividendsFromUkCompanies -> TaskTitle.StockDividends,
        DividendsUnitTrustsInvestmentCompanies -> TaskTitle.DividendsFromUnitTrusts,
        FreeOrRedeemableShares -> TaskTitle.FreeRedeemableShares,
        CloseCompanyLoansWrittenOffReleased -> TaskTitle.CloseCompanyLoans
      )

      ua.get(UkDividendsSharesLoansPage).map(_.toSeq) match {
        case Some(value) if !value.contains(UkDividendsSharesLoans.NoUkDividendsSharesOrLoans) =>
          Some(items.intersect(value).map(k => TaskListSectionItem(taskTitles(k), TaskStatus.NotStarted, Some(dividendsUrl(k)))))
        case _ => None
      }
    }

    TaskListSection(DividendsTitle, dividends)
  }

}
