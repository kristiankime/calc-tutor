package controllers.quiz

import javax.inject._

import _root_.controllers.support.{Consented, RequireAccess}
import com.artclod.markup.Markdowner
import com.artclod.mathml.MathML
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
import models.quiz._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.{JsError, JsSuccess, Json}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Right


@Singleton
class QuestionController @Inject()(val config: Config, val playSessionStore: PlaySessionStore, override val ec: HttpExecutionContext, userDAO: UserDAO, organizationDAO: OrganizationDAO, courseDAO: CourseDAO, quizDAO: QuizDAO, questionDAO: QuestionDAO, answerDAO: AnswerDAO, skillDAO: SkillDAO)(implicit executionContext: ExecutionContext) extends Controller with Security[CommonProfile]  {

  def createCourseSubmit(organizationId: OrganizationId, courseId: CourseId, quizId: QuizId) = RequireAccess(Edit, to=courseId) { Secure("RedirectUnauthenticatedClient", "Access") { profiles => Consented(profiles, userDAO) { implicit user => Action.async { implicit request =>

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

  def view(organizationId: OrganizationId, courseId: CourseId, quizId: QuizId, questionId: QuestionId, answerIdOp: Option[AnswerId]) = Secure("RedirectUnauthenticatedClient", "Access") { profiles => Consented(profiles, userDAO) { implicit user => Action.async { implicit request =>

    (courseDAO(organizationId, courseId) +& quizDAO(quizId) +& questionDAO.frameByIdEither(questionId) +& answerDAO.frameByIdEither(questionId, answerIdOp)).map{ _ match {
      case Left(notFoundResult) => notFoundResult
      case Right((course, quiz, question, answerOp)) =>
        val answerJson : AnswerJson = answerOp.map(a => AnswerJson(a)).getOrElse(controllers.quiz.AnswerJson.blank(question))
        Ok(views.html.quiz.viewQuestionForCourse(Own, course, quiz, question, answerJson ))
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

  def questionJsonCourse(organizationId: OrganizationId, courseId: CourseId, quizId: QuizId, questionId: QuestionId) = RequireAccess(Edit, to=quizId /* TODO figure out access to questions */ ) { Secure("RedirectUnauthenticatedClient", "Access") { profiles => Consented(profiles, userDAO) { user => Action.async { implicit request =>

    (courseDAO(organizationId, courseId) +& quizDAO(quizId) +& questionDAO.frameByIdEither(questionId)).map{ _ match {
      case Left(notFoundResult) => notFoundResult
      case Right((course, quiz, question)) =>
        Ok(QuestionCreate.questionFormat.writes(QuestionJson(question)))
    } }

  } } } }

  def remove(organizationId: OrganizationId, courseId: CourseId, quizId: QuizId, questionId: QuestionId) = RequireAccess(Edit, to=quizId) { Secure("RedirectUnauthenticatedClient", "Access") { profiles => Consented(profiles, userDAO) { implicit user => Action.async { implicit request =>

    (courseDAO(organizationId, courseId) +& quizDAO(quizId) +& questionDAO(questionId)).flatMap{ _ match {
      case Left(notFoundResult) => Future.successful(notFoundResult)
      case Right((course, quiz, question)) =>

        val detachFuture = quizDAO.detach(questionId, quiz)
        detachFuture.map(update => Redirect(controllers.quiz.routes.QuizController.view(organizationId, course.id, quiz.id, None)))
    } }

  } } } }

}

// === QuestionJson
case class QuestionJson(title: String, descriptionRaw: String, descriptionHtml: String, sections: Vector[QuestionSectionJson], skills: Vector[String]) {
  if(sections.size == 0) { throw new IllegalArgumentException("Questions must have at least one section")}
  if(skills.size == 0) { throw new IllegalArgumentException("Questions must have at least one skill")}
}

object QuestionJson {

  def apply(title: String, description: String, sections: Seq[QuestionSectionJson], skills: Seq[String]) : QuestionJson = QuestionJson(title, description, Markdowner.string(description), Vector(sections:_*), Vector(skills:_*))

  def apply(questionFrame: QuestionFrame) : QuestionJson = {
    val sections = questionFrame.sections.map(s => QuestionSectionJson(s))
    val skills = questionFrame.skills.map((s => s.name))
    QuestionJson(questionFrame.question.title, questionFrame.question.descriptionRaw, questionFrame.question.descriptionHtml.toString, sections, skills)
  }

}

// === QuestionSectionJson
case class QuestionSectionJson(explanationRaw: String, explanationHtml: String, choiceOrFunction: String, correctChoiceIndex: Int, choices: Vector[QuestionPartChoiceJson], functions: Vector[QuestionPartFunctionJson]) {
  choiceOrFunction match {
    case QuestionCreate.choice => {
      if(correctChoiceIndex < 0 || correctChoiceIndex >= choices.size){ throw new IllegalArgumentException("Choice index was no in correct range")}
      if(choices.size == 0) {throw new IllegalArgumentException() }
    }
    case QuestionCreate.function => null
    case _ => throw new IllegalArgumentException("choiceOrFunction was not recognized type [" + choiceOrFunction + "]")
  }
}

object QuestionSectionJson {

  def apply(explanation: String, correctChoiceIndex : Int = -1)(choices: QuestionPartChoiceJson*)(functions: QuestionPartFunctionJson*) : QuestionSectionJson = {
    (correctChoiceIndex, choices.nonEmpty, functions.nonEmpty) match {
     case(i,  true,  false) if i >= 0 && i < choices.size => QuestionSectionJson(explanation, Markdowner.string(explanation), QuestionCreate.choice,   correctChoiceIndex, Vector(choices:_*), Vector())
     case(-1, false, true )                               => QuestionSectionJson(explanation, Markdowner.string(explanation), QuestionCreate.function,                -1 ,           Vector(), Vector(functions:_*))
     case _ => throw new IllegalArgumentException("Not a valid QuestionSectionJson combo")
    }
  }

  def apply(questionSectionFrame: QuestionSectionFrame) : QuestionSectionJson = {
    val choices : Vector[QuestionPartChoiceJson] = questionSectionFrame.parts.left.getOrElse(Vector()).map(c => QuestionPartChoiceJson(c))
    val functions : Vector[QuestionPartFunctionJson] = questionSectionFrame.parts.right.getOrElse(Vector()).map(f => QuestionPartFunctionJson(f))
    QuestionSectionJson(
      questionSectionFrame.section.explanationRaw,
      questionSectionFrame.section.explanationHtml.toString,
      if(questionSectionFrame.parts.isLeft){QuestionCreate.choice}else{QuestionCreate.function},
      questionSectionFrame.correctIndex.getOrElse(-1),
      choices,
      functions)
  }

}

// === QuestionPartChoiceJson
case class QuestionPartChoiceJson(summaryRaw: String, summaryHtml: String)

object QuestionPartChoiceJson {

  def apply(summary: String) : QuestionPartChoiceJson = QuestionPartChoiceJson(summary, Markdowner.string(summary))

  def apply(questionPartChoice: QuestionPartChoice) : QuestionPartChoiceJson =
    QuestionPartChoiceJson(
      questionPartChoice.summaryRaw,
      questionPartChoice.summaryHtml.toString)
}

// === QuestionPartFunctionJson
case class QuestionPartFunctionJson(summaryRaw: String, summaryHtml: String, functionRaw: String, functionMath: String)

object QuestionPartFunctionJson {

  def apply(summary: String, function: String) : QuestionPartFunctionJson = QuestionPartFunctionJson(summary, Markdowner.string(summary), function, MathML(function).get.toString)

  def apply(questionPartFunction: QuestionPartFunction) : QuestionPartFunctionJson =
    QuestionPartFunctionJson(
      questionPartFunction.summaryRaw,
      questionPartFunction.summaryHtml.toString,
      questionPartFunction.functionRaw,
      questionPartFunction.functionMath.toString)

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

  // Section
  val explanationRaw = "explanationRaw"
  val explanationHtml = "explanationHtml"
  val choiceOrFunction = "choiceOrFunction"
  val correctChoiceIndex = "correctChoiceIndex"
  val choices = "choices"
  val functions = "functions"

  // Parts
  val summaryRaw = "summaryRaw"
  val summaryHtml = "summaryHtml"
  val functionRaw = "functionRaw"
  val functionMath = "functionMath"

  // Function versus choice
  val function = "function"
  val choice = "choice"

  implicit val questionPartChoiceFormat = Json.format[QuestionPartChoiceJson]
  implicit val questionPartFunctionFormat = Json.format[QuestionPartFunctionJson]
  implicit val questionSectionFormat = Json.format[QuestionSectionJson]
  implicit val questionFormat = Json.format[QuestionJson]
}

