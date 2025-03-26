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

import models.propertypensionsinvestments.RentalIncome
import play.api.libs.json.{Json, Reads}

case class PropertyPrePopulationResponse (hasUkPropertyPrePop: Boolean,
                                          hasForeignPropertyPrePop: Boolean) extends PrePopulationResponse[Set[RentalIncome]] {

  override def toPageModel: Set[RentalIncome] =
    (if (hasUkPropertyPrePop) Set(RentalIncome.Uk) else Set()) ++
      (if (hasForeignPropertyPrePop) Set(RentalIncome.NonUk) else Set())

  override def toMessageString(isAgent:  Boolean): String = ""

  override val hasPrePop: Boolean = hasUkPropertyPrePop || hasForeignPropertyPrePop
}

object PropertyPrePopulationResponse {
  implicit val reads: Reads[PropertyPrePopulationResponse] = Json.reads[PropertyPrePopulationResponse]

  val empty: PropertyPrePopulationResponse = PropertyPrePopulationResponse(
    hasUkPropertyPrePop = false,
    hasForeignPropertyPrePop = false
  )
}
