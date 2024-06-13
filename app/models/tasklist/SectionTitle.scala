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
import play.api.libs.json.{Json, OWrites, Reads}

trait SectionTitle extends Enumerable.Implicits


object SectionTitle extends SectionTitle {

  case class AboutYouTitle() extends WithName("AboutYou") with SectionTitle
  object AboutYouTitle {
    implicit val nonStrictReads: Reads[AboutYouTitle] = Reads.pure(AboutYouTitle())
    implicit val writes: OWrites[AboutYouTitle] = OWrites[AboutYouTitle](_ => Json.obj())
  }

  case class CharitableDonationsTitle() extends WithName("CharitableDonations") with SectionTitle
  object CharitableDonationsTitle {
    implicit val nonStrictReads: Reads[CharitableDonationsTitle] = Reads.pure(CharitableDonationsTitle())
    implicit val writes: OWrites[CharitableDonationsTitle] = OWrites[CharitableDonationsTitle](_ => Json.obj())
  }

  case class EmploymentTitle() extends WithName("Employment") with SectionTitle
  object EmploymentTitle {
    implicit val nonStrictReads: Reads[EmploymentTitle] = Reads.pure(EmploymentTitle())
    implicit val writes: OWrites[EmploymentTitle] = OWrites[EmploymentTitle](_ => Json.obj())
  }

  case class SelfEmploymentTitle() extends WithName("SelfEmployment") with SectionTitle
  object SelfEmploymentTitle {
    implicit val nonStrictReads: Reads[SelfEmploymentTitle] = Reads.pure(SelfEmploymentTitle())
    implicit val writes: OWrites[SelfEmploymentTitle] = OWrites[SelfEmploymentTitle](_ => Json.obj())
  }

  case class EsaTitle() extends WithName("Esa") with SectionTitle
  object EsaTitle {
    implicit val nonStrictReads: Reads[EsaTitle] = Reads.pure(EsaTitle())
    implicit val writes: OWrites[EsaTitle] = OWrites[EsaTitle](_ => Json.obj())
  }

  case class JsaTitle() extends WithName("Jsa") with SectionTitle
  object JsaTitle {
    implicit val nonStrictReads: Reads[JsaTitle] = Reads.pure(JsaTitle())
    implicit val writes: OWrites[JsaTitle] = OWrites[JsaTitle](_ => Json.obj())
  }

  case class PensionsTitle() extends WithName("Pensions") with SectionTitle
  object PensionsTitle {
    implicit val nonStrictReads: Reads[PensionsTitle] = Reads.pure(PensionsTitle())
    implicit val writes: OWrites[PensionsTitle] = OWrites[PensionsTitle](_ => Json.obj())
  }

  case class PaymentsIntoPensionsTitle() extends WithName("PaymentsIntoPensions") with SectionTitle
  object PaymentsIntoPensionsTitle {
    implicit val nonStrictReads: Reads[PaymentsIntoPensionsTitle] = Reads.pure(PaymentsIntoPensionsTitle())
    implicit val writes: OWrites[PaymentsIntoPensionsTitle] = OWrites[PaymentsIntoPensionsTitle](_ => Json.obj())
  }

  case class InterestTitle() extends WithName("Interest") with SectionTitle
  object InterestTitle {
    implicit val nonStrictReads: Reads[InterestTitle] = Reads.pure(InterestTitle())
    implicit val writes: OWrites[InterestTitle] = OWrites[InterestTitle](_ => Json.obj())
  }

  case class DividendsTitle() extends WithName("Dividends") with SectionTitle
  object DividendsTitle {
    implicit val nonStrictReads: Reads[DividendsTitle] = Reads.pure(DividendsTitle())
    implicit val writes: OWrites[DividendsTitle] = OWrites[DividendsTitle](_ => Json.obj())
  }

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
    DividendsTitle()
  )

  implicit val enumerable: Enumerable[SectionTitle] =
    Enumerable(values.map(v => v.toString -> v): _*)

}