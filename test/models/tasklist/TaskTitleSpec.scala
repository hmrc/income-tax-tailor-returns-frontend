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

import models.tasklist.TaskTitle._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsValue, Json}

class TaskTitleSpec extends AnyFreeSpec with Matchers {

  "TaskTitle" - {

    "must contain the correct values" in {
      TaskTitle.values mustEqual Seq[TaskTitle](
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
        paymentsIntoPensionsTitles.PaymentsIntoUk(),
        paymentsIntoPensionsTitles.PaymentsIntoOverseas(),
        paymentsIntoPensionsTitles.AnnualAllowances(),
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
    }

    "must parse each element to jsValue successfully" in {
      val underTest: Seq[JsValue] = TaskTitle.values.map(x => Json.toJson(x))
      underTest.isInstanceOf[Seq[JsValue]] mustBe true
    }
  }
}
