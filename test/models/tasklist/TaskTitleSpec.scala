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
import play.api.libs.json.{JsPath, JsSuccess, JsValue, Json}

class TaskTitleSpec extends AnyFreeSpec with Matchers {

  "TaskTitle" - {

    "must contain the correct values" in {
      TaskTitle.values mustEqual Seq[TaskTitle](
        UkResidenceStatus,
        FosterCarer,
        DonationsUsingGiftAid,
        GiftsOfLandOrProperty,
        GiftsOfShares,
        PayeEmployment,
        CIS,
        ESA,
        JSA,
        StatePension,
        OtherUkPensions,
        UnauthorisedPayments,
        ShortServiceRefunds,
        IncomeFromOverseas,
        LifeInsurance,
        LifeAnnuity,
        CapitalRedemption,
        VoidedISA,
        PaymentsIntoUk,
        AnnualAllowances,
        PaymentsIntoOverseas,
        OverseasTransfer,
        BanksAndBuilding,
        TrustFundBond,
        GiltEdged,
        CashDividends,
        StockDividends,
        DividendsFromUnitTrusts,
        FreeRedeemableShares,
        CloseCompanyLoans
      )
    }

    "must parse each element to jsValue successfully" in {
      val underTest: Seq[JsValue] = TaskTitle.values.map(x => Json.toJson(x))
      underTest.isInstanceOf[Seq[JsValue]] mustBe true
    }
  }

  "UkResidenceStatus" - {

    "must parse to and from json" in {
      val underTest = Json.toJson(UkResidenceStatus)

      underTest.toString() mustBe s"\"${UkResidenceStatus.toString}\""
      underTest.validate[TaskTitle] mustBe JsSuccess(UkResidenceStatus, JsPath())
    }
  }

  "FosterCarer" - {

    "must parse to and from json" in {
      val underTest = Json.toJson(FosterCarer)

      underTest.toString() mustBe s"\"${FosterCarer.toString}\""
      underTest.validate[TaskTitle] mustBe JsSuccess(FosterCarer, JsPath())
    }
  }

  "DonationsUsingGiftAid" - {

    "must parse to and from json" in {
      val underTest = Json.toJson(DonationsUsingGiftAid)

      underTest.toString() mustBe s"\"${DonationsUsingGiftAid.toString}\""
      underTest.validate[TaskTitle] mustBe JsSuccess(DonationsUsingGiftAid, JsPath())
    }
  }

  "GiftsOfLandOrProperty" - {

    "must parse to and from json" in {
      val underTest = Json.toJson(GiftsOfLandOrProperty)

      underTest.toString() mustBe s"\"${GiftsOfLandOrProperty.toString}\""
      underTest.validate[TaskTitle] mustBe JsSuccess(GiftsOfLandOrProperty, JsPath())
    }
  }

  "GiftsOfShares" - {

    "must parse to and from json" in {
      val underTest = Json.toJson(GiftsOfShares)

      underTest.toString() mustBe s"\"${GiftsOfShares.toString}\""
      underTest.validate[TaskTitle] mustBe JsSuccess(GiftsOfShares, JsPath())
    }
  }

  "PayeEmployment" - {

    "must parse to and from json" in {
      val underTest = Json.toJson(PayeEmployment)

      underTest.toString() mustBe s"\"${PayeEmployment.toString}\""
      underTest.validate[TaskTitle] mustBe JsSuccess(PayeEmployment, JsPath())
    }
  }

  "CIS" - {

    "must parse to and from json" in {
      val underTest = Json.toJson(CIS)

      underTest.toString() mustBe s"\"${CIS.toString}\""
      underTest.validate[TaskTitle] mustBe JsSuccess(CIS, JsPath())
    }
  }

  "ESA" - {

    "must parse to and from json" in {
      val underTest = Json.toJson(ESA)

      underTest.toString() mustBe s"\"${ESA.toString}\""
      underTest.validate[TaskTitle] mustBe JsSuccess(ESA, JsPath())
    }
  }

  "JSA" - {

    "must parse to and from json" in {
      val underTest = Json.toJson(JSA)

      underTest.toString() mustBe s"\"${JSA.toString}\""
      underTest.validate[TaskTitle] mustBe JsSuccess(JSA, JsPath())
    }
  }

  "StatePension" - {

    "must parse to and from json" in {
      val underTest = Json.toJson(StatePension)

      underTest.toString() mustBe s"\"${StatePension.toString}\""
      underTest.validate[TaskTitle] mustBe JsSuccess(StatePension, JsPath())
    }
  }

  "OtherUkPensions" - {

    "must parse to and from json" in {
      val underTest = Json.toJson(OtherUkPensions)

      underTest.toString() mustBe s"\"${OtherUkPensions.toString}\""
      underTest.validate[TaskTitle] mustBe JsSuccess(OtherUkPensions, JsPath())
    }
  }

  "UnauthorisedPayments" - {

    "must parse to and from json" in {
      val underTest = Json.toJson(UnauthorisedPayments)

      underTest.toString() mustBe s"\"${UnauthorisedPayments.toString}\""
      underTest.validate[TaskTitle] mustBe JsSuccess(UnauthorisedPayments, JsPath())
    }
  }

  "ShortServiceRefunds" - {

    "must parse to and from json" in {
      val underTest = Json.toJson(ShortServiceRefunds)

      underTest.toString() mustBe s"\"${ShortServiceRefunds.toString}\""
      underTest.validate[TaskTitle] mustBe JsSuccess(ShortServiceRefunds, JsPath())
    }
  }

  "IncomeFromOverseas" - {

    "must parse to and from json" in {
      val underTest = Json.toJson(IncomeFromOverseas)

      underTest.toString() mustBe s"\"${IncomeFromOverseas.toString}\""
      underTest.validate[TaskTitle] mustBe JsSuccess(IncomeFromOverseas, JsPath())
    }
  }

  "LifeInsurance" - {

    "must parse to and from json" in {
      val underTest = Json.toJson(LifeInsurance)

      underTest.toString() mustBe s"\"${LifeInsurance.toString}\""
      underTest.validate[TaskTitle] mustBe JsSuccess(LifeInsurance, JsPath())
    }
  }

  "LifeAnnuity" - {

    "must parse to and from json" in {
      val underTest = Json.toJson(LifeAnnuity)

      underTest.toString() mustBe s"\"${LifeAnnuity.toString}\""
      underTest.validate[TaskTitle] mustBe JsSuccess(LifeAnnuity, JsPath())
    }
  }

  "CapitalRedemption" - {

    "must parse to and from json" in {
      val underTest = Json.toJson(CapitalRedemption)

      underTest.toString() mustBe s"\"${CapitalRedemption.toString}\""
      underTest.validate[TaskTitle] mustBe JsSuccess(CapitalRedemption, JsPath())
    }
  }

  "VoidedISA" - {

    "must parse to and from json" in {
      val underTest = Json.toJson(VoidedISA)

      underTest.toString() mustBe s"\"${VoidedISA.toString}\""
      underTest.validate[TaskTitle] mustBe JsSuccess(VoidedISA, JsPath())
    }
  }

  "PaymentsIntoUk" - {

    "must parse to and from json" in {
      val underTest = Json.toJson(PaymentsIntoUk)

      underTest.toString() mustBe s"\"${PaymentsIntoUk.toString}\""
      underTest.validate[TaskTitle] mustBe JsSuccess(PaymentsIntoUk, JsPath())
    }
  }

  "AnnualAllowances" - {

    "must parse to and from json" in {
      val underTest = Json.toJson(AnnualAllowances)

      underTest.toString() mustBe s"\"${AnnualAllowances.toString}\""
      underTest.validate[TaskTitle] mustBe JsSuccess(AnnualAllowances, JsPath())
    }
  }

  "PaymentsIntoOverseas" - {

    "must parse to and from json" in {
      val underTest = Json.toJson(PaymentsIntoOverseas)

      underTest.toString() mustBe s"\"${PaymentsIntoOverseas.toString}\""
      underTest.validate[TaskTitle] mustBe JsSuccess(PaymentsIntoOverseas, JsPath())
    }
  }

  "OverseasTransfer" - {

    "must parse to and from json" in {
      val underTest = Json.toJson(OverseasTransfer)

      underTest.toString() mustBe s"\"${OverseasTransfer.toString}\""
      underTest.validate[TaskTitle] mustBe JsSuccess(OverseasTransfer, JsPath())
    }
  }

  "BanksAndBuilding" - {

    "must parse to and from json" in {
      val underTest = Json.toJson(BanksAndBuilding)

      underTest.toString() mustBe s"\"${BanksAndBuilding.toString}\""
      underTest.validate[TaskTitle] mustBe JsSuccess(BanksAndBuilding, JsPath())
    }
  }

  "TrustFundBond" - {

    "must parse to and from json" in {
      val underTest = Json.toJson(TrustFundBond)

      underTest.toString() mustBe s"\"${TrustFundBond.toString}\""
      underTest.validate[TaskTitle] mustBe JsSuccess(TrustFundBond, JsPath())
    }
  }

  "GiltEdged" - {

    "must parse to and from json" in {
      val underTest = Json.toJson(GiltEdged)

      underTest.toString() mustBe s"\"${GiltEdged.toString}\""
      underTest.validate[TaskTitle] mustBe JsSuccess(GiltEdged, JsPath())
    }
  }

  "CashDividends" - {

    "must parse to and from json" in {
      val underTest = Json.toJson(CashDividends)

      underTest.toString() mustBe s"\"${CashDividends.toString}\""
      underTest.validate[TaskTitle] mustBe JsSuccess(CashDividends, JsPath())
    }
  }

  "StockDividends" - {

    "must parse to and from json" in {
      val underTest = Json.toJson(StockDividends)

      underTest.toString() mustBe s"\"${StockDividends.toString}\""
      underTest.validate[TaskTitle] mustBe JsSuccess(StockDividends, JsPath())
    }
  }

  "DividendsFromUnitTrusts" - {

    "must parse to and from json" in {
      val underTest = Json.toJson(DividendsFromUnitTrusts)

      underTest.toString() mustBe s"\"${DividendsFromUnitTrusts.toString}\""
      underTest.validate[TaskTitle] mustBe JsSuccess(DividendsFromUnitTrusts, JsPath())
    }
  }

  "FreeRedeemableShares" - {

    "must parse to and from json" in {
      val underTest = Json.toJson(FreeRedeemableShares)

      underTest.toString() mustBe s"\"${FreeRedeemableShares.toString}\""
      underTest.validate[TaskTitle] mustBe JsSuccess(FreeRedeemableShares, JsPath())
    }
  }

  "CloseCompanyLoans" - {

    "must parse to and from json" in {
      val underTest = Json.toJson(CloseCompanyLoans)

      underTest.toString() mustBe s"\"${CloseCompanyLoans.toString}\""
      underTest.validate[TaskTitle] mustBe JsSuccess(CloseCompanyLoans, JsPath())
    }
  }
}
