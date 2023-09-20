package forms

import forms.mappings.Mappings
import play.api.data.Form

import javax.inject.Inject

class $className$FormProvider @Inject() extends Mappings {

  def apply(): Form[Boolean] =
    Form(
      "value" -> boolean("$className;format="decap"$.error.required")
    )
}
