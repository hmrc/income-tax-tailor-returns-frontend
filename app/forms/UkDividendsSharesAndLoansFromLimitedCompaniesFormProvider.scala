package forms

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.Forms.set
import models.UkDividendsSharesAndLoansFromLimitedCompanies

class UkDividendsSharesAndLoansFromLimitedCompaniesFormProvider @Inject() extends Mappings {

  def apply(isAgent: Boolean): Form[Set[UkDividendsSharesAndLoansFromLimitedCompanies]] = {
    val error: String =
      if (isAgent) {
        "ukDividendsSharesAndLoansFromLimitedCompanies.agent.error.required"
      } else {
        "ukDividendsSharesAndLoansFromLimitedCompanies.error.required"
      }
    Form(
      "value" -> set(enumerable[UkDividendsSharesAndLoansFromLimitedCompanies](error)).verifying(
        firstError(nonEmptySet(error), exclusiveItemInSet(error, UkDividendsSharesAndLoansFromLimitedCompanies.ExclusiveOption.toString))
      )
    )
  }
}
