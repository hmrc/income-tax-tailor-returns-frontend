package controllers

import controllers.actions._
import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.$className$View

class $className$Controller @Inject()(
                                       override val messagesApi: MessagesApi,
                                       identify: IdentifierActionProvider,
                                       getData: DataRetrievalActionProvider,
                                       requireData: DataRequiredActionProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: $className$View
                                     ) extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int): Action[AnyContent] = (identify(taxYear) andThen getData(taxYear) andThen requireData(taxYear)) {
    implicit request =>
      Ok(view(taxYear))
  }
}
