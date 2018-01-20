package controllers.library

import javax.inject.{Inject, Singleton}

import com.artclod.slick.JodaUTC
import controllers.Application
import controllers.organization.CourseJoin
import controllers.quiz.{QuestionCreate, QuizAvailability}
import controllers.support.{Consented, RequireAccess}
import dao.organization.{CourseDAO, OrganizationDAO}
import dao.quiz.{QuestionDAO, QuizDAO, SkillDAO}
import dao.user.UserDAO
import models._
import models.organization.Course
import models.user.User
import org.pac4j.core.config.Config
import org.pac4j.core.profile.CommonProfile
import org.pac4j.play.scala.Security
import org.pac4j.play.store.PlaySessionStore
import play.api.data.Form
import play.api.data.Forms.{mapping, optional, text, tuple, _}
import play.api.mvc.Results.Redirect
import play.api.mvc._
import play.libs.concurrent.HttpExecutionContext
import com.artclod.util._
import models.quiz.QuestionFrame
import play.api.libs.json.{JsError, JsSuccess, Json}

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Random, Right}

@Singleton
class LibraryController @Inject()(val config: Config, val playSessionStore: PlaySessionStore, override val ec: HttpExecutionContext, userDAO: UserDAO, organizationDAO: OrganizationDAO, courseDAO: CourseDAO, quizDAO: QuizDAO, skillDAO: SkillDAO, questionDAO: QuestionDAO)(implicit executionContext: ExecutionContext) extends Controller with Security[CommonProfile] {

  def view() = Secure("RedirectUnauthenticatedClient", "Access") { profiles => Consented(profiles, userDAO) { implicit user => Action.async { implicit request =>
    skillDAO.allSkills.flatMap(skills => { questionDAO.skillsForAllSet().map(questionsAndSkills => {
            Ok(views.html.library.list(skills, questionsAndSkills))
      })})
    }}}

  def createQuestionView() = Secure("RedirectUnauthenticatedClient", "Access") { profiles => Consented(profiles, userDAO) { implicit user => Action.async { implicit request =>
    skillDAO.allSkills.map(skills => Ok(views.html.library.create(skills)))
  }}}

  def createQuestionSubmit() = Secure("RedirectUnauthenticatedClient", "Access") { profiles => Consented(profiles, userDAO) { implicit user => Action.async { implicit request =>

        QuestionCreate.form.bindFromRequest.fold(
          errors => Future.successful(BadRequest(views.html.errors.formErrorPage(errors))),
          form => {
            QuestionCreate.questionFormat.reads(Json.parse(form)) match {
              case JsError(errors) => Future.successful(BadRequest(views.html.errors.jsonErrorPage(errors)))
              case JsSuccess(value, path) => {
                skillDAO.skillsMap.flatMap(skillsMap => { questionDAO.insert(QuestionFrame(value, user.id, skillsMap)).map(questionFrame => {
                  Redirect(controllers.library.routes.LibraryController.view())
                })})
              }
            }
          }
        )

  } } }

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