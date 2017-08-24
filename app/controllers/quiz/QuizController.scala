package controllers.quiz

import javax.inject._

import _root_.controllers.support.{Consented, RequireAccess}
import com.artclod.slick.JodaUTC
import dao.organization.{CourseDAO, OrganizationDAO}
import dao.user.UserDAO
import models._
import org.pac4j.core.config.Config
import org.pac4j.core.profile.CommonProfile
import org.pac4j.play.scala.Security
import org.pac4j.play.store.PlaySessionStore
import play.api.mvc._
import play.libs.concurrent.HttpExecutionContext
import com.artclod.util._
import controllers.organization.CourseCreate
import dao.quiz.QuizDAO
import models.organization.Course
import models.quiz.Quiz
import play.api.data.Form
import play.api.data.Forms._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Right


@Singleton
class QuizController @Inject()(val config: Config, val playSessionStore: PlaySessionStore, override val ec: HttpExecutionContext, userDAO: UserDAO, organizationDAO: OrganizationDAO, courseDAO: CourseDAO, quizDAO: QuizDAO)(implicit executionContext: ExecutionContext) extends Controller with Security[CommonProfile]  {


  def createForm(organizationId: OrganizationId, courseId: CourseId) = RequireAccess(Edit, to=courseId) { Secure("RedirectUnauthenticatedClient", "Access") { profiles => Consented(profiles, userDAO) { user => Action.async { implicit request =>

    courseDAO(organizationId, courseId).map{ _ match {
      case Left(notFoundResult) => notFoundResult
      case Right(course) => Ok(views.html.quiz.createQuizForCourse(course))
      }
    }

  } } } }

  def createSubmit(organizationId: OrganizationId, courseId: CourseId) = RequireAccess(Edit, to=organizationId) { Secure("RedirectUnauthenticatedClient", "Access") { profiles => Consented(profiles, userDAO) { user => Action.async { implicit request =>

    courseDAO(organizationId, courseId).flatMap{ _ match {
      case Left(notFoundResult) => Future.successful(notFoundResult)
      case Right(course) =>
        QuizCreate.form.bindFromRequest.fold(
          errors => Future.successful(BadRequest(views.html.errors.formErrorPage(errors))),
          form => {
            val now = JodaUTC.now
            val quizFuture = quizDAO.insert(Quiz(null, user.id, form, now, now))
            quizFuture.map(quiz => Redirect(controllers.quiz.routes.QuizController.view(organizationId, course.id, quiz.id, None)))
          })
    }
    }

  } } } }

  def view(organizationId: OrganizationId, courseId: CourseId, quizId: QuizId, answerIdOp: Option[AnswerId]) = RequireAccess(View, to=courseId) { Secure("RedirectUnauthenticatedClient", "Access") { profiles => Consented(profiles, userDAO) { user => Action.async { implicit request =>

    (courseDAO(organizationId, courseId) +& quizDAO(quizId) +^ quizDAO.access(user.id, quizId)).map{ _ match {
      case Left(notFoundResult) => notFoundResult
      case Right((course, quiz, access)) => Ok(views.html.quiz.viewQuizForCourse(access, course, quiz))
    }
    }

  } } } }

  def rename(organizationId: OrganizationId, courseId: CourseId, quizId: QuizId) = RequireAccess(Edit, to=quizId) { Secure("RedirectUnauthenticatedClient", "Access") { profiles => Consented(profiles, userDAO) { user => Action.async { implicit request =>

    (courseDAO(organizationId, courseId) +& quizDAO(quizId)).flatMap{ _ match {
      case Left(notFoundResult) => Future.successful(notFoundResult)
      case Right((course, quiz)) =>
        QuizRename.form.bindFromRequest.fold(
          errors => Future.successful(BadRequest(views.html.errors.formErrorPage(errors))),
          form => {
            val updateNameFuture = quizDAO.updateName(quiz, form)
            updateNameFuture.map(updateName => Redirect(controllers.quiz.routes.QuizController.view(organizationId, course.id, quiz.id, None)))
          })
      }
    }

  } } } }

}

object QuizCreate {
  val name = "name"

  val form : Form[String] = Form(name -> nonEmptyText)
}

object QuizRename {
  val name = "name"

  val form : Form[String] = Form(name -> nonEmptyText)
}