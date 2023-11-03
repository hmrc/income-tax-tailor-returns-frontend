package generators

import org.scalacheck.Arbitrary
import pages.aboutyou.{ChildBenefitIncomePage, ChildBenefitPage, UkResidenceStatusPage}

trait PageGenerators {

  implicit lazy val arbitraryUkResidenceStatusPage: Arbitrary[UkResidenceStatusPage.type] =
    Arbitrary(UkResidenceStatusPage)

  implicit lazy val arbitraryChildBenefitPage: Arbitrary[ChildBenefitPage.type] =
    Arbitrary(ChildBenefitPage)

  implicit lazy val arbitraryChildBenefitIncomePage: Arbitrary[ChildBenefitIncomePage.type] =
    Arbitrary(ChildBenefitIncomePage)


}
