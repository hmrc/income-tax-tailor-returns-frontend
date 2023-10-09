package forms

import forms.behaviours.CheckboxFieldBehaviours
import models.$className$
import play.api.data.FormError

class $className$FormProviderSpec extends CheckboxFieldBehaviours {

  val form = new $className$FormProvider()(false)
  val agentForm = new $className$FormProvider()(true)

  ".value" - {

    val fieldName = "value"
    val requiredKey = "$className;format="decap"$.error.required"

    behave like checkboxField[$className$](
      form,
      fieldName,
      validValues  = $className$.values,
      invalidError = FormError(s"\$fieldName[0]", "error.invalid")
    )

    behave like mandatoryCheckboxField(
      form,
      fieldName,
      requiredKey
    )
  }

  ".value for an agent" - {

    val fieldName = "value"
    val requiredKey = "$className;format="decap"$.agent.error.required"

    behave like mandatoryCheckboxField(
      agentForm,
      fieldName,
      requiredKey
    )
  }
}
