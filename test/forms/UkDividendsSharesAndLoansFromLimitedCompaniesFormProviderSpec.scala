package forms

import forms.behaviours.CheckboxFieldBehaviours
import models.UkDividendsSharesAndLoansFromLimitedCompanies
import play.api.data.FormError

class UkDividendsSharesAndLoansFromLimitedCompaniesFormProviderSpec extends CheckboxFieldBehaviours {

  val form = new UkDividendsSharesAndLoansFromLimitedCompaniesFormProvider()(false)
  val agentForm = new UkDividendsSharesAndLoansFromLimitedCompaniesFormProvider()(true)

  ".value" - {

    val fieldName = "value"
    val requiredKey = "ukDividendsSharesAndLoansFromLimitedCompanies.error.required"

    behave like checkboxField[UkDividendsSharesAndLoansFromLimitedCompanies](
      form,
      fieldName,
      validValues  = UkDividendsSharesAndLoansFromLimitedCompanies.values,
      invalidError = FormError(s"$fieldName[0]", "error.invalid")
    )

    behave like mandatoryCheckboxField(
      form,
      fieldName,
      requiredKey
    )

    behave like exclusiveCheckboxField(
      form,
      UkDividendsSharesAndLoansFromLimitedCompanies.ExclusiveOption.toString,
      fieldName,
      UkDividendsSharesAndLoansFromLimitedCompanies.Option1.toString,
      requiredKey
    )
  }

  ".value for an agent" - {

    val fieldName = "value"
    val requiredKey = "ukDividendsSharesAndLoansFromLimitedCompanies.agent.error.required"

    behave like mandatoryCheckboxField(
      agentForm,
      fieldName,
      requiredKey
    )

    behave like exclusiveCheckboxField(
      agentForm,
      UkDividendsSharesAndLoansFromLimitedCompanies.ExclusiveOption.toString,
      fieldName,
      UkDividendsSharesAndLoansFromLimitedCompanies.Option1.toString,
      requiredKey
    )
  }
}
