package forms

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class $className$FormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "$className;format="decap"$.error.required"
  val agentRequiredKey = "$className;format="decap"$.agent.error.required"
  val invalidKey = "error.boolean"

  val form = new $className$FormProvider(false)()
  val agentForm = new $className$FormProvider(true)()

  ".value" - {

    val fieldName = "value"

    behave like booleanField(
      form,
      fieldName,
      invalidError = FormError(fieldName, invalidKey)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".value for an agent" - {

    val fieldName = "value"

    behave like mandatoryField(
      agentForm,
      fieldName,
      requiredError = FormError(fieldName, agentRequiredKey)
    )
  }
}
