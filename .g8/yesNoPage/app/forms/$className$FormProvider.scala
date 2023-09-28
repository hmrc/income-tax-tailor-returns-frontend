package forms

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form

class $className$FormProvider @Inject() extends Mappings {

  def apply(isAgent: Boolean): Form[Boolean] = {
    val error: String =
      if (isAgent) {
        "$className;format="decap"$.agent.error.required"
      } else {
        "$className;format="decap"$.error.required"
      }
    Form(
      "value" -> boolean(error)
    )
  }
}
