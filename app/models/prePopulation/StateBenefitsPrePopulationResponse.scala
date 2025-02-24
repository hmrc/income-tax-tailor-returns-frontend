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

import models.workandbenefits.JobseekersAllowance
import play.api.libs.json.{Json, Reads}

case class StateBenefitsPrePopulationResponse (hasEsaPrePop: Boolean,
                                               hasJsaPrePop: Boolean)
  extends PrePopulationResponse[Set[JobseekersAllowance]] {
  val hasStateBenefits: Boolean = hasEsaPrePop || hasJsaPrePop

  def stateBenefitsMessageString(isAgent: Boolean): String = {
    val agentStringOpt = if (isAgent) "agent." else ""

    (hasEsaPrePop, hasJsaPrePop) match {
      case (true, true) => s"jobseekersAllowance.insetText.${agentStringOpt}both"
      case (true, false) => s"jobseekersAllowance.insetText.${agentStringOpt}esa"
      case (false, true) => s"jobseekersAllowance.insetText.${agentStringOpt}jsa"
      case _ => ""
    }
  }

  def toPageModel: Set[JobseekersAllowance] =
    (if (hasEsaPrePop) Set(JobseekersAllowance.Esa) else Set()) ++
      (if (hasJsaPrePop) Set(JobseekersAllowance.Jsa) else Set())
}

object StateBenefitsPrePopulationResponse {
  implicit val reads: Reads[StateBenefitsPrePopulationResponse] = Json.reads[StateBenefitsPrePopulationResponse]

  val empty: StateBenefitsPrePopulationResponse = StateBenefitsPrePopulationResponse(
    hasEsaPrePop = false,
    hasJsaPrePop = false
  )
}
