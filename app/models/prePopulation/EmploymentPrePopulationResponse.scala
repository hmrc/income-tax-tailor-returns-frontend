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

import play.api.libs.json.{Json, Reads}

case class EmploymentPrePopulationResponse (hasEmployment: Boolean) extends PrePopulationResponse {

  val hasEmployments: Boolean = hasEmployment

  def employmentMessageString(isAgent: Boolean): String = {
    val agentStringOpt = if (isAgent) "agent." else ""

    (hasEmployments) match {
      case (true) => s"employmentLumpSums.insetText.${agentStringOpt}employment"
      case _ => ""
    }

  }
}

object EmploymentPrePopulationResponse {
  implicit val reads: Reads[EmploymentPrePopulationResponse] = Json.reads[EmploymentPrePopulationResponse]

  val empty: EmploymentPrePopulationResponse = EmploymentPrePopulationResponse(
    hasEmployment = false,
  )
}
