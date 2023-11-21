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

import models.pensions._
import models.aboutyou._
import models.propertypensionsinvestments._
import models.workandbenefits.{AboutYourWork, JobseekersAllowance}
import org.scalacheck.{Arbitrary, Gen}

trait ModelGenerators {

  implicit lazy val arbitraryUkDividendsSharesAndLoansFromLimitedCompanies: Arbitrary[UkDividendsSharesAndLoansFromLimitedCompanies] =
    Arbitrary {
      Gen.oneOf(UkDividendsSharesAndLoansFromLimitedCompanies.values)
    }

  implicit lazy val arbitraryJobseekersAllowance: Arbitrary[JobseekersAllowance] =
    Arbitrary {
      Gen.oneOf(JobseekersAllowance.values)
    }

  implicit lazy val arbitraryAboutYourWork: Arbitrary[AboutYourWork] =
    Arbitrary {
      Gen.oneOf(AboutYourWork.values)
    }

  implicit lazy val arbitraryPaymentsIntoPensions: Arbitrary[PaymentsIntoPensions] =
    Arbitrary {
      Gen.oneOf(PaymentsIntoPensions.values)
    }

  implicit lazy val arbitraryUkInterest: Arbitrary[UkInterest] =
    Arbitrary {
      Gen.oneOf(UkInterest.values)
    }

  implicit lazy val arbitraryUkInsuranceGains: Arbitrary[UkInsuranceGains] =
    Arbitrary {
      Gen.oneOf(UkInsuranceGains.values)
    }

  implicit lazy val arbitraryUkDividendsSharesLoans: Arbitrary[UkDividendsSharesLoans] =
    Arbitrary {
      Gen.oneOf(UkDividendsSharesLoans.values)
    }

  implicit lazy val arbitraryRentalIncome: Arbitrary[RentalIncome] =
    Arbitrary {
      Gen.oneOf(RentalIncome.values)
    }

  implicit lazy val arbitraryPensions: Arbitrary[Pensions] =
    Arbitrary {
      Gen.oneOf(Pensions.values)
    }

  implicit lazy val arbitraryNonUkInterestDividendsInsurance: Arbitrary[NonUkInterestDividendsInsurance] =
    Arbitrary {
      Gen.oneOf(NonUkInterestDividendsInsurance.values)
    }

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
