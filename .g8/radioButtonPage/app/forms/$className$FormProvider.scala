package forms

import forms.mappings.Mappings
import play.api.data.Form

import javax.inject.Inject

class $className$FormProvider @Inject() extends Mappings {

  def apply(): Form[$className$] =
    Form(
      "value" -> enumerable[$className$]("$className;format="decap"$.error.required")
    )
}
