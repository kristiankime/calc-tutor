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
import models.quiz.{QuestionFrame, Quiz}
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.{JsError, JsSuccess, Json}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Right


@Singleton
class AnswerController @Inject()(val config: Config, val playSessionStore: PlaySessionStore, override val ec: HttpExecutionContext, userDAO: UserDAO, organizationDAO: OrganizationDAO, courseDAO: CourseDAO, quizDAO: QuizDAO, questionDAO: QuestionDAO)(implicit executionContext: ExecutionContext) extends Controller with Security[CommonProfile]  {

  def createCourseSubmit(organizationId: OrganizationId, courseId: CourseId, quizId: QuizId, questionId: QuestionId) = RequireAccess(Edit, to=organizationId) { Secure("RedirectUnauthenticatedClient", "Access") { profiles => Consented(profiles, userDAO) { user => Action.async { implicit request =>

    (courseDAO(organizationId, courseId) +& quizDAO(courseId, quizId) +& questionDAO.frameByIdEither(questionId)).flatMap{ _ match {
      case Left(notFoundResult) => Future.successful(notFoundResult)
      case Right((course, quiz, question)) =>

//        AnswerCreate.form.bindFromRequest.fold(
//          errors => Future.successful(BadRequest(views.html.errors.formErrorPage(errors))),
//          form => {
//            QuestionCreate.questionFormat.reads(Json.parse(form)) match {
//              case JsError(errors) => Future.successful(BadRequest(views.html.errors.jsonErrorPage(errors)))
//              case JsSuccess(value, path) => {
//                val questionFrameFuture = questionDAO.insert(QuestionFrame(value, user.id))
//                questionFrameFuture.flatMap(questionFrame => {
//                  quizDAO.attach(questionFrame.question, quiz, user.id)
//                  Future.successful(Redirect(controllers.quiz.routes.QuizController.view(organizationId, course.id, quizId, None)))
//                })
//              }
//            }
//          }
//        )
        Future.successful(Redirect(controllers.quiz.routes.QuizController.view(organizationId, course.id, quizId, None)))
      }
    }

  } } } }

//  def view(organizationId: OrganizationId, courseId: CourseId, quizId: QuizId, questionId: QuestionId, answerIdOp: Option[AnswerId]) = RequireAccess(View, to=courseId) { Secure("RedirectUnauthenticatedClient", "Access") { profiles => Consented(profiles, userDAO) { user => Action.async { implicit request =>
//
//    (courseDAO(organizationId, courseId) +& quizDAO(quizId) +& questionDAO.frameByIdEither(questionId) +^ quizDAO.access(user.id, quizId)).map{ _ match {
//      case Left(notFoundResult) => notFoundResult
//      case Right((course, quiz, question, access)) => Ok(views.html.quiz.viewQuestionForCourse(access, course, quiz, question))
//      }
//    }
//
//  } } } }

}

object AnswerCreate {
  val answerJson = "answer-json"
}

