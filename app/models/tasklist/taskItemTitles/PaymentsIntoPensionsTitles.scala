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

object PaymentsIntoPensionsTitles {

  case class PaymentsIntoUk() extends WithName("PaymentsIntoUkTitle") with TaskTitle
  object PaymentsIntoUk {
    implicit val nonStrictReads: Reads[PaymentsIntoUk] = Reads.pure(PaymentsIntoUk())
    implicit val writes: OWrites[PaymentsIntoUk] = OWrites[PaymentsIntoUk](_ => Json.obj())
  }

  case class PaymentsIntoOverseas() extends WithName("PaymentsIntoOverseasTitle") with TaskTitle
  object PaymentsIntoOverseas {
    implicit val nonStrictReads: Reads[PaymentsIntoOverseas] = Reads.pure(PaymentsIntoOverseas())
    implicit val writes: OWrites[PaymentsIntoOverseas] = OWrites[PaymentsIntoOverseas](_ => Json.obj())
  }

  case class OverseasTransfer() extends WithName("OverseasTransferTitle") with TaskTitle
  object OverseasTransfer {
    implicit val nonStrictReads: Reads[OverseasTransfer] = Reads.pure(OverseasTransfer())
    implicit val writes: OWrites[OverseasTransfer] = OWrites[OverseasTransfer](_ => Json.obj())
  }

}
