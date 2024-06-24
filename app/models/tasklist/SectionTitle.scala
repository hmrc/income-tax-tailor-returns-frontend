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

import enumeratum._
import models.PlayJsonEnum

sealed abstract class SectionTitle(override val entryName: String) extends EnumEntry {
  override def toString: String = entryName
}

object SectionTitle extends Enum[SectionTitle] with PlayJsonEnum[SectionTitle] {

  val values: IndexedSeq[SectionTitle] = findValues

  case object AboutYouTitle extends SectionTitle("AboutYou")

  case object CharitableDonationsTitle extends SectionTitle("CharitableDonations")

  case object EmploymentTitle extends SectionTitle("Employment")

  case object SelfEmploymentTitle extends SectionTitle("SelfEmployment")

  case object EsaTitle extends SectionTitle("Esa")

  case object JsaTitle extends SectionTitle("Jsa")

  case object PensionsTitle extends SectionTitle("Pensions")

  case object InsuranceGainsTitle extends SectionTitle("InsuranceGains")

  case object PaymentsIntoPensionsTitle extends SectionTitle("PaymentsIntoPensions")

  case object InterestTitle extends SectionTitle("Interest")

  case object DividendsTitle extends SectionTitle("Dividends")

}