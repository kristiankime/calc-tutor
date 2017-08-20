package controllers.support

import play.api.mvc._
import controllers.{Application}
import dao.user.UserDAO
import org.pac4j.core.profile.CommonProfile
import play.api.mvc.Results.Redirect

import scala.concurrent.{Await, Future}

case class Consented[A](commonProfiles: List[CommonProfile], userDAO: UserDAO)(action: Action[A]) extends Action[A] {

  lazy val parser = action.parser

  def apply(request: Request[A]): Future[Result] = {
    val user = Await.result(userDAO.ensureByLoginId(commonProfiles), Application.appTimeout)
    val path = request.path
    if(user.consented) {
      action(request)
    } else {
      Future.successful(Redirect(controllers.auth.routes.ConsentController.consent(Some(path), None)))
    }
  }

}