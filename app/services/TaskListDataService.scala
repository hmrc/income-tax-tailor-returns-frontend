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

import connectors.TaskListDataConnector
import models.TagStatus.NotStarted
import models.aboutyou.CharitableDonations.{DonationsUsingGiftAid, GiftsOfLandOrProperty, GiftsOfSharesOrSecurities}
import models.aboutyou.{CharitableDonations, UkResidenceStatus}
import models.pensions.PaymentsIntoPensions
import models.pensions.PaymentsIntoPensions.{Overseas, UkPensions}
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

class TaskListDataService @Inject()(connector: TaskListDataConnector) extends TaskListService {

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

    val residenceStatusUrl: String = ""
    val fosterCarerUrl: String = ""

    def ukResidence: Option[Seq[TaskListSectionItem]] =
      ua.get(UkResidenceStatusPage) match {
        case Some(UkResidenceStatus.Uk) =>
          Some(Seq(TaskListSectionItem(TaskTitle(UkResidenceStatusPage), TaskStatus(NotStarted.toString), Some(residenceStatusUrl))))
        case Some(UkResidenceStatus.Domiciled) =>
          Some(Seq(TaskListSectionItem(TaskTitle(UkResidenceStatusPage), TaskStatus(NotStarted.toString), Some(residenceStatusUrl))))
        case _ => None
      }

    def fosterCarer: Option[Seq[TaskListSectionItem]] = {
      ua.get(FosterCarerPage) match {
        case Some(value) if value => Some(Seq(TaskListSectionItem(TaskTitle(FosterCarerPage), TaskStatus(NotStarted.toString), Some(fosterCarerUrl))))
        case _ => None
      }
    }

    val aboutYou: Option[Seq[TaskListSectionItem]] = for {
      residence <- ukResidence
      foster <- fosterCarer
    } yield {
      residence ++ foster
    }

    TaskListSection(AboutYouTitle.toString, aboutYou)
  }


  private def charitableDonationsSection()(implicit ua: UserAnswers): TaskListSection = {

    val charitableDonationsUrl: String = ""

    def charitableDonations: Option[Seq[TaskListSectionItem]] = {

      val links = Seq(DonationsUsingGiftAid, GiftsOfLandOrProperty, GiftsOfSharesOrSecurities)

      ua.get(CharitableDonationsPage).map(_.toList) match {
        case Some(value) if !value.contains(CharitableDonations.NoDonations) =>
          Some(links.intersect(value).map(k => TaskListSectionItem(TaskTitle(k.toString), TaskStatus(NotStarted.toString), Some(charitableDonationsUrl))))
        case _ => None
      }
    }

    TaskListSection(CharitableDonationsTitle.toString, charitableDonations)
  }


  private def employmentSection()(implicit ua: UserAnswers): TaskListSection = {

    val employmentUrl: String = ""

    def employment: Option[Seq[TaskListSectionItem]] = {

      ua.get(AboutYourWorkPage).map(_.toSeq) match {
        case Some(value) if value.contains(Employed) =>
          Some(Seq(TaskListSectionItem(TaskTitle(AboutYourWork.Employed.toString), TaskStatus(NotStarted.toString), Some(employmentUrl))))
        case _ => None
      }
    }

    TaskListSection(EmploymentTitle.toString, employment)
  }


  private def selfEmploymentSection()(implicit ua: UserAnswers): TaskListSection = {

    val selfEmploymentUrl: String = ""

    def selfEmployment: Option[Seq[TaskListSectionItem]] = {

      ua.get(AboutYourWorkPage).map(_.toSeq) match {
        case Some(value) if value.contains(SelfEmployed) =>
          Some(Seq(TaskListSectionItem(TaskTitle(AboutYourWork.SelfEmployed.toString), TaskStatus(NotStarted.toString), Some(selfEmploymentUrl))))
        case _ => None
      }
    }

    TaskListSection(SelfEmploymentTitle.toString, selfEmployment)
  }


  private def esaSection()(implicit ua: UserAnswers): TaskListSection = {

    val esaUrl: String = ""

    def esa: Option[Seq[TaskListSectionItem]] = {

      ua.get(JobseekersAllowancePage).map(_.toSeq) match {
        case Some(value) if value.contains(Esa) =>
          Some(Seq(TaskListSectionItem(TaskTitle(JobseekersAllowance.Esa.toString), TaskStatus(NotStarted.toString), Some(esaUrl))))
        case _ => None
      }
    }

    TaskListSection(EsaTitle.toString, esa)
  }


  private def jsaSection()(implicit ua: UserAnswers): TaskListSection = {

    val jsaUrl: String = ""

    def jsa: Option[Seq[TaskListSectionItem]] = {

      ua.get(JobseekersAllowancePage).map(_.toSeq) match {
        case Some(value) if value.contains(Esa) =>
          Some(Seq(TaskListSectionItem(TaskTitle(JobseekersAllowance.Jsa.toString), TaskStatus(NotStarted.toString), Some(jsaUrl))))
        case _ => None
      }
    }

    TaskListSection(JsaTitle.toString, jsa)
  }


  private def pensionsSection()(implicit ua: UserAnswers): TaskListSection = {

    val items = List(StatePension, OtherUkPensions, UnauthorisedPayments, ShortServiceRefunds, models.propertypensionsinvestments.Pensions.NonUkPensions)

    def pensionsUrl: Pensions => String = {
      case Pensions.StatePension => ""
      case Pensions.OtherUkPensions => ""
      case Pensions.UnauthorisedPayments => ""
      case Pensions.ShortServiceRefunds => ""
      case Pensions.NonUkPensions => ""
    }

    def pensions: Option[Seq[TaskListSectionItem]] = {

      ua.get(PensionsPage).map(_.toSeq) match {
        case Some(value) if !value.contains(Pensions.NoPensions) =>
          Some(items.intersect(value).map(k => TaskListSectionItem(TaskTitle(k.toString), TaskStatus(NotStarted.toString), Some(pensionsUrl(k)))))
        case _ => None
      }
    }

    TaskListSection(PensionsTitle.toString, pensions)
  }


  private def paymentsIntoPensionsSection()(implicit ua: UserAnswers): TaskListSection = {

    def paymentsIntoPensionsUrl: PaymentsIntoPensions => String = {
      case PaymentsIntoPensions.UkPensions => ""
      case PaymentsIntoPensions.NonUkPensions => ""
      case PaymentsIntoPensions.Overseas => ""
    }

    def paymentsIntoPensions: Option[Seq[TaskListSectionItem]] = {
      val links = List(UkPensions, models.pensions.PaymentsIntoPensions.NonUkPensions, Overseas)

      ua.get(PaymentsIntoPensionsPage).map(_.toSeq) match {
        case Some(value) if !value.contains(PaymentsIntoPensions.No) =>
          Some(links.intersect(value).map(k => TaskListSectionItem(TaskTitle(k.toString), TaskStatus(NotStarted.toString), Some(paymentsIntoPensionsUrl(k)))))
        case _ => None
      }
    }

    TaskListSection(PaymentsIntoPensionsTitle.toString, paymentsIntoPensions)
  }


  private def interestSection()(implicit ua: UserAnswers): TaskListSection = {

    def interestUrl: UkInterest => String = {
      case UkInterest.FromUkBanks => ""
      case UkInterest.FromUkTrustFunds => ""
      case UkInterest.FromGiltEdged => ""
    }

    def interest: Option[Seq[TaskListSectionItem]] = {

      val links = List(FromUkBanks, FromUkTrustFunds, FromGiltEdged)

      ua.get(UkInterestPage).map(_.toSeq) match {
        case Some(value) if !value.contains(UkInterest.NoInterest) =>
          Some(links.intersect(value).map(k => TaskListSectionItem(TaskTitle(k.toString), TaskStatus(NotStarted.toString), Some(interestUrl(k)))))
        case _ => None
      }
    }

    TaskListSection(InterestTitle.toString, interest)
  }


  private def dividendsSection()(implicit ua: UserAnswers): TaskListSection = {

    def dividendsUrl: UkDividendsSharesLoans => String = {
      case UkDividendsSharesLoans.CashDividendsFromUkStocksAndShares => ""
      case UkDividendsSharesLoans.StockDividendsFromUkCompanies => ""
      case UkDividendsSharesLoans.DividendsUnitTrustsInvestmentCompanies => ""
      case UkDividendsSharesLoans.FreeOrRedeemableShares => ""
      case UkDividendsSharesLoans.CloseCompanyLoansWrittenOffReleased => ""
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
          Some(links.intersect(value).map(k => TaskListSectionItem(TaskTitle(k.toString), TaskStatus(NotStarted.toString), Some(dividendsUrl(k)))))
        case _ => None
      }
    }

    TaskListSection(DividendsTitle.toString, dividends)
  }

}
