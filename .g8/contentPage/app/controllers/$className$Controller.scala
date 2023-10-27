package controllers

import controllers.actions.TaxYearAction.taxYearAction
import controllers.actions._
import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.$className$View
import views.html.$className$AgentView

class $className$Controller @Inject()(
                                       override val messagesApi: MessagesApi,
                                       identify: IdentifierActionProvider,
                                       getData: DataRetrievalActionProvider,
                                       requireData: DataRequiredActionProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: $className$View,
                                       agentView: $className$AgentView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int): Action[AnyContent] =
    (identify(taxYear) andThen taxYearAction(taxYear) andThen getData(taxYear) andThen requireData(taxYear)) {
    implicit request =>
      if (request.isAgent) {
        Ok(agentView(taxYear))
      } else {
        Ok(view(taxYear))
      }
  }
}
