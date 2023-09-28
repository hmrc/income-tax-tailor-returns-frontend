package forms

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form
import models.$className$

class $className$FormProvider @Inject() extends Mappings {

  def apply(isAgent: Boolean): Form[$className$] = {
    val error: String =
      if (isAgent) {
        "$className;format="decap"$.agent.error.required"
      } else {
        "$className;format="decap"$.error.required"
      }
    Form(
      "value" -> enumerable[$className$](error)
    )
  }
}
