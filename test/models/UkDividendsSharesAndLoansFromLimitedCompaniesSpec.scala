package models

import generators.ModelGenerators
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import play.api.libs.json.{JsError, JsString, Json}

class UkDividendsSharesAndLoansFromLimitedCompaniesSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues with ModelGenerators {

  "UkDividendsSharesAndLoansFromLimitedCompanies" - {

    "must deserialise valid values" in {

      val gen = arbitrary[UkDividendsSharesAndLoansFromLimitedCompanies]

      forAll(gen) {
        ukDividendsSharesAndLoansFromLimitedCompanies =>

          JsString(ukDividendsSharesAndLoansFromLimitedCompanies.toString).validate[UkDividendsSharesAndLoansFromLimitedCompanies].asOpt.value mustEqual ukDividendsSharesAndLoansFromLimitedCompanies
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!UkDividendsSharesAndLoansFromLimitedCompanies.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[UkDividendsSharesAndLoansFromLimitedCompanies] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = arbitrary[UkDividendsSharesAndLoansFromLimitedCompanies]

      forAll(gen) {
        ukDividendsSharesAndLoansFromLimitedCompanies =>

          Json.toJson(ukDividendsSharesAndLoansFromLimitedCompanies) mustEqual JsString(ukDividendsSharesAndLoansFromLimitedCompanies.toString)
      }
    }
  }
}
