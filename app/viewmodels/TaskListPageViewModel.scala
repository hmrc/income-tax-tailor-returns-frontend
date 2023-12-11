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

package viewmodels

import models.TagStatus.NotStarted
import models.UserAnswers
import models.aboutyou.CharitableDonations._
import models.aboutyou.UkResidenceStatus
import models.pensions.PaymentsIntoPensions._
import models.propertypensionsinvestments.Pensions.{NonUkPensions, _}
import models.propertypensionsinvestments.RentalIncome._
import models.propertypensionsinvestments.UkDividendsSharesLoans._
import models.propertypensionsinvestments.UkInsuranceGains._
import models.propertypensionsinvestments.UkInterest._
import models.workandbenefits.AboutYourWork._
import models.workandbenefits.JobseekersAllowance._
import pages.aboutyou._
import pages.pensions.PaymentsIntoPensionsPage
import pages.propertypensionsinvestments._
import pages.workandbenefits._
import play.api.i18n.Messages

case class TaskListPageViewModel(ua: UserAnswers, prefix: String)(implicit messages: Messages) {
  private def aboutYouSections: List[Task] = {

    def ukResidence: List[Task] =
      ua.get(UkResidenceStatusPage) match {
        case Some(UkResidenceStatus.Uk) => List(Task(Link(messages("ukResidenceStatus.heading"), "#"), NotStarted, ""))
        case Some(UkResidenceStatus.Domiciled) => List(Task(Link(messages("ukResidenceStatus.heading"), "#"), NotStarted, ""))
        case _ => Nil
      }

    def charitableDonations: List[Task] = {

      val links = List(
        DonationsUsingGiftAid,
        GiftsOfLandOrProperty,
        GiftsOfSharesOrSecurities
      )

      ua.get(CharitableDonationsPage).map(_.toSeq) match {
        case Some(value) => links.intersect(value).map(k => Task(Link(messages(k.toString), "#"), NotStarted, "charitableDonations"))
        case _ => Nil
      }
    }

    def fosterCarer: List[Task] = {
      ua.get(FosterCarerPage) match {
        case Some(value) if value => List[Task](Task(Link("Foster Carer", "#"), NotStarted, ""))
        case _ => Nil
      }
    }

    ukResidence ++ charitableDonations ++ fosterCarer
  }

  private def employmentSection: List[Task] = {

    ua.get(AboutYourWorkPage).map(_.toSeq) match {
      case Some(value) if value.contains(Employed) => List(Task(Link("employers", "#"), NotStarted, prefix))
      case _ => Nil
    }

  }

  private def selfEmploymentSection: List[Task] = {

    def selfEmployment: List[Task] =
      ua.get(AboutYourWorkPage).map(_.toSeq) match {
        case Some(value) if value.contains(SelfEmployed) => List(Task(Link("Check your self-employment details", "#"), NotStarted, ""))
        case _ => Nil
      }

    def cis: List[Task] =
      ua.get(ConstructionIndustrySchemePage) match {
        case Some(value) if value => List[Task](Task(Link("Review CIS", "#"), NotStarted, ""))
        case _ => Nil
      }

    selfEmployment ++ cis
  }

  private def esaSection: List[Task] = {
    ua.get(JobseekersAllowancePage).map(_.toSeq) match {
      case Some(value) if value.contains(Esa) => List(Task(Link("Review Jobseeker’s Allowance claims", "#"), NotStarted, ""))
      case _ => Nil
    }
  }

  private def jsaSection: List[Task] = {
    ua.get(JobseekersAllowancePage).map(_.toSeq) match {
      case Some(value) if value.contains(Jsa) => List(Task(Link("Review Jobseeker’s Allowance claims", "#"), NotStarted, ""))
      case _ => Nil
    }
  }

  private def ukPropertySection: List[Task] = {
    ua.get(RentalIncomePage).map(_.toSeq) match {
      case Some(value) if value.contains(Uk) => List(Task(Link("About UK property", "#"), NotStarted, ""))
      case _ => Nil
    }
  }

  private def nonUkPropertySection: List[Task] = {
    ua.get(RentalIncomePage).map(_.toSeq) match {
      case Some(value) if value.contains(NonUk) => List(Task(Link("About Foreign property", "#"), NotStarted, ""))
      case _ => Nil
    }
  }

  private def incomeFromPensionsSection: List[Task] = {

    val linkMappings = Map(StatePension -> "pensions.statePension",
      OtherUkPensions -> "pensions.otherUkPensions",
      UnauthorisedPayments -> "pensions.unauthorisedPayments",
      ShortServiceRefunds -> "pensions.shortServiceRefunds",
      NonUkPensions -> "pensions.nonUkPensions"
    )

    ua.get(PensionsPage).map(_.toSeq) match {
      case Some(value) => linkMappings.keySet.toList.intersect(value).map(k => Task(Link(messages(linkMappings(k)), "#"), NotStarted, ""))
      case _ => Nil
    }
  }

  private def ukInsuranceGainsSection: List[Task] = {

    val linkMappings = Map(LifeInsurance -> "ukInsuranceGains.lifeInsurance",
      LifeAnnuity -> "ukInsuranceGains.lifeAnnuity",
      CapitalRedemption -> "ukInsuranceGains.capitalRedemption",
      VoidedISA -> "ukInsuranceGains.voidedISA"
    )

    ua.get(UkInsuranceGainsPage).map(_.toSeq) match {
      case Some(value) => linkMappings.keySet.toList.intersect(value).map(k => Task(Link(messages(linkMappings(k)), "#"), NotStarted, ""))
      case _ => Nil
    }
  }

  private def ukInterestSection: List[Task] = {

    val linkMappings = Map(FromUkBanks -> "ukInterest.fromUkBanks",
      FromUkTrustFunds -> "ukInterest.fromUkTrustFunds",
      FromGiltEdged -> "ukInterest.fromGiltEdged"
    )

    ua.get(UkInterestPage).map(_.toSeq) match {
      case Some(value) => linkMappings.keySet.toList.intersect(value).map(k => Task(Link(messages(linkMappings(k)), "#"), NotStarted, ""))
      case _ => Nil
    }
  }

  private def ukDividendsSection: List[Task] = {

    val linkMappings = Map(CashDividendsFromUkStocksAndShares -> "ukDividendsSharesLoans.cashDividendsUkStocksAndShares",
      StockDividendsFromUkCompanies -> "ukDividendsSharesLoans.stockDividendsUkCompanies",
      DividendsUnitTrustsInvestmentCompanies -> "ukDividendsSharesLoans.dividendsUnitTrustsInvestmentCompanies",
      FreeOrRedeemableShares -> "ukDividendsSharesLoans.freeOrRedeemableShares",
      CloseCompanyLoansWrittenOffReleased -> "ukDividendsSharesLoans.closeCompanyLoansWrittenOffReleased"
    )

    ua.get(UkDividendsSharesLoansPage).map(_.toSeq) match {
      case Some(value) => linkMappings.keySet.toList.intersect(value).map(k => Task(Link(messages(linkMappings(k)), "#"), NotStarted, ""))
      case _ => Nil
    }
  }

  private def paymentsIntoPensionsSection: List[Task] = {

    val linkMappings = Map(UkPensions -> "paymentsIntoPensions.ukPensions",
      models.pensions.PaymentsIntoPensions.NonUkPensions -> "paymentsIntoPensions.nonUkPensions",
      Overseas -> "paymentsIntoPensions.overseas"
    )

    ua.get(PaymentsIntoPensionsPage).map(_.toSeq) match {
      case Some(value) => linkMappings.keySet.toList.intersect(value).map(k => Task(Link(messages(linkMappings(k)), "#"), NotStarted, ""))
      case _ => Nil
    }
  }

  def getSections: List[(String, List[Task])] = {

    List(
      (messages("addSections.aboutYou"), aboutYouSections),
      ("All Employment", employmentSection),
      (messages("aboutYourWork.selfEmployed"), selfEmploymentSection),
      ("Employment and Support Allowance", esaSection),
      (messages("jobseekersAllowance.jsa"), jsaSection),
      (messages("rentalIncome.uk"), ukPropertySection),
      (messages("rentalIncome.nonUk"), nonUkPropertySection),
      (messages("pensions.title"), incomeFromPensionsSection),
      (messages("ukInsuranceGains.title"), ukInsuranceGainsSection),
      (messages("ukInterest.title"), ukInterestSection),
      (messages("ukDividendsSharesLoans.title"), ukDividendsSection),
      (messages("paymentsIntoPensions.title"), paymentsIntoPensionsSection)
    ).filterNot(_._2 == List[Task]())

  }

}
