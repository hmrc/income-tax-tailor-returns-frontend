/*
 * Copyright 2023 HM Revenue & Customs
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

package generators

import models._
import models.aboutyou._
import org.scalacheck.{Arbitrary, Gen}

trait ModelGenerators {

  implicit lazy val arbitraryTaxAvoidanceSchemes: Arbitrary[TaxAvoidanceSchemes] =
    Arbitrary {
      Gen.oneOf(TaxAvoidanceSchemes.values)
    }

  implicit lazy val arbitraryHighIncomeChildBenefitCharge: Arbitrary[HighIncomeChildBenefitCharge] =
    Arbitrary {
      Gen.oneOf(HighIncomeChildBenefitCharge.values.toSeq)
    }

  implicit lazy val arbitraryCharitableDonations: Arbitrary[CharitableDonations] =
    Arbitrary {
      Gen.oneOf(CharitableDonations.values)
    }

  implicit lazy val arbitraryYourResidenceStatus: Arbitrary[YourResidenceStatus] =
    Arbitrary {
      Gen.oneOf(YourResidenceStatus.values.toSeq)
    }

  implicit lazy val arbitraryUkResidenceStatus: Arbitrary[UkResidenceStatus] =
    Arbitrary {
      Gen.oneOf(UkResidenceStatus.values.toSeq)
    }


}
