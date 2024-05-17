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

import models.{Enumerable, WithName}

sealed trait SectionTitle extends Enumerable.Implicits

object SectionTitle extends SectionTitle {
  case object AboutYouTitle extends WithName("AboutYou") with SectionTitle
  case object CharitableDonationsTitle extends WithName("CharitableDonations") with SectionTitle
  case object EmploymentTitle extends WithName("Employment") with SectionTitle
  case object SelfEmploymentTitle extends WithName("SelfEmployment") with SectionTitle
  case object EsaTitle extends WithName("Esa") with SectionTitle
  case object JsaTitle extends WithName("Jsa") with SectionTitle
  case object PensionsTitle extends WithName("Pensions") with SectionTitle
  case object PaymentsIntoPensionsTitle extends WithName("PaymentsIntoPensions") with SectionTitle
  case object InterestTitle extends WithName("Interest") with SectionTitle
  case object DividendsTitle extends WithName("Dividends") with SectionTitle
}
