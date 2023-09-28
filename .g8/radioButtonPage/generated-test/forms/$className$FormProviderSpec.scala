package forms

import forms.behaviours.OptionFieldBehaviours
import models.$className$
import play.api.data.FormError

class $className$FormProviderSpec extends OptionFieldBehaviours {

  val form = new $className$FormProvider()(false)
  val agentForm = new $className$FormProvider()(true)

  ".value" - {

    val fieldName = "value"
    val requiredKey = "$className;format="decap"$.error.required"

    behave like optionsField[$className$](
      form,
      fieldName,
      validValues  = $className$.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".value for an agent" - {

    val fieldName = "value"
    val agentRequiredKey = "$className;format="decap"$.agent.error.required"

    behave like mandatoryField(
      agentForm,
      fieldName,
      requiredError = FormError(fieldName, agentRequiredKey)
    )
  }
}
