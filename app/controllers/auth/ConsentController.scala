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

//  val splitEmailOnAt = """([^@]+)@([^@]+)""".r


  //
  //  def consent(goTo: Option[String], error : Option[String]) = SecuredUserDBAction { implicit request => implicit user => implicit session =>
  //    Ok(views.html.user.consent(goTo, error))
  //  }
  def consent(goTo: Option[String], errorInfo: Option[String]) = Secure("RedirectUnauthenticatedClient") { profiles =>
    Action { request =>
      Ok(views.html.auth.consent(goTo, errorInfo, request))
    }
  }

  //  def noConsent() = SecuredUserDBAction { implicit request => implicit user => implicit session =>
  //    Ok(views.html.user.noConsent())
  //  }
  def noConsent() = Secure("RedirectUnauthenticatedClient") { profiles =>
    Action { request =>
        Ok(views.html.auth.noConsent())
    }
  }


//  def consentSubmit(goTo: Option[String]) = SecuredUserDBAction("TODO REMOVE ME WHEN INTELLIJ 14 CAN PARSE WITHOUT THIS") { implicit request => implicit user => implicit session =>
//    ConsentForm.values.bindFromRequest.fold(
//      errors => {
//        Logger("consent").error("error" + errors)
//        BadRequest(views.html.errors.formErrorPage(errors))
//      },
//      consented => {
//
//        val settings = (Users(user.id) match {
//          case Some(setting) => Users.update(setting.copy(consented = consented))
//          case None => Users.create(User(id = user.id, consented = consented, name = defaultName(user), allowAutoMatch = true, seenHelp = false, emailUpdates = true, lastAccess = JodaUTC.now))
//        })
//
//        (settings, consented, goTo) match {
//          case (Failure(t), _, _) => {
//            Logger("consent").error(stackTraceToString(t))
//            Redirect(routes.Consent.consent(goTo, Some("Sorry a system error occured please try again [" + t.getMessage + "]")))
//          }
//          case (_, false, _) => Redirect(routes.Consent.noConsent())
//          case (_, true, Some(path)) => Redirect(path)
//          case (_, true, None) => Redirect(routes.Home.index())
//        }
//
//      })
//  }


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
                case (_, true, None) => Redirect(controllers.routes.ApplicationController.index())
              }
            }
          })
      }
    }

//  private def stackTraceToString(t : Throwable) = {
//    val sw = new StringWriter();
//    val pw = new PrintWriter(sw);
//    t.printStackTrace(pw);
//    sw.toString();
//  }
//
//  private def defaultName(user: Login)(implicit session: Session) : String = "Player" // UserSettings.validName(startingName(user))
//
//  @VisibleForTesting
//  def startingName(user: Login) =
//    user.email match {
//      case Some(splitEmailOnAt(before, after)) => before
//      case _ => user.fullName
//    }

//  def revokeConsent() = SecuredUserDBAction { implicit request => implicit user => implicit session =>
//    val settings = (Users(user.id) match {
//      case Some(setting) => Users.update(setting.copy(consented = false))
//      case None => throw new IllegalStateException("Attempted to revoke consent for [" + user.id + "] but user had no settings")
//    })
//    Ok(views.html.user.noConsent())
//  }
  def revokeConsent() = Secure("RedirectUnauthenticatedClient") { profiles =>
    Action.async { request =>
      userDAO.ensureByLoginId(profiles).flatMap(user => userDAO.updateConsent(user, false).map(worked => Ok(views.html.auth.noConsent()))
      )
    }
  }
}

object ConsentForm {
  val agree = "agree"

  val values = Form(agree -> boolean)
}
