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
import dao.quiz.{AnswerDAO, QuestionDAO, QuizDAO, SkillDAO}
import models.organization.Course
import models.quiz.{QuestionFrame, Quiz, QuizFrame}
import org.joda.time.DateTime
import play.api.data._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.Forms.jodaDate
import play.api.data.Forms.optional
import play.api.libs.json.{JsError, JsSuccess, Json}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Right


@Singleton
class QuizController @Inject()(val config: Config, val playSessionStore: PlaySessionStore, override val ec: HttpExecutionContext, userDAO: UserDAO, organizationDAO: OrganizationDAO, courseDAO: CourseDAO, quizDAO: QuizDAO, answerDAO: AnswerDAO, skillDAO: SkillDAO)(implicit executionContext: ExecutionContext) extends Controller with Security[CommonProfile]  {


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
            quizDAO.insert(Quiz(null, user.id, form.name, now, now)).flatMap(quiz => // Create the Quiz
              quizDAO.attach(course, quiz, form.viewHide, if(form.useStartDate){Some(form.startDate)}else{None}, if(form.useEndDate){Some(form.endDate)}else{None}).map( _ => // Attach it to the Course
                Redirect(controllers.quiz.routes.QuizController.view(organizationId, course.id, quiz.id, None)))) // Redirect to the view
          }
        )
      }
    }

  } } } }

  def view(organizationId: OrganizationId, courseId: CourseId, quizId: QuizId, answerIdOp: Option[AnswerId]) = RequireAccess(View, to=courseId) { Secure("RedirectUnauthenticatedClient", "Access") { profiles => Consented(profiles, userDAO) { user => Action.async { implicit request =>

    (courseDAO(organizationId, courseId) +& quizDAO(courseId, quizId) +^ quizDAO.access(user.id, quizId) +& answerDAO(answerIdOp) +^ skillDAO.allSkills).flatMap{ _ match {
      case Left(notFoundResult) => Future.successful(notFoundResult)
      case Right((course, (course2Quiz, quiz), access, answerOp, skills)) =>
        quizDAO.questionSummariesFor(quiz).map(questions => Ok(views.html.quiz.viewQuizForCourse(access, course, quiz, course2Quiz, questions, answerOp, skills.map(_.name))))
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
            updateNameFuture.map(update => Redirect(controllers.quiz.routes.QuizController.view(organizationId, course.id, quiz.id, None)))
          }
        )
      }
    }

  } } } }

  def updateAttachment(organizationId: OrganizationId, courseId: CourseId, quizId: QuizId) = RequireAccess(Edit, to=courseId) { Secure("RedirectUnauthenticatedClient", "Access") { profiles => Consented(profiles, userDAO) { user => Action.async { implicit request =>

    (courseDAO(organizationId, courseId) +& quizDAO(quizId)).flatMap{ _ match {
      case Left(notFoundResult) => Future.successful(notFoundResult)
      case Right((course, quiz)) =>
        QuizAvailability.form.bindFromRequest.fold(
          errors => Future.successful(BadRequest(views.html.errors.formErrorPage(errors))),
          form => {
            val updateQuiz2CourseFuture = quizDAO.update(course, quiz, form.viewHide, Some(form.startDate), Some(form.endDate))
            updateQuiz2CourseFuture.map(_ =>  Redirect(controllers.quiz.routes.QuizController.view(organizationId, course.id, quiz.id, None)))
          }
        )
    }
    }

  } } } }

  def remove(organizationId: OrganizationId, courseId: CourseId, quizId: QuizId) = RequireAccess(Edit, to=courseId) { Secure("RedirectUnauthenticatedClient", "Access") { profiles => Consented(profiles, userDAO) { user => Action.async { implicit request =>

    (courseDAO(organizationId, courseId) +& quizDAO(quizId)).flatMap{ _ match {
      case Left(notFoundResult) => Future.successful(notFoundResult)
      case Right((course, quiz)) =>
        quizDAO.detach(course, quiz).map(update => Redirect(controllers.organization.routes.CourseController.view(organizationId, course.id)))
      }
    }

  } } } }

  /**
    * Get the Quiz as JSON
    * @param quizId id of the quiz
    * @return HTTP OK with question JSON as the body
    */
  def quizJson(quizId: QuizId) = RequireAccess(Edit, to=quizId) { Secure("RedirectUnauthenticatedClient", "Access") { profiles => Consented(profiles, userDAO) { user => Action.async { implicit request =>

    ( quizDAO.frameByIdEither(quizId) ).map{ _ match {
      case Left(notFoundResult) => notFoundResult
      case Right(quiz) =>
        Ok(QuizCreateFromJson.quizFormat.writes(QuizJson(quiz)))
    } }

  } } } }

  def quizJsonCourse(organizationId: OrganizationId, courseId: CourseId, quizId: QuizId) = RequireAccess(Edit, to=quizId) { Secure("RedirectUnauthenticatedClient", "Access") { profiles => Consented(profiles, userDAO) { user => Action.async { implicit request =>

    (courseDAO(organizationId, courseId) +& quizDAO.frameByIdEither(quizId)).map{ _ match {
      case Left(notFoundResult) => notFoundResult
      case Right((course, quiz)) =>
        Ok(QuizCreateFromJson.quizFormat.writes(QuizJson(quiz)))
    } }

  } } } }

  def createJson(organizationId: OrganizationId, courseId: CourseId) = RequireAccess(Edit, to=courseId) { Secure("RedirectUnauthenticatedClient", "Access") { profiles => Consented(profiles, userDAO) { user => Action.async { implicit request =>

    (courseDAO(organizationId, courseId) +^ courseDAO.access(user.id, courseId)).map{ _ match {
      case Left(notFoundResult) => notFoundResult
      case Right((course, access)) =>
        Ok(views.html.quiz.inputQuizJson(access, course))
    } }

  } } } }

  def createSubmitJson(organizationId: OrganizationId, courseId: CourseId) = RequireAccess(Edit, to=courseId) { Secure("RedirectUnauthenticatedClient", "Access") { profiles => Consented(profiles, userDAO) { user => Action.async { implicit request =>

    (courseDAO(organizationId, courseId) +^ skillDAO.skillsMap).flatMap{ _ match {
      case Left(notFoundResult) => Future.successful(notFoundResult)
      case Right((course, skillsMap)) =>
        QuizCreateFromJson.form.bindFromRequest.fold(
          errors => Future.successful(BadRequest(views.html.errors.formErrorPage(errors))),
          form => {
            QuizCreateFromJson.quizFormat.reads(Json.parse(form)) match {
              case JsError(errors) => Future.successful(BadRequest(views.html.errors.jsonErrorPage(errors)))
              case JsSuccess(value, path) => {
                val quizFrameFuture = quizDAO.insert(QuizFrame(user.id, value, skillsMap))
                quizFrameFuture.flatMap(quizFrame => {
                  quizDAO.attach(course, quizFrame.quiz, false, None, None).map(_ =>
                    Redirect(controllers.quiz.routes.QuizController.view(organizationId, course.id, quizFrame.quiz.id, None)))
                })
              }
            }
          }
        )
    }
    }

  } } } }

}

// -----------
case class QuizCreateForm(name: String, viewHide: Boolean, useStartDate: Boolean, startDate: DateTime, useEndDate: Boolean, endDate: DateTime)

object QuizCreate {
  val name = "name"
  val viewHide = "viewHide"
  val useStartDate = "useStartDate"
  val startDate = "startDate"
  val useEndDate = "useEndDate"
  val endDate = "endDate"

  val form : Form[QuizCreateForm] = Form(
    mapping(
      name ->         nonEmptyText,
      viewHide ->     boolean,
      useStartDate -> boolean,
      startDate ->    jodaDate("yyyy-MM-dd'T'HH:mm:ss.SSSZZ"),
      useEndDate ->   boolean,
      endDate ->      jodaDate("yyyy-MM-dd'T'HH:mm:ss.SSSZZ")
    )(QuizCreateForm.apply)(QuizCreateForm.unapply)
  )
}

// -----------
object QuizRename {
  val name = "name"

  val form : Form[String] = Form(name -> nonEmptyText)
}

// -----------
case class QuizAvailabilityForm(viewHide: Boolean, useStartDate: Boolean, startDate: DateTime, useEndDate: Boolean, endDate: DateTime)

object QuizAvailability {
  val viewHide = "viewHide"
  val useStartDate = "useStartDate"
  val startDate = "startDate"
  val useEndDate = "useEndDate"
  val endDate = "endDate"

  val form : Form[QuizAvailabilityForm] = Form(
    mapping(
      viewHide ->     boolean,
      useStartDate -> boolean,
      startDate ->    jodaDate("yyyy-MM-dd'T'HH:mm:ss.SSSZZ"),
      useEndDate ->   boolean,
      endDate ->      jodaDate("yyyy-MM-dd'T'HH:mm:ss.SSSZZ")
    )(QuizAvailabilityForm.apply)(QuizAvailabilityForm.unapply)
  )
}

// -----------
case class QuizJson(name: String, questions: Vector[QuestionJson])

object QuizJson {

  def apply(quizFrame: QuizFrame) : QuizJson =
    QuizJson(
      quizFrame.quiz.name,
      quizFrame.questions.map(QuestionJson(_))
    )

}

// -----------
object QuizCreateFromJson {
  val data = "data"

  val form : Form[String] =
    Form(data -> nonEmptyText)

  import QuestionCreate.questionFormat
  implicit val quizFormat = Json.format[QuizJson]
}