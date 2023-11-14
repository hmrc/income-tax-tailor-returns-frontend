package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.UkDividendsSharesAndLoansFromLimitedCompaniesPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object UkDividendsSharesAndLoansFromLimitedCompaniesSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(UkDividendsSharesAndLoansFromLimitedCompaniesPage).map {
      checkboxes =>

        val value = ValueViewModel(
          HtmlContent(
            checkboxes.map {
              answer => HtmlFormat.escape(messages(s"ukDividendsSharesAndLoansFromLimitedCompanies.$answer")).toString
            }
            .mkString(",<br>")
          )
        )

        SummaryListRowViewModel(
          key     = "ukDividendsSharesAndLoansFromLimitedCompanies.checkYourAnswersLabel",
          value   = value,
          actions = Seq(
            ActionItemViewModel("site.change", routes.UkDividendsSharesAndLoansFromLimitedCompaniesController.onPageLoad(CheckMode, answers.taxYear).url)
              .withVisuallyHiddenText(messages("ukDividendsSharesAndLoansFromLimitedCompanies.change.hidden"))
          )
        )
    }
}
