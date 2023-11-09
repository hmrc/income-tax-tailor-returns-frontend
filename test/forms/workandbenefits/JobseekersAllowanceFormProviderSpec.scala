/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package forms.workandbenefits

import forms.behaviours.CheckboxFieldBehaviours
import models.workandbenefits.JobseekersAllowance
import play.api.data.FormError

class JobseekersAllowanceFormProviderSpec extends CheckboxFieldBehaviours {

  val form = new JobseekersAllowanceFormProvider()(false)
  val agentForm = new JobseekersAllowanceFormProvider()(true)

  ".value" - {

    val fieldName = "value"
    val requiredKey = "jobseekersAllowance.error.required"

    behave like checkboxField[JobseekersAllowance](
      form,
      fieldName,
      validValues  = JobseekersAllowance.values,
      invalidError = FormError(s"$fieldName[0]", "error.invalid")
    )

    behave like mandatoryCheckboxField(
      form,
      fieldName,
      requiredKey
    )

    behave like exclusiveCheckboxField(
      form,
      JobseekersAllowance.ExclusiveOption.toString,
      fieldName,
      JobseekersAllowance.Option1.toString,
      requiredKey
    )
  }

  ".value for an agent" - {

    val fieldName = "value"
    val requiredKey = "jobseekersAllowance.agent.error.required"

    behave like mandatoryCheckboxField(
      agentForm,
      fieldName,
      requiredKey
    )

    behave like exclusiveCheckboxField(
      agentForm,
      JobseekersAllowance.ExclusiveOption.toString,
      fieldName,
      JobseekersAllowance.Option1.toString,
      requiredKey
    )
  }
}
