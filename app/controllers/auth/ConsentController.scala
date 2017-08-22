package controllers.auth

import java.io.{PrintWriter, StringWriter}
import javax.inject.{Inject, Singleton}

import com.artclod.slick.JodaUTC
import com.google.common.annotations.VisibleForTesting
import dao.auth.NamePassDAO
import dao.user.UserDAO
import org.pac4j.core.config.Config
import org.pac4j.core.profile.CommonProfile
import org.pac4j.play.scala.Security
import org.pac4j.play.store.PlaySessionStore
import org.pac4j.sql.profile.service.DbProfileService
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.{Action, Controller}
import play.libs.concurrent.HttpExecutionContext

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure

//object ConsentController extends Controller with SecureSocialDB {
@Singleton
class ConsentController @Inject()(val config: Config, val playSessionStore: PlaySessionStore, val dbProfileService: DbProfileService, val userDAO: UserDAO, override val ec: HttpExecutionContext)(implicit val executionContext: ExecutionContext) extends Controller with Security[CommonProfile] {

  def consent(goTo: Option[String], errorInfo: Option[String]) = Secure("RedirectUnauthenticatedClient") { profiles =>
    Action { implicit request =>
      Ok(views.html.auth.consent(goTo, errorInfo))
    }
  }

  def noConsent() = Secure("RedirectUnauthenticatedClient") { profiles =>
    Action { implicit request =>
        Ok(views.html.auth.noConsent())
    }
  }

  def consentSubmit(goTo: Option[String]) = Secure("RedirectUnauthenticatedClient") { profiles =>
    Action.async { implicit request =>
      ConsentForm.values.bindFromRequest.fold(
        errors => {
          Logger("ConsentController.consentSubmit").error("error" + errors)
          Future.successful(BadRequest(views.html.errors.formErrorPage(errors)))
        },
        consented => {
          userDAO.ensureByLoginId(profiles).flatMap( user =>
            userDAO.updateConsent(user, consented)
          ).map{ worked => (worked, consented, goTo) match {
              case (0, _, _) => {
                Logger("ConsentController.consentSubmit").error("Could not update User")
                Redirect(controllers.auth.routes.ConsentController.consent(goTo, Some("Sorry a system error occured please try again, Could not update User")))
              }
              case (_, false, _) => Redirect(controllers.auth.routes.ConsentController.noConsent())
              case (_, true, Some(path)) => Redirect(path)
              case (_, true, None) => Redirect(controllers.routes.HomeController.index())
            }
          }
        })
    }
  }

  def revokeConsent() = Secure("RedirectUnauthenticatedClient") { profiles =>
    Action.async { implicit request =>
      userDAO.ensureByLoginId(profiles).flatMap(user => userDAO.updateConsent(user, false).map(worked => Ok(views.html.auth.noConsent()))
      )
    }
  }

}

object ConsentForm {
  val agree = "agree"

  val values = Form(agree -> boolean)
}
