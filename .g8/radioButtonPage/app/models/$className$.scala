package models

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait $className$

object $className$ extends Enumerable.Implicits {

  case object $option1key;
  format = "Camel" $
  extends WithName("$option1key;format="
  decap"$"
  ) with $className$

  case object $option2key;
  format = "Camel" $
  extends WithName("$option2key;format="
  decap"$"
  ) with $className$

  val values: Seq[$className$] = Seq(
    $option1key;
  format = "Camel" $
  , $option2key;
  format = "Camel" $
  )

  private def getRadioItems(contentPrefix: String)(implicit messages: Messages): Seq[RadioItem] = {
    values.zipWithIndex.map {
      case (value, index) =>
        RadioItem(
          content = Text(messages(s"$contentPrefix;format="decap"$.\${value.toString}") ),
          value = Some(value.toString),
          id = Some(s"value_\$index")
      )
    }
  }

  def options(implicit messages: Messages): Seq[RadioItem] = getRadioItems(s"$className")

  def agentOptions(implicit messages: Messages): Seq[RadioItem] = getRadioItems(s"$className.agent")

  implicit val enumerable: Enumerable[$className$] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
