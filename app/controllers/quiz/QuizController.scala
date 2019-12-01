package controllers.quiz

import javax.inject._
import _root_.controllers.support.{Consented, RequireAccess}
import com.artclod.slick.JodaUTC
import dao.organization.{CourseDAO, OrganizationDAO}
import dao.user.UserDAO
import models._
import org.pac4j.core.config.Config
import org.pac4j.core.profile.CommonProfile
import org.pac4j.play.scala.{Security, SecurityComponents}
import org.pac4j.play.store.PlaySessionStore
import play.api.mvc._
import play.libs.concurrent.HttpExecutionContext
import com.artclod.util._
import controllers.ApplicationInfo
import controllers.library.QuestionLibraryResponses
import controllers.organization.CourseCreate
import dao.quiz._
import models.organization.Course
import models.quiz._
import org.joda.time.DateTime
import play.api.data._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.JodaForms._
import play.api.libs.json.{JsError, JsSuccess, Json}
import controllers.library.QuestionLibrary.QuestionLibraryResponse
import play.twirl.api.Html
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Right


@Singleton
class QuizController @Inject()(/*val config: Config, val playSessionStore: PlaySessionStore, override val ec: HttpExecutionContext*/ val controllerComponents: SecurityComponents, userDAO: UserDAO, organizationDAO: OrganizationDAO, courseDAO: CourseDAO, quizDAO: QuizDAO, answerDAO: AnswerDAO, skillDAO: SkillDAO, questionDAO: QuestionDAO)(implicit executionContext: ExecutionContext) extends BaseController with Security[CommonProfile]  {


  def createForm(organizationId: OrganizationId, courseId: CourseId) = RequireAccess(Edit, to=courseId) { Secure(ApplicationInfo.defaultSecurityClients, "Access").async { authenticatedRequest => Consented(authenticatedRequest, userDAO) { implicit user => Action.async { implicit request =>

    courseDAO(organizationId, courseId).map{ _ match {
      case Left(notFoundResult) => notFoundResult
      case Right(course) => Ok(views.html.quiz.createQuizForCourse(course))
      }
    }

  } } } }

  def createSubmit(organizationId: OrganizationId, courseId: CourseId) = RequireAccess(Edit, to=organizationId) { Secure(ApplicationInfo.defaultSecurityClients, "Access").async { authenticatedRequest => Consented(authenticatedRequest, userDAO) { implicit user => Action.async { implicit request =>

    courseDAO(organizationId, courseId).flatMap{ _ match {
      case Left(notFoundResult) => Future.successful(notFoundResult)
      case Right(course) =>
        QuizCreate.form.bindFromRequest.fold(
          errors => Future.successful(BadRequest(views.html.errors.formErrorPage(errors))),
          form => {
            val now = JodaUTC.now
            quizDAO.insert(Quiz(null, user.id, form.name, form.descriptionRaw, Html(form.descriptionHtml), now, now)).flatMap(quiz => // Create the Quiz
              quizDAO.attach(course, quiz, form.viewHide, if(form.useStartDate){Some(form.startDate)}else{None}, if(form.useEndDate){Some(form.endDate)}else{None}).map( _ => // Attach it to the Course
                Redirect(controllers.quiz.routes.QuizController.view(organizationId, course.id, quiz.id, None, None)))) // Redirect to the view
          }
        )
      }
    }

  } } } }

  def view(organizationId: OrganizationId, courseId: CourseId, quizId: QuizId, answerIdOp: Option[AnswerId], questionIdOp: Option[QuestionId]) = RequireAccess(View, to=courseId) { Secure(ApplicationInfo.defaultSecurityClients, "Access").async { authenticatedRequest => Consented(authenticatedRequest, userDAO) { implicit user => Action.async { implicit request =>

    (courseDAO(organizationId, courseId) +& quizDAO(courseId, quizId) +^ quizDAO.access(user.id, quizId) +& answerDAO(answerIdOp) +^ quizDAO.attempts(quizId, user) +& questionDAO.frameByOptionIdEither(questionIdOp) ).flatMap{ _ match {
      case Left(notFoundResult) => Future.successful(notFoundResult)
      case Right((course, (course2Quiz, quiz), access, answerOp, attempts, questionFrameOp)) =>
        quizDAO.questionSummariesFor(quiz).flatMap(questions => {

          val attemptsMap = attempts.groupBy(_.questionId)

          if(!access.write) {
            Future.successful(Ok(views.html.quiz.viewQuizForCourseStudent(access, course, quiz, course2Quiz, questions, answerOp, attemptsMap)))
          } else {
            (skillDAO.allSkills +# questionDAO.questionSearchSet(user.id,"%", Seq(), Seq()) +# answerDAO.resultsTable(course, quiz) ).map(v => {
              Ok(views.html.quiz.viewQuizForCourseTeacher(access, course, quiz, course2Quiz, questions, answerOp, attemptsMap, v._1, QuestionLibraryResponses(v._2), v._3, questionFrameOp.map(QuestionJson(_))))
            })
          }

        })
      }
    }

  } } } }

  def update(organizationId: OrganizationId, courseId: CourseId, quizId: QuizId) = RequireAccess(Edit, to=courseId) { Secure(ApplicationInfo.defaultSecurityClients, "Access").async { authenticatedRequest => Consented(authenticatedRequest, userDAO) { implicit user => Action.async { implicit request =>

    (courseDAO(organizationId, courseId) +& quizDAO(quizId)).flatMap{ _ match {
      case Left(notFoundResult) => Future.successful(notFoundResult)
      case Right((course, quiz)) =>
        QuizAvailability.form.bindFromRequest.fold(
          errors => Future.successful(BadRequest(views.html.errors.formErrorPage(errors))),
          form => {
            val updateQuiz2CourseFuture = quizDAO.update(course, quiz, form.viewHide, if(form.useStartDate){Some(form.startDate)}else{None}, if(form.useEndDate){Some(form.endDate)}else{None})
            val updateQuizFuture = quizDAO.update( form.toQuiz(quiz) )
            updateQuiz2CourseFuture.flatMap(_ => updateQuizFuture.map(_ =>  Redirect(controllers.quiz.routes.QuizController.view(organizationId, course.id, quiz.id, None, None))))
          }
        )
    }
    }

  } } } }

  def attach(organizationId: OrganizationId, courseId: CourseId, quizId: QuizId, questionId: QuestionId) = RequireAccess(Edit, to=courseId) { Secure(ApplicationInfo.defaultSecurityClients, "Access").async { authenticatedRequest => Consented(authenticatedRequest, userDAO) { implicit user => Action.async { implicit request =>

    (courseDAO(organizationId, courseId) +& quizDAO(quizId) +& questionDAO(questionId)).flatMap{ _ match {
      case Left(notFoundResult) => Future.successful(notFoundResult)
      case Right((course, quiz, question)) =>
        quizDAO.attach(question, quiz, user.id).map(_ => Redirect(controllers.quiz.routes.QuizController.view(organizationId, course.id, quiz.id, None, None)))
      }
    }

  } } } }

  def attachAjax(organizationId: OrganizationId, courseId: CourseId, quizId: QuizId, questionId: QuestionId) = RequireAccess(Edit, to=quizId) { Secure(ApplicationInfo.defaultSecurityClients, "Access").async { authenticatedRequest => Consented(authenticatedRequest, userDAO) { implicit user => Action.async { implicit request =>
    import MinimalQuestionJson.minimalQuestionFormat

    (courseDAO(organizationId, courseId) +& quizDAO(quizId) +& questionDAO(questionId) +^ quizDAO.attempts(quizId, user)).flatMap{ _ match {
      case Left(notFoundResult) => Future.successful(notFoundResult)
      case Right((course, quiz, question, attempts)) =>
        quizDAO.attach(question, quiz, user.id).flatMap(_ =>
          quizDAO.questionSummariesFor(quiz).map(questions =>
            Ok(Json.toJson(MinimalQuestionJson.s(questions, None, attempts.groupBy(_.questionId))))))
      }
    }

  } } } }

  def remove(organizationId: OrganizationId, courseId: CourseId, quizId: QuizId) = RequireAccess(Edit, to=courseId) { Secure(ApplicationInfo.defaultSecurityClients, "Access").async { authenticatedRequest => Consented(authenticatedRequest, userDAO) { implicit user => Action.async { implicit request =>

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
  def quizJson(quizId: QuizId) = RequireAccess(Edit, to=quizId) { Secure(ApplicationInfo.defaultSecurityClients, "Access").async { authenticatedRequest => Consented(authenticatedRequest, userDAO) { implicit user => Action.async { implicit request =>

    ( quizDAO.frameByIdEither(quizId) ).map{ _ match {
      case Left(notFoundResult) => notFoundResult
      case Right(quiz) =>
        Ok(QuizCreateFormJson.quizFormat.writes(QuizJson(quiz)))
    } }

  } } } }

  def quizJsonCourse(organizationId: OrganizationId, courseId: CourseId, quizId: QuizId) = RequireAccess(Edit, to=quizId) { Secure(ApplicationInfo.defaultSecurityClients, "Access").async { authenticatedRequest => Consented(authenticatedRequest, userDAO) { implicit user => Action.async { implicit request =>

    (courseDAO(organizationId, courseId) +& quizDAO.frameByIdEither(quizId)).map{ _ match {
      case Left(notFoundResult) => notFoundResult
      case Right((course, quiz)) =>
        Ok(QuizCreateFormJson.quizFormat.writes(QuizJson(quiz)))
    } }

  } } } }

  def createJson(organizationId: OrganizationId, courseId: CourseId) = RequireAccess(Edit, to=courseId) { Secure(ApplicationInfo.defaultSecurityClients, "Access").async { authenticatedRequest => Consented(authenticatedRequest, userDAO) { implicit user => Action.async { implicit request =>

    (courseDAO(organizationId, courseId) +^ courseDAO.access(user.id, courseId)).map{ _ match {
      case Left(notFoundResult) => notFoundResult
      case Right((course, access)) =>
        Ok(views.html.quiz.inputQuizJson(access, course))
    } }

  } } } }

  def createSubmitJson(organizationId: OrganizationId, courseId: CourseId) = RequireAccess(Edit, to=courseId) { Secure(ApplicationInfo.defaultSecurityClients, "Access").async { authenticatedRequest => Consented(authenticatedRequest, userDAO) { implicit user => Action.async { implicit request =>

    (courseDAO(organizationId, courseId) +^ skillDAO.skillsMap).flatMap{ _ match {
      case Left(notFoundResult) => Future.successful(notFoundResult)
      case Right((course, skillsMap)) =>
        QuizCreateFormJson.form.bindFromRequest.fold(
          errors => Future.successful(BadRequest(views.html.errors.formErrorPage(errors))),
          form => {
            QuizCreateFormJson.quizFormat.reads(Json.parse(form)) match {
              case JsError(errors) => Future.successful(BadRequest(views.html.errors.jsonErrorPage(errors)))
              case JsSuccess(value, path) => {
                val quizFrameFuture = quizDAO.insert(QuizFrame(user.id, value, skillsMap), user.id)
                quizFrameFuture.flatMap(quizFrame => {
                  quizDAO.attach(course, quizFrame.quiz, false, None, None).map(_ =>
                    Redirect(controllers.quiz.routes.QuizController.view(organizationId, course.id, quizFrame.quiz.id, None, None)))
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
case class QuizCreateForm(name: String, descriptionRaw: String, descriptionHtml: String, viewHide: Boolean, useStartDate: Boolean, startDate: DateTime, useEndDate: Boolean, endDate: DateTime)

object QuizCreate {
  val name = "name"
  val descriptionRaw = "descriptionRaw"
  val descriptionHtml = "descriptionHtml"
  val viewHide = "viewHide"
  val useStartDate = "useStartDate"
  val startDate = "startDate"
  val useEndDate = "useEndDate"
  val endDate = "endDate"

  val form : Form[QuizCreateForm] = Form(
    mapping(
      name ->            nonEmptyText,
      descriptionRaw ->  text,
      descriptionHtml -> text,
      viewHide ->        boolean,
      useStartDate ->    boolean,
      startDate ->       jodaDate("yyyy-MM-dd'T'HH:mm:ss.SSSZZ"),
      useEndDate ->      boolean,
      endDate ->         jodaDate("yyyy-MM-dd'T'HH:mm:ss.SSSZZ")
    )(QuizCreateForm.apply)(QuizCreateForm.unapply)
  )
}

// -----------
case class QuizUpdateForm(name: String, descriptionRaw: String, descriptionHtml: String, viewHide: Boolean, useStartDate: Boolean, startDate: DateTime, useEndDate: Boolean, endDate: DateTime) {
  def toQuiz(quiz: Quiz, now: DateTime = JodaUTC.now) = quiz.copy(name = name, descriptionRaw = descriptionRaw, descriptionHtml = Html(descriptionHtml), updateDate = now)
}

object QuizAvailability {
  val name = "name"
  val descriptionRaw = "descriptionRaw"
  val descriptionHtml = "descriptionHtml"
  val viewHide = "viewHide"
  val useStartDate = "useStartDate"
  val startDate = "startDate"
  val useEndDate = "useEndDate"
  val endDate = "endDate"

  val form : Form[QuizUpdateForm] = Form(
    mapping(
      name ->            nonEmptyText,
      descriptionRaw ->  text,
      descriptionHtml -> text,
      viewHide ->     boolean,
      useStartDate -> boolean,
      startDate ->    jodaDate("yyyy-MM-dd'T'HH:mm:ss.SSSZZ"),
      useEndDate ->   boolean,
      endDate ->      jodaDate("yyyy-MM-dd'T'HH:mm:ss.SSSZZ")
    )(QuizUpdateForm.apply)(QuizUpdateForm.unapply)
  )
}



// -----------
object QuizCreateFormJson {
  val data = "data"

  val form : Form[String] =
    Form(data -> nonEmptyText)

  import QuestionCreate.questionFormat
  implicit val quizFormat = Json.format[QuizJson]
}