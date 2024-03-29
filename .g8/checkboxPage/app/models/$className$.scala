package models

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.{CheckboxItem, ExclusiveCheckbox}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import viewmodels.govuk.checkbox._

sealed trait $className$

object $className$ extends Enumerable.Implicits {

  case object $option1key;format="Camel"$ extends WithName("$option1key;format="decap"$") with $className$
  case object $option2key;format="Camel"$ extends WithName("$option2key;format="decap"$") with $className$
  case object ExclusiveOption extends WithName("exclusive") with $className$

  val values: Seq[$className$] = Seq(
    $option1key;format="Camel"$,
    $option2key;format="Camel"$,
    ExclusiveOption
  )

  private def getCheckboxItem(contentPrefix: String)(implicit messages: Messages): Seq[CheckboxItem] = {
    values.zipWithIndex.map {
      case (value, index) =>
        value match {
          case ExclusiveOption => CheckboxItemViewModel(content = Text(messages(contentPrefix+s".\${value.toString}")),
                fieldId = "value",
                index = index,
                value = value.toString,
                behaviour = Some (ExclusiveCheckbox)
        )
          case _ => CheckboxItemViewModel(content = Text(messages(contentPrefix+s".\${value.toString}")),
                fieldId = "value",
                index = index,
                value = value.toString
        )
        }
    }
  }



  def checkboxItems(implicit messages: Messages): Seq[CheckboxItem] =
    getCheckboxItem("$className;format=" decap"$")


  def agentCheckboxItems(implicit messages: Messages): Seq[CheckboxItem] =
    getCheckboxItem("$className;format=" decap"$.agent")

  implicit val enumerable: Enumerable[$className$] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
