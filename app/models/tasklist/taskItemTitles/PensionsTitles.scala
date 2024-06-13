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


object PensionsTitles {

  case class StatePension() extends WithName("StatePensionTitle") with TaskTitle
  object StatePension {
    implicit val nonStrictReads: Reads[StatePension] = Reads.pure(StatePension())
    implicit val writes: OWrites[StatePension] = OWrites[StatePension](_ => Json.obj())
  }

  case class OtherUkPensions() extends WithName("OtherUkPensionsTitle") with TaskTitle
  object OtherUkPensions {
    implicit val nonStrictReads: Reads[OtherUkPensions] = Reads.pure(OtherUkPensions())
    implicit val writes: OWrites[OtherUkPensions] = OWrites[OtherUkPensions](_ => Json.obj())
  }

  case class UnauthorisedPayments() extends WithName("UnauthorisedPaymentsTitle") with TaskTitle
  object UnauthorisedPayments {
    implicit val nonStrictReads: Reads[UnauthorisedPayments] = Reads.pure(UnauthorisedPayments())
    implicit val writes: OWrites[UnauthorisedPayments] = OWrites[UnauthorisedPayments](_ => Json.obj())
  }

  case class ShortServiceRefunds() extends WithName("ShortServiceRefundsTitle") with TaskTitle
  object ShortServiceRefunds {
    implicit val nonStrictReads: Reads[ShortServiceRefunds] = Reads.pure(ShortServiceRefunds())
    implicit val writes: OWrites[ShortServiceRefunds] = OWrites[ShortServiceRefunds](_ => Json.obj())
  }

  case class IncomeFromOverseas() extends WithName("IncomeFromOverseasTitle") with TaskTitle
  object IncomeFromOverseas {
    implicit val nonStrictReads: Reads[IncomeFromOverseas] = Reads.pure(IncomeFromOverseas())
    implicit val writes: OWrites[IncomeFromOverseas] = OWrites[IncomeFromOverseas](_ => Json.obj())
  }

}
