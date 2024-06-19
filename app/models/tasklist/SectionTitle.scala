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

trait SectionTitle extends Enumerable.Implicits

object SectionTitle extends SectionTitle {

  case class AboutYouTitle() extends WithName("AboutYou") with SectionTitle
  object AboutYouTitle extends ReadsWrites[AboutYouTitle]

  case class CharitableDonationsTitle() extends WithName("CharitableDonations") with SectionTitle
  object CharitableDonationsTitle extends ReadsWrites[CharitableDonationsTitle]

  case class EmploymentTitle() extends WithName("Employment") with SectionTitle
  object EmploymentTitle extends ReadsWrites[EmploymentTitle]

  case class SelfEmploymentTitle() extends WithName("SelfEmployment") with SectionTitle
  object SelfEmploymentTitle extends ReadsWrites[SelfEmploymentTitle]

  case class EsaTitle() extends WithName("Esa") with SectionTitle
  object EsaTitle extends ReadsWrites[EsaTitle]

  case class JsaTitle() extends WithName("Jsa") with SectionTitle
  object JsaTitle extends ReadsWrites[JsaTitle]

  case class PensionsTitle() extends WithName("Pensions") with SectionTitle
  object PensionsTitle extends ReadsWrites[PensionsTitle]

  case class PaymentsIntoPensionsTitle() extends WithName("PaymentsIntoPensions") with SectionTitle
  object PaymentsIntoPensionsTitle extends ReadsWrites[PaymentsIntoPensionsTitle]

  case class InterestTitle() extends WithName("Interest") with SectionTitle
  object InterestTitle extends ReadsWrites[InterestTitle]

  case class DividendsTitle() extends WithName("Dividends") with SectionTitle
  object DividendsTitle extends ReadsWrites[DividendsTitle]

  case class InsuranceGainsTitle() extends WithName("InsuranceGains") with SectionTitle
  object InsuranceGainsTitle extends ReadsWrites[InsuranceGainsTitle]

  val values: Seq[SectionTitle] = Seq(
    AboutYouTitle(),
    CharitableDonationsTitle(),
    EmploymentTitle(),
    SelfEmploymentTitle(),
    EsaTitle(),
    JsaTitle(),
    PensionsTitle(),
    PaymentsIntoPensionsTitle(),
    InterestTitle(),
    DividendsTitle(),
    InsuranceGainsTitle()
  )

  implicit val enumerable: Enumerable[SectionTitle] =
    Enumerable(values.map(v => v.toString -> v): _*)

}