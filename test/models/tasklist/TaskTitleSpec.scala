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
import play.api.libs.json.{JsPath, JsString, JsSuccess, Json}

class TaskTitleSpec extends AnyFreeSpec with Matchers {

  "TaskTitle" - {

    "must contain the correct values" in {
      TaskTitle.values mustEqual Seq[TaskTitle](
        UkResidenceStatus,
        FosterCarer,
        DonationsUsingGiftAid,
        GiftsOfLandOrProperty,
        GiftsOfShares,
        GiftsToOverseas,
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
        CloseCompanyLoans,
        UkProperty,
        ForeignProperty,
        UkForeignProperty
      )
    }

    "must parse each element as JSON successfully" - {

      TaskTitle.values.foreach { taskTitle =>
        s"for ${taskTitle.toString}" - {

          "serialize to JSON" in {
            Json.toJson(taskTitle) mustBe JsString(taskTitle.toString)
          }

          "deserialize from JSON" in {
            JsString(taskTitle.toString).validate[TaskTitle] mustBe JsSuccess(taskTitle, JsPath())
          }
        }
      }
    }
  }
}
