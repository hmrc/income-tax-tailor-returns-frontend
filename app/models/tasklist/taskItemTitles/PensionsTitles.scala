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


object PensionsTitles {

  case class StatePension() extends WithName("StatePensionTitle") with TaskTitle
  object StatePension extends ReadsWrites[StatePension]

  case class OtherUkPensions() extends WithName("OtherUkPensionsTitle") with TaskTitle
  object OtherUkPensions extends ReadsWrites[OtherUkPensions]

  case class UnauthorisedPayments() extends WithName("UnauthorisedPaymentsTitle") with TaskTitle
  object UnauthorisedPayments extends ReadsWrites[UnauthorisedPayments]

  case class ShortServiceRefunds() extends WithName("ShortServiceRefundsTitle") with TaskTitle
  object ShortServiceRefunds extends ReadsWrites[ShortServiceRefunds]

  case class IncomeFromOverseas() extends WithName("IncomeFromOverseasTitle") with TaskTitle
  object IncomeFromOverseas extends ReadsWrites[IncomeFromOverseas]

}
