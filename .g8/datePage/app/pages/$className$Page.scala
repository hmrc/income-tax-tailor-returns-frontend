package pages

import play.api.libs.json.JsPath

import java.time.LocalDate

case object $className$Page extends QuestionPage[LocalDate] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "$className;format="decap"$"
}
