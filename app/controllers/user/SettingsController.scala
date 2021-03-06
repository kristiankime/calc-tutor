package controllers.user

import javax.inject.{Inject, Singleton}
import com.artclod.slick.JodaUTC
import controllers.ApplicationInfo
import controllers.organization.CourseJoin
import controllers.quiz.QuizAvailability
import controllers.support.{Consented, RequireAccess}
import dao.organization.{CourseDAO, OrganizationDAO}
import dao.quiz.QuizDAO
import dao.user.UserDAO
import models._
import models.organization.Course
import models.user.User
import org.pac4j.core.config.Config
import org.pac4j.core.profile.CommonProfile
import org.pac4j.play.scala.{Security, SecurityComponents}
import org.pac4j.play.store.PlaySessionStore
import play.api.data.Form
import play.api.data.Forms.{mapping, optional, text, tuple, _}
import play.api.mvc.Results.Redirect
import play.api.mvc._
import play.libs.concurrent.HttpExecutionContext
import com.artclod.util._

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Random, Right}

@Singleton
class SettingsController @Inject()(/*val config: Config, val playSessionStore: PlaySessionStore, override val ec: HttpExecutionContext*/ val controllerComponents: SecurityComponents, userDAO: UserDAO, organizationDAO: OrganizationDAO, courseDAO: CourseDAO, quizDAO: QuizDAO)(implicit executionContext: ExecutionContext) extends BaseController with Security[CommonProfile]  {

  def updateSettings() = Secure(ApplicationInfo.defaultSecurityClients, "Access").async { authenticatedRequest => Consented(authenticatedRequest, userDAO) { user => Action.async { implicit request =>

    UserSettings.form.bindFromRequest.fold(
        errors => Future.successful(BadRequest(views.html.errors.formErrorPage(errors))),
        form => {
          val settingsFuture = userDAO.updateSettings(user, form.name, form.emailGameUpdates)
          settingsFuture.map(_ =>  Redirect(controllers.routes.HomeController.home()))
        }
      )
    }

  } }

}

case class UserSettingsData(name: Option[String], emailGameUpdates: Boolean)

object UserSettings {
  val name = "name"
  val emailGameUpdates = "emailGameUpdates"

  val form = Form(mapping(
    name             -> optional(text.verifying("Name must not be blank", _.trim != "")),
    emailGameUpdates -> boolean
  )(UserSettingsData.apply)(UserSettingsData.unapply))

  private def validName(name: String) = { name.trim != "" }
}