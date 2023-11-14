package pages

import models.UkDividendsSharesAndLoansFromLimitedCompanies
import play.api.libs.json.JsPath

case object UkDividendsSharesAndLoansFromLimitedCompaniesPage extends QuestionPage[Set[UkDividendsSharesAndLoansFromLimitedCompanies]] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "ukDividendsSharesAndLoansFromLimitedCompanies"
}
