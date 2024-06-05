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
import models.TagStatus.NotStarted
import models.aboutyou.CharitableDonations.{DonationsUsingGiftAid, GiftsOfLandOrProperty, GiftsOfSharesOrSecurities}
import models.aboutyou.{CharitableDonations, UkResidenceStatus}
import models.pensions.PaymentsIntoPensions
import models.pensions.PaymentsIntoPensions.{AnnualAllowances, Overseas, UkPensions}
import models.propertypensionsinvestments.Pensions.{OtherUkPensions, ShortServiceRefunds, StatePension, UnauthorisedPayments}
import models.propertypensionsinvestments.UkDividendsSharesLoans._
import models.propertypensionsinvestments.UkInterest.{FromGiltEdged, FromUkBanks, FromUkTrustFunds}
import models.propertypensionsinvestments.{Pensions, UkDividendsSharesLoans, UkInterest}
import models.tasklist.SectionTitle._
import models.tasklist._
import models.workandbenefits.AboutYourWork.{Employed, SelfEmployed}
import models.workandbenefits.JobseekersAllowance.Esa
import models.workandbenefits.{AboutYourWork, JobseekersAllowance}
import models.{Done, UserAnswers}
import pages.aboutyou.{CharitableDonationsPage, FosterCarerPage, UkResidenceStatusPage}
import pages.pensions.PaymentsIntoPensionsPage
import pages.propertypensionsinvestments.{PensionsPage, UkDividendsSharesLoansPage, UkInterestPage}
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
      paymentsIntoPensionsSection,
      interestSection,
      dividendsSection
    ).filter(_.taskItems.isDefined))
  }

  // TODO: URLs to be added as part of dependency mapping
  private def aboutYouSection()(implicit ua: UserAnswers): TaskListSection = {

    val residenceStatusUrl: String = appConfig.tailoringUkResidenceUrl(ua.taxYear)
    val fosterCarerUrl: String = appConfig.tailoringFosterCarerUrl(ua.taxYear)

    def ukResidence: Option[Seq[TaskListSectionItem]] =
      ua.get(UkResidenceStatusPage) match {
        case Some(UkResidenceStatus.Uk) =>
          Some(Seq(TaskListSectionItem(TaskTitle(UkResidenceStatusPage), TaskStatus.NotStarted(), Some(residenceStatusUrl))))
        case Some(UkResidenceStatus.Domiciled) =>
          Some(Seq(TaskListSectionItem(TaskTitle(UkResidenceStatusPage), TaskStatus.NotStarted(), Some(residenceStatusUrl))))
        case _ => None
      }

    def fosterCarer: Option[Seq[TaskListSectionItem]] = {
      ua.get(FosterCarerPage) match {
        case Some(value) if value => Some(Seq(TaskListSectionItem(TaskTitle(FosterCarerPage), TaskStatus.NotStarted(), Some(fosterCarerUrl))))
        case _ => None
      }
    }

    val aboutYou = ukResidence.getOrElse(Seq()) ++ fosterCarer.getOrElse(Seq())

    TaskListSection(AboutYouTitle(), Some(aboutYou))
  }


  private def charitableDonationsSection()(implicit ua: UserAnswers): TaskListSection = {

    val charitableDonationsUrl: String = appConfig.charityGatewayUrl(taxYear = ua.taxYear)

    def charitableDonations: Option[Seq[TaskListSectionItem]] = {

      val links = Seq(DonationsUsingGiftAid, GiftsOfLandOrProperty, GiftsOfSharesOrSecurities)

      ua.get(CharitableDonationsPage).map(_.toList) match {
        case Some(value) if !value.contains(CharitableDonations.NoDonations) =>
          Some(links.intersect(value).map(k => TaskListSectionItem(TaskTitle(k.toString), TaskStatus.NotStarted(), Some(charitableDonationsUrl))))
        case _ => None
      }
    }

    TaskListSection(CharitableDonationsTitle(), charitableDonations)
  }


  private def employmentSection()(implicit ua: UserAnswers): TaskListSection = {

    val employmentUrl: String = appConfig.employmentGatewayUrl(taxYear = ua.taxYear)

    def employment: Option[Seq[TaskListSectionItem]] = {

      ua.get(AboutYourWorkPage).map(_.toSeq) match {
        case Some(value) if value.contains(Employed) =>
          Some(Seq(TaskListSectionItem(TaskTitle(AboutYourWork.Employed.toString), TaskStatus.NotStarted(), Some(employmentUrl))))
        case _ => None
      }
    }

    TaskListSection(EmploymentTitle(), employment)
  }


  private def selfEmploymentSection()(implicit ua: UserAnswers): TaskListSection = {

    val cisGatewayUrl: String = appConfig.cisGatewayUrl(taxYear = ua.taxYear)

    def selfEmployment: Option[Seq[TaskListSectionItem]] = {

      ua.get(AboutYourWorkPage).map(_.toSeq) match {
        case Some(value) if value.contains(SelfEmployed) =>
          Some(Seq(TaskListSectionItem(TaskTitle(AboutYourWork.SelfEmployed.toString), TaskStatus.NotStarted(), Some(cisGatewayUrl))))
        case _ => None
      }
    }

    TaskListSection(SelfEmploymentTitle(), selfEmployment)
  }


  private def esaSection()(implicit ua: UserAnswers): TaskListSection = {

    val esaUrl: String = appConfig.stateBenefitsEsaJourneyGatewayUrl(taxYear = ua.taxYear)

    def esa: Option[Seq[TaskListSectionItem]] = {

      ua.get(JobseekersAllowancePage).map(_.toSeq) match {
        case Some(value) if value.contains(Esa) =>
          Some(Seq(TaskListSectionItem(TaskTitle(JobseekersAllowance.Esa.toString), TaskStatus.NotStarted(), Some(esaUrl))))
        case _ => None
      }
    }

    TaskListSection(EsaTitle(), esa)
  }


  private def jsaSection()(implicit ua: UserAnswers): TaskListSection = {

    val jsaUrl: String = appConfig.stateBenefitsJsaJourneyGatewayUrl(taxYear = ua.taxYear)

    def jsa: Option[Seq[TaskListSectionItem]] = {

      ua.get(JobseekersAllowancePage).map(_.toSeq) match {
        case Some(value) if value.contains(Esa) =>
          Some(Seq(TaskListSectionItem(TaskTitle(JobseekersAllowance.Jsa.toString), TaskStatus.NotStarted(), Some(jsaUrl))))
        case _ => None
      }
    }

    TaskListSection(JsaTitle(), jsa)
  }


  private def pensionsSection()(implicit ua: UserAnswers): TaskListSection = {

    val items = List(StatePension, OtherUkPensions, UnauthorisedPayments, ShortServiceRefunds, models.propertypensionsinvestments.Pensions.NonUkPensions)

    val pensionsGatewayUrl = appConfig.pensionsGatewayUrl(taxYear = ua.taxYear)
    def pensionsUrl: Pensions => String = {
      case Pensions.StatePension => pensionsGatewayUrl
      case Pensions.OtherUkPensions => pensionsGatewayUrl
      case Pensions.UnauthorisedPayments => pensionsGatewayUrl
      case Pensions.ShortServiceRefunds => pensionsGatewayUrl
      case Pensions.NonUkPensions => pensionsGatewayUrl
    }

    def pensions: Option[Seq[TaskListSectionItem]] = {

      ua.get(PensionsPage).map(_.toSeq) match {
        case Some(value) if !value.contains(Pensions.NoPensions) =>
          Some(items.intersect(value).map(k => TaskListSectionItem(TaskTitle(k.toString), TaskStatus.NotStarted(), Some(pensionsUrl(k)))))
        case _ => None
      }
    }

    TaskListSection(PensionsTitle(), pensions)
  }


  private def paymentsIntoPensionsSection()(implicit ua: UserAnswers): TaskListSection = {
    val paymentsIntoPensionsGatewayUrl = appConfig.paymentsIntoPensionsGatewayUrl(taxYear = ua.taxYear)
    val incomeFromOverseasGatewayUrl = appConfig.incomeFromOverseasGatewayUrl(taxYear = ua.taxYear)
    val overseasTransferChargesGatewayUrl = appConfig.overseasTransferChargesGatewayUrl(taxYear = ua.taxYear)
    def paymentsIntoPensionsUrl: PaymentsIntoPensions => String = {
      case PaymentsIntoPensions.UkPensions => paymentsIntoPensionsGatewayUrl
      case PaymentsIntoPensions.NonUkPensions => incomeFromOverseasGatewayUrl
      case PaymentsIntoPensions.AnnualAllowances => ""
      case PaymentsIntoPensions.Overseas => overseasTransferChargesGatewayUrl
    }

    def paymentsIntoPensions: Option[Seq[TaskListSectionItem]] = {
      val links = List(UkPensions, models.pensions.PaymentsIntoPensions.NonUkPensions, AnnualAllowances, Overseas)

      ua.get(PaymentsIntoPensionsPage).map(_.toSeq) match {
        case Some(value) if !value.contains(PaymentsIntoPensions.No) =>
          Some(links.intersect(value).map(k => TaskListSectionItem(TaskTitle(k.toString), TaskStatus.NotStarted(), Some(paymentsIntoPensionsUrl(k)))))
        case _ => None
      }
    }

    TaskListSection(PaymentsIntoPensionsTitle(), paymentsIntoPensions)
  }


  private def interestSection()(implicit ua: UserAnswers): TaskListSection = {

    val ukInterestGatewayUrl = appConfig.ukInterestGatewayUrl(taxYear = ua.taxYear)
    val giltEdgedGatewayUrl = appConfig.giltEdgedGatewayUrl(taxYear = ua.taxYear)
    def interestUrl: UkInterest => String = {
      case UkInterest.FromUkBanks => ukInterestGatewayUrl
      case UkInterest.FromUkTrustFunds => ukInterestGatewayUrl
      case UkInterest.FromGiltEdged => giltEdgedGatewayUrl
    }

    def interest: Option[Seq[TaskListSectionItem]] = {

      val links = List(FromUkBanks, FromUkTrustFunds, FromGiltEdged)

      ua.get(UkInterestPage).map(_.toSeq) match {
        case Some(value) if !value.contains(UkInterest.NoInterest) =>
          Some(links.intersect(value).map(k => TaskListSectionItem(TaskTitle(k.toString), TaskStatus.NotStarted(), Some(interestUrl(k)))))
        case _ => None
      }
    }

    TaskListSection(InterestTitle(), interest)
  }


  private def dividendsSection()(implicit ua: UserAnswers): TaskListSection = {

    val dividendsGatewayUrl = appConfig.dividendsGatewayUrl(taxYear = ua.taxYear)
    def dividendsUrl: UkDividendsSharesLoans => String = {
      case UkDividendsSharesLoans.CashDividendsFromUkStocksAndShares => dividendsGatewayUrl
      case UkDividendsSharesLoans.StockDividendsFromUkCompanies => dividendsGatewayUrl
      case UkDividendsSharesLoans.DividendsUnitTrustsInvestmentCompanies => dividendsGatewayUrl
      case UkDividendsSharesLoans.FreeOrRedeemableShares => dividendsGatewayUrl
      case UkDividendsSharesLoans.CloseCompanyLoansWrittenOffReleased => dividendsGatewayUrl
    }

    def dividends: Option[Seq[TaskListSectionItem]] = {
      val links = List(
        CashDividendsFromUkStocksAndShares,
        StockDividendsFromUkCompanies,
        DividendsUnitTrustsInvestmentCompanies,
        FreeOrRedeemableShares,
        CloseCompanyLoansWrittenOffReleased
      )

      ua.get(UkDividendsSharesLoansPage).map(_.toSeq) match {
        case Some(value) if !value.contains(UkDividendsSharesLoans.NoUkDividendsSharesOrLoans) =>
          Some(links.intersect(value).map(k => TaskListSectionItem(TaskTitle(k.toString), TaskStatus.NotStarted(), Some(dividendsUrl(k)))))
        case _ => None
      }
    }

    TaskListSection(DividendsTitle(), dividends)
  }

}
