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
import dao.quiz.{QuestionDAO, QuizDAO}
import models.organization.Course
import models.quiz.Quiz
import play.api.data.Form
import play.api.data.Forms._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Right


@Singleton
class QuestionController @Inject()(val config: Config, val playSessionStore: PlaySessionStore, override val ec: HttpExecutionContext, userDAO: UserDAO, organizationDAO: OrganizationDAO, courseDAO: CourseDAO, quizDAO: QuizDAO, questionDAO: QuestionDAO)(implicit executionContext: ExecutionContext) extends Controller with Security[CommonProfile]  {

//  def view(organizationId: OrganizationId, courseId: CourseId, quizId: QuizId, questionId: QuestionId, answerIdOp: Option[AnswerId]) = RequireAccess(View, to=courseId) { Secure("RedirectUnauthenticatedClient", "Access") { profiles => Consented(profiles, userDAO) { user => Action.async { implicit request =>
//
//    (courseDAO(organizationId, courseId) +& quizDAO(quizId) +& questionDAO(questionId)).map{ _ match {
//      case Left(notFoundResult) => notFoundResult
//      case Right((course, quiz, question)) => Ok(views.html.quiz.viewQuestionForCourse(course, quiz, question))
//      }
//    }
//
//  } } } }

}

//object QuizCreate {
//  val name = "name"
//
//  val form : Form[String] = Form(name -> nonEmptyText)
//}
//
//object QuizRename {
//  val name = "name"
//
//  val form : Form[String] = Form(name -> nonEmptyText)
//}