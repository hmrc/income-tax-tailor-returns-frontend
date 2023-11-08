package forms

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.Forms.set
import models.$className$

class $className$FormProvider @Inject() extends Mappings {

  def apply(isAgent: Boolean): Form[Set[$className$]] = {
    val error: String =
      if (isAgent) {
        "$className;format="decap"$.agent.error.required"
      } else {
        "$className;format="decap"$.error.required"
      }
    Form(
      "value" -> set(enumerable[$className$](error)).verifying(
        firstError(nonEmptySet(error), exclusiveItemInSet(error, $className$.ExclusiveOption.toString))
      )
    )
  }
}
