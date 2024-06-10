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

package models.tasklist.taskItemTitles

import models.WithName
import models.tasklist.TaskTitle
import play.api.libs.json.{Json, OWrites, Reads}

object UkDividendsTitles {

  case class CashDividends() extends WithName("CashDividendsTitle") with TaskTitle
  object CashDividends {
    implicit val nonStrictReads: Reads[CashDividends] = Reads.pure(CashDividends())
    implicit val writes: OWrites[CashDividends] = OWrites[CashDividends](_ => Json.obj())
  }

  case class StockDividends() extends WithName("StockDividendsTitle") with TaskTitle
  object StockDividends {
    implicit val nonStrictReads: Reads[StockDividends] = Reads.pure(StockDividends())
    implicit val writes: OWrites[StockDividends] = OWrites[StockDividends](_ => Json.obj())
  }

  case class DividendsFromUnitTrusts() extends WithName("DividendsFromUnitTrustsTitle") with TaskTitle
  object DividendsFromUnitTrusts {
    implicit val nonStrictReads: Reads[DividendsFromUnitTrusts] = Reads.pure(DividendsFromUnitTrusts())
    implicit val writes: OWrites[DividendsFromUnitTrusts] = OWrites[DividendsFromUnitTrusts](_ => Json.obj())
  }

  case class FreeRedeemableShares() extends WithName("FreeRedeemableSharesTitle") with TaskTitle
  object FreeRedeemableShares {
    implicit val nonStrictReads: Reads[FreeRedeemableShares] = Reads.pure(FreeRedeemableShares())
    implicit val writes: OWrites[FreeRedeemableShares] = OWrites[FreeRedeemableShares](_ => Json.obj())
  }

  case class CloseCompanyLoans() extends WithName("CloseCompanyLoansTitle") with TaskTitle
  object CloseCompanyLoans {
    implicit val nonStrictReads: Reads[CloseCompanyLoans] = Reads.pure(CloseCompanyLoans())
    implicit val writes: OWrites[CloseCompanyLoans] = OWrites[CloseCompanyLoans](_ => Json.obj())
  }

}
