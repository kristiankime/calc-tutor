package controllers.quiz

import javax.inject._

import _root_.controllers.support.{Consented, RequireAccess}
import com.artclod.mathml.MathML
import com.artclod.slick.{JodaUTC, NumericBoolean}
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
import controllers.quiz.QuestionCreate.questionJson
import dao.quiz.{AnswerDAO, QuestionDAO, QuizDAO}
import models.organization.Course
import models.quiz._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.{JsError, JsNumber, JsSuccess, Json}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Random, Right}


@Singleton
class AnswerController @Inject()(val config: Config, val playSessionStore: PlaySessionStore, override val ec: HttpExecutionContext, userDAO: UserDAO, organizationDAO: OrganizationDAO, courseDAO: CourseDAO, quizDAO: QuizDAO, questionDAO: QuestionDAO, answerDAO: AnswerDAO)(implicit executionContext: ExecutionContext) extends Controller with Security[CommonProfile]  {

  def createCourseSubmit(organizationId: OrganizationId, courseId: CourseId, quizId: QuizId, questionId: QuestionId) = RequireAccess(Edit, to=organizationId) { Secure("RedirectUnauthenticatedClient", "Access") { profiles => Consented(profiles, userDAO) { user => Action.async { implicit request =>

    (courseDAO(organizationId, courseId) +& quizDAO(courseId, quizId) +& questionDAO.frameByIdEither(questionId)).flatMap{ _ match {
      case Left(notFoundResult) => Future.successful(notFoundResult)
      case Right((course, quiz, question)) =>

        AnswerCreate.form.bindFromRequest.fold(
          errors =>
            Future.successful(BadRequest(views.html.errors.formErrorPage(errors))),
          form => {
            AnswerCreate.answerFormat.reads(Json.parse(form)) match {
              case JsError(errors) => Future.successful(BadRequest(views.html.errors.jsonErrorPage(errors)))
              case JsSuccess(value, path) => {
                val answerFrame = AnswerFrame(question, value, user.id)

                if(answerFrame.correctUnknown) { // Here we are unable to determine if the question was answered correctly so we go back to the page
                  Future.successful( Ok(views.html.quiz.viewQuestionForCourse(Own, course, quiz, question, AnswerJson(answerFrame))) )
                } else {
                  answerDAO.insert(answerFrame).map(answerFrame => {
                    if (answerFrame.answer.correct) { // Here everything was correct so go back to the quiz itself
                      Redirect(controllers.quiz.routes.QuizController.view(organizationId, course.id, quizId, Some(answerFrame.answer.id)))
                    } else { // Here an answer was wrong so give the user another chance to answer
                      Redirect(controllers.quiz.routes.QuestionController.view(organizationId, course.id, quizId, questionId, Some(answerFrame.answer.id)))
                    }

                  })
                }

              }
            }
          }
        )

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

  val form : Form[String] = Form(answerJson -> nonEmptyText)

  // all
  val id = "id"
  val correct = "correct"

  // Question
  val sections = "sections"

  // Section
  val choiceOrFunction = "choiceOrFunction"
  val choiceIndex = "choiceIndex"
  val functions = "functions"

  // Parts
  val functionRaw = "functionRaw"
  val functionMath = "functionMath"

  implicit val answerPartFunctionFormat = Json.format[AnswerPartFunctionJson]
  implicit val answerSectionFormat = Json.format[AnswerSectionJson]
  implicit val answerFormat = Json.format[AnswerJson]
}

// === AnswerJson
case class AnswerJson(sections: Vector[AnswerSectionJson], correct: Int) {
  if(sections.size == 0) {throw new IllegalArgumentException("Answers must have at least one section")}
}

object AnswerJson {
  // These can be stored in the db
  val correctYes = NumericBoolean.T
  val correctNo = NumericBoolean.F
  // These are temporary values only used in the Json/Ui
  val correctUnknown = NumericBoolean.Unknown
  val correctBlank = NumericBoolean.Blank

  val noChoiceSelected : Short = -1

  // === Build a "Blank" Answer from the question
  def blank(questionFrame: QuestionFrame): AnswerJson = {
    AnswerJson(questionFrame.sections.map(sectionFrame => AnswerSectionJson.blank(sectionFrame)), correct = correctBlank)
  }

  // === Easier Builder
  def apply(correct: Int, sections: AnswerSectionJson*) : AnswerJson = AnswerJson(Vector(sections:_*), correct)

  // === Filled in from previous answer
  def apply(answerFrame: AnswerFrame) : AnswerJson =
    AnswerJson(answerFrame.sections.map(s => AnswerSectionJson(s)), answerFrame.answer.correctNum)
}

// === AnswerSectionJson
case class AnswerSectionJson(choiceIndex: Int, functions: Vector[AnswerPartFunctionJson], correct: Int)

object AnswerSectionJson {
  val rand = new Random(System.currentTimeMillis())

  def blank(sectionFrame: QuestionSectionFrame): AnswerSectionJson =
    AnswerSectionJson(choiceIndex = sectionFrame.choiceSize.map(v => rand.nextInt(v)).getOrElse(AnswerJson.noChoiceSelected), functions=AnswerPartFunctionJson.blank(sectionFrame.parts), correct = AnswerJson.correctBlank)

  def apply(correct: Int, choiceIndex: Int, parts: AnswerPartFunctionJson*) : AnswerSectionJson =
    AnswerSectionJson(choiceIndex, Vector(parts:_*), correct)

  def apply(answerSectionFrame: AnswerSectionFrame) : AnswerSectionJson =
    AnswerSectionJson(
      choiceIndex = answerSectionFrame.answerSection.choice.getOrElse(AnswerJson.noChoiceSelected).toShort,
      functions = answerSectionFrame.parts.map(p => AnswerPartFunctionJson(p)),
      correct = answerSectionFrame.answerSection.correctNum )

}

// === AnswerPartFunctionJson
case class AnswerPartFunctionJson(functionRaw: String, functionMath: String, correct: Int)

object AnswerPartFunctionJson {

  def blank(functionParts: Either[_, Vector[QuestionPartFunction]]): Vector[AnswerPartFunctionJson] = functionParts match {
    case Left(_) => Vector()
    case Right(fps) => fps.map(p => AnswerPartFunctionJson("", "", AnswerJson.correctBlank))
  }

  def apply(function: String, correct: Int) : AnswerPartFunctionJson =
    AnswerPartFunctionJson(function, MathML(function).get.toString, correct)

  def apply(answerPartFunction: AnswerPart) : AnswerPartFunctionJson =
    AnswerPartFunctionJson(answerPartFunction.functionRaw, answerPartFunction.functionMath.toString, answerPartFunction.correctNum)

}