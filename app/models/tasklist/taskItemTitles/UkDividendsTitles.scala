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
import models.tasklist.{ReadsWrites, TaskTitle}

object UkDividendsTitles {

  case class CashDividends() extends WithName("CashDividendsTitle") with TaskTitle
  object CashDividends extends ReadsWrites[CashDividends]

  case class StockDividends() extends WithName("StockDividendsTitle") with TaskTitle
  object StockDividends extends ReadsWrites[StockDividends]

  case class DividendsFromUnitTrusts() extends WithName("DividendsFromUnitTrustsTitle") with TaskTitle
  object DividendsFromUnitTrusts extends ReadsWrites[DividendsFromUnitTrusts]

  case class FreeRedeemableShares() extends WithName("FreeRedeemableSharesTitle") with TaskTitle
  object FreeRedeemableShares extends ReadsWrites[FreeRedeemableShares]

  case class CloseCompanyLoans() extends WithName("CloseCompanyLoansTitle") with TaskTitle
  object CloseCompanyLoans extends ReadsWrites[CloseCompanyLoans]

}
