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

package viewmodels.govuk

import play.api.data.Form
import play.api.i18n.Messages
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.{ErrorMessage, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.{CheckboxBehaviour, CheckboxItem, Checkboxes}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{Content, Empty}
import uk.gov.hmrc.govukfrontend.views.viewmodels.fieldset.{Fieldset, Legend}
import uk.gov.hmrc.govukfrontend.views.viewmodels.hint.Hint
import uk.gov.hmrc.govukfrontend.views.viewmodels.label.Label
import viewmodels.ErrorMessageAwareness

object checkbox extends CheckboxFluency

trait CheckboxFluency {

  object CheckboxesViewModel extends ErrorMessageAwareness with FieldsetFluency {

    def apply(
               form: Form[_],
               idPrefix: String,
               name: String,
               items: Seq[CheckboxItem],
               legend: Legend,
               hint: Option[Hint] = None
             )(implicit messages: Messages): Checkboxes =
      apply(
        form = form,
        idPrefix = idPrefix,
        name = name,
        items = items,
        hint = hint,
        fieldset = FieldsetViewModel(legend)
      )

    def apply(
               form: Form[_],
               idPrefix: String,
               name: String,
               hint: Option[Hint],
               items: Seq[CheckboxItem],
               fieldset: Fieldset
             )(implicit messages: Messages): Checkboxes =
      Checkboxes(
        fieldset     = Some(fieldset),
        idPrefix     = Some(idPrefix),
        name         = name,
        hint         = hint,
        errorMessage = form.errors.headOption.map(err => ErrorMessage(content = Text(messages(err.message, err.args:_*)))),
        items        = items.map {
          item =>
            item.copy(checked = form.data.exists(data => data._2 == item.value))
        }
      )
  }

  implicit class FluentCheckboxes(checkboxes: Checkboxes) {

    def describedBy(value: String): Checkboxes =
      checkboxes.copy(describedBy = Some(value))

    def withHint(hint: Hint): Checkboxes =
      checkboxes.copy(hint = Some(hint))
  }

  object CheckboxItemViewModel {

    def apply(
               content: Content,
               fieldId: String,
               index: Int,
               value: String,
               hint: Option[Hint] = None,
               behaviour: Option[CheckboxBehaviour] = None
             ): CheckboxItem =
      CheckboxItem(
        content = content,
        value   = value,
        hint = hint,
        behaviour = behaviour
      )

    def apply(
               fieldId: String,
               index: Int,
               value: String,
               divider: String
             ): CheckboxItem =
      CheckboxItem(
        content = Empty,
        name = Some(s"$fieldId"),
        value = value,
        divider = Some(divider)
      )
  }

  implicit class FluentCheckboxItem(item: CheckboxItem) {

    def withLabel(label: Label): CheckboxItem =
      item.copy(label = Some(label))

    def withHint(hint: Hint): CheckboxItem =
      item.copy(hint = Some(hint))

    def withConditionalHtml(html: Html): CheckboxItem =
      item.copy(conditionalHtml = Some(html))

    def disabled(): CheckboxItem =
      item.copy(disabled = true)

    def withAttribute(attribute: (String, String)): CheckboxItem =
      item.copy(attributes = item.attributes + attribute)
  }
}
