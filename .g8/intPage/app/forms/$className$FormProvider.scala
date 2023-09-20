package forms

import forms.mappings.Mappings
import play.api.data.Form

import javax.inject.Inject

class $className$FormProvider @Inject() extends Mappings {

  def apply(): Form[Int] =
    Form(
      "value" -> int(
        "$className;format="decap"$.error.required",
        "$className;format="decap"$.error.wholeNumber",
        "$className;format="decap"$.error.nonNumeric")
          .verifying(inRange($minimum$, $maximum$, "$className;format="decap"$.error.outOfRange"))
    )
}
