/*
 * Copyright 2025 HM Revenue & Customs
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

package models.prePopulation

import models.prePopulation.EmploymentPrePopulationResponse.{EmploymentPrePop, EmploymentRadioPrePop}
import models.workandbenefits.AboutYourWork
import play.api.libs.json.{Json, Reads}

case class EmploymentPrePopulationResponse(hasEmploymentPrePop: Boolean) {
  val hasPrePop: Boolean = hasEmploymentPrePop

  def toPrePopRadioModel: EmploymentRadioPrePop = new EmploymentRadioPrePop(hasPrePop)
  def toPrePopModel: EmploymentPrePop = new EmploymentPrePop(hasPrePop)
}

object EmploymentPrePopulationResponse {
  implicit val reads: Reads[EmploymentPrePopulationResponse] = Json.reads[EmploymentPrePopulationResponse]

  val empty: EmploymentPrePopulationResponse = EmploymentPrePopulationResponse(hasEmploymentPrePop = false)

  class EmploymentRadioPrePop(hasEmploymentPrePop: Boolean) extends EmploymentPrePopulationResponse(hasEmploymentPrePop)
    with PrePopulationResponse[Boolean] {

    override def toPageModel: Boolean = hasEmploymentPrePop

    override def toMessageString(isAgent: Boolean): String = ""
  }

  object EmploymentRadioPrePop {
    val empty: EmploymentRadioPrePop = new EmploymentRadioPrePop(hasEmploymentPrePop = false)
  }

  class EmploymentPrePop(hasEmploymentPrePop: Boolean) extends EmploymentPrePopulationResponse(hasEmploymentPrePop)
    with PrePopulationResponse[Set[AboutYourWork]] {

    override def toPageModel: Set[AboutYourWork] = ??? //TODO: Figure this out

    override def toMessageString(isAgent: Boolean): String = ""
  }

  object EmploymentPrePop {
    val empty: EmploymentPrePop = new EmploymentPrePop(hasEmploymentPrePop = false)
  }
}
