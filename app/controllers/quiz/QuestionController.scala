package controllers.quiz

import javax.inject._
import _root_.controllers.support.{Consented, RequireAccess}
import com.artclod.markup.Markdowner
import com.artclod.mathml.MathML
import com.artclod.slick.JodaUTC
import dao.organization.{CourseDAO, OrganizationDAO}
import dao.user.UserDAO
import models.{quiz, _}
import org.pac4j.core.config.Config
import org.pac4j.core.profile.CommonProfile
import org.pac4j.play.scala.{Security, SecurityComponents}
import org.pac4j.play.store.PlaySessionStore
import play.api.mvc._
import play.libs.concurrent.HttpExecutionContext
import com.artclod.util._
import com.artclod.util.ofthree.{First, Second, Third}
import controllers.ApplicationInfo
import controllers.organization.CourseCreate
import dao.quiz.{AnswerDAO, QuestionDAO, QuizDAO, SkillDAO}
import models.organization.Course
import models.quiz._
import models.quiz.util.SetOfNumbers
import org.joda.time.DateTime
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.twirl.api.Html

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Right


@Singleton
class QuestionController @Inject()(/*val config: Config, val playSessionStore: PlaySessionStore, override val ec: HttpExecutionContext*/ val controllerComponents: SecurityComponents, userDAO: UserDAO, organizationDAO: OrganizationDAO, courseDAO: CourseDAO, quizDAO: QuizDAO, questionDAO: QuestionDAO, answerDAO: AnswerDAO, skillDAO: SkillDAO)(implicit executionContext: ExecutionContext) extends BaseController with Security[CommonProfile]  {

  def createCourseSubmit(organizationId: OrganizationId, courseId: CourseId, quizId: QuizId) = RequireAccess(Edit, to=courseId) { Secure(ApplicationInfo.defaultSecurityClients, "Access").async { authenticatedRequest => Consented(authenticatedRequest, userDAO) { implicit user => Action.async { implicit request =>

    (courseDAO(organizationId, courseId) +& quizDAO(courseId, quizId) +^ skillDAO.skillsMap).flatMap{ _ match {
      case Left(notFoundResult) => Future.successful(notFoundResult)
      case Right((course, (course2Quiz, quiz), skillsMap)) =>
        QuestionCreate.form.bindFromRequest.fold(
          errors => Future.successful(BadRequest(views.html.errors.formErrorPage(errors))),
          form => {
            QuestionCreate.questionFormat.reads(Json.parse(form)) match {
              case JsError(errors) => Future.successful(BadRequest(views.html.errors.jsonErrorPage(errors)))
              case JsSuccess(value, path) => {
                val questionFrameFuture = questionDAO.insert(QuestionFrame(value, user.id, skillsMap))
                questionFrameFuture.flatMap(questionFrame => {
                  quizDAO.attach(questionFrame.question, quiz, user.id).map(_ =>
                    Redirect(controllers.quiz.routes.QuizController.view(organizationId, course.id, quizId, None)))
                })
              }
            }
          }
        )
      }
    }

  } } } }

  def view(organizationId: OrganizationId, courseId: CourseId, quizId: QuizId, questionId: QuestionId, answerIdOp: Option[AnswerId]) = Secure(ApplicationInfo.defaultSecurityClients, "Access").async { authenticatedRequest => Consented(authenticatedRequest, userDAO) { implicit user => Action.async { implicit request =>

    (courseDAO(organizationId, courseId) +& quizDAO(quizId) +& questionDAO.frameByIdEither(questionId) +& answerDAO.frameByIdEither(questionId, answerIdOp) +^ answerDAO.attempts(user.id, questionId)).map{ _ match {
      case Left(notFoundResult) => notFoundResult
      case Right((course, quiz, question, answerOp, attempts)) =>
        val answerJson : AnswerJson = answerOp.map(a => AnswerJson(a)).getOrElse(controllers.quiz.AnswerJson.blank(question))
        Ok(views.html.quiz.viewQuestionForCourse(Own, course, quiz, question, answerJson, attempts))
    } }

  } } }

  /**
    * Get the Question as JSON
    * @param questionId id of the question
    * @return HTTP OK with question JSON as the body
    */
  def questionJson(questionId: QuestionId) = Action.async { implicit request => /* TODO figure out access to questions */

    ( questionDAO.frameByIdEither(questionId) ).map{ _ match {
      case Left(notFoundResult) => notFoundResult
      case Right(question) =>
        Ok(QuestionCreate.questionFormat.writes(QuestionJson(question)))
    } }

  }

  def questionJsonCourse(organizationId: OrganizationId, courseId: CourseId, quizId: QuizId, questionId: QuestionId) = RequireAccess(Edit, to=quizId /* TODO figure out access to questions */ ) { Secure(ApplicationInfo.defaultSecurityClients, "Access").async { authenticatedRequest => Consented(authenticatedRequest, userDAO) { user => Action.async { implicit request =>

    (courseDAO(organizationId, courseId) +& quizDAO(quizId) +& questionDAO.frameByIdEither(questionId)).map{ _ match {
      case Left(notFoundResult) => notFoundResult
      case Right((course, quiz, question)) =>
        Ok(QuestionCreate.questionFormat.writes(QuestionJson(question)))
    } }

  } } } }

  def remove(organizationId: OrganizationId, courseId: CourseId, quizId: QuizId, questionId: QuestionId) = RequireAccess(Edit, to=quizId) { Secure(ApplicationInfo.defaultSecurityClients, "Access").async { authenticatedRequest => Consented(authenticatedRequest, userDAO) { implicit user => Action.async { implicit request =>

    (courseDAO(organizationId, courseId) +& quizDAO(quizId) +& questionDAO(questionId)).flatMap{ _ match {
      case Left(notFoundResult) => Future.successful(notFoundResult)
      case Right((course, quiz, question)) =>

        val detachFuture = quizDAO.detach(questionId, quiz)
        detachFuture.map(update => Redirect(controllers.quiz.routes.QuizController.view(organizationId, course.id, quiz.id, None)))
    } }

  } } } }


  def removeAjax(organizationId: OrganizationId, courseId: CourseId, quizId: QuizId, questionId: QuestionId) = RequireAccess(Edit, to=quizId) { Secure(ApplicationInfo.defaultSecurityClients, "Access").async { authenticatedRequest => Consented(authenticatedRequest, userDAO) { implicit user => Action.async { implicit request =>
    implicit val minimalQuestionFormat = MinimalQuestionJson.minimalQuestionFormat

    (courseDAO(organizationId, courseId) +& quizDAO(quizId) +& questionDAO(questionId) +^ quizDAO.attempts(quizId, user)).flatMap{ _ match {
      case Left(notFoundResult) => Future.successful(notFoundResult)
      case Right((course, quiz, question, attempts)) =>
        quizDAO.detach(question.id, quiz).flatMap(_ =>
          quizDAO.questionSummariesFor(quiz).map(questions =>
            Ok(Json.toJson(MinimalQuestionJson.s(questions, None, attempts.groupBy(_.questionId))))))
    } }

  } } } }

  def studentSummary(organizationId: OrganizationId, courseId: CourseId, quizId: QuizId, questionId: QuestionId, studentId: UserId) = RequireAccess(Edit, to=quizId) { Secure(ApplicationInfo.defaultSecurityClients, "Access").async { authenticatedRequest => Consented(authenticatedRequest, userDAO) { implicit user => Action.async { implicit request =>

    (courseDAO(organizationId, courseId) +& quizDAO(quizId) +& questionDAO.frameByIdEither(questionId) +& userDAO.byIdEither(studentId) +& answerDAO.correctOrLatestEither(questionId, studentId) ).flatMap{ _ match {
      case Left(notFoundResult) => Future.successful(notFoundResult)
      case Right((course, quiz, question, student, (answer, answers))) => {
        answerDAO.frameById(answer.id).map(answerFrame => {
          Ok(views.html.quiz.viewQuestionSummaryForCourse(Own, course, quiz, question, answerFrame.get, student, answers)) })
      }
    } }

  } } } }

  def studentSummaryAnswerSpecified(organizationId: OrganizationId, courseId: CourseId, quizId: QuizId, questionId: QuestionId, answerId: AnswerId, studentId: UserId) = RequireAccess(Edit, to=quizId) { Secure(ApplicationInfo.defaultSecurityClients, "Access").async { authenticatedRequest => Consented(authenticatedRequest, userDAO) { implicit user => Action.async { implicit request =>

    (courseDAO(organizationId, courseId) +& quizDAO(quizId) +& questionDAO.frameByIdEither(questionId) +& userDAO.byIdEither(studentId) +& answerDAO.frameByIdEither(questionId, answerId) +^ answerDAO.attempts(studentId, questionId)).map{ _ match {
      case Left(notFoundResult) => notFoundResult
      case Right((course, quiz, question, student, answer, attempts)) =>
        Ok(views.html.quiz.viewQuestionSummaryForCourse(Own, course, quiz, question, answer, student, attempts))

    } }

  } } } }
}



object QuestionCreate {
  val questionJson = "question-json"

  val form : Form[String] = Form(questionJson -> nonEmptyText)

  // all
  val id = "id"

  // Question
  val title = "title"
  val descriptionRaw = "descriptionRaw"
  val descriptionHtml = "descriptionHtml"
  val sections = "sections"
  val skills = "skills"

  // User Constants


  // Section
  val explanationRaw = "explanationRaw"
  val explanationHtml = "explanationHtml"
  val partType = "partType"
  val correctChoiceIndex = "correctChoiceIndex"
  val choices = "choices"
  val functions = "functions"
  val sequences = "sequences"

  // Parts
  val summaryRaw = "summaryRaw"
  val summaryHtml = "summaryHtml"
  val functionRaw = "functionRaw"
  val functionMath = "functionMath"
  val sequenceStr = "sequenceStr"
  val sequenceMath = "sequenceMath"

  // Part Type
  val choice = "choice"
  val function = "function"
  val sequence = "sequence"

  implicit val questionPartChoiceFormat = Json.format[QuestionPartChoiceJson]
  implicit val questionPartFunctionFormat = Json.format[QuestionPartFunctionJson]
  implicit val questionPartSequenceFormat = Json.format[QuestionPartSequenceJson]
  implicit val questionSectionFormat = Json.format[QuestionSectionJson]
  implicit val questionUserConstantIntegerFormat = Json.format[QuestionUserConstantIntegerJson]
  implicit val questionUserConstantDecimalFormat = Json.format[QuestionUserConstantDecimalJson]
  implicit val questionUserConstantSetFormat = Json.format[QuestionUserConstantSetJson]
  implicit val questionUserConstantsFormat = Json.format[QuestionUserConstantsJson]
  implicit val questionFormat = Json.format[QuestionJson]
}

object QuestionArchive {
  val questionArchive = "question-archive"

  val form : Form[Short] = Form(questionArchive -> shortNumber(0,1))
}