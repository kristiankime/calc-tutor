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
import org.pac4j.play.scala.{Security, SecurityComponents}
import org.pac4j.play.store.PlaySessionStore
import play.api.mvc._
import play.libs.concurrent.HttpExecutionContext
import com.artclod.util._
import com.artclod.util.ofthree.{First, Second, Third}
import controllers.ApplicationInfo
import controllers.organization.CourseCreate
import controllers.quiz.QuestionCreate.questionJson
import dao.quiz.{AnswerDAO, QuestionDAO, QuizDAO, SkillDAO}
import models.organization.Course
import models.quiz._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.{JsError, JsNumber, JsSuccess, Json}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Random, Right}


@Singleton
class AnswerController @Inject()(/*val config: Config, val playSessionStore: PlaySessionStore, override val ec: HttpExecutionContext*/ val controllerComponents: SecurityComponents, userDAO: UserDAO, organizationDAO: OrganizationDAO, courseDAO: CourseDAO, quizDAO: QuizDAO, questionDAO: QuestionDAO, answerDAO: AnswerDAO, skillDAO: SkillDAO)(implicit executionContext: ExecutionContext) extends BaseController with Security[CommonProfile]  {

  def createCourseSubmit(organizationId: OrganizationId, courseId: CourseId, quizId: QuizId, questionId: QuestionId) = RequireAccess(Edit, to=organizationId) { Secure(ApplicationInfo.defaultSecurityClients, "Access").async { authenticatedRequest => Consented(authenticatedRequest, userDAO) { implicit user => Action.async { implicit request =>

    (courseDAO(organizationId, courseId) +& quizDAO(courseId, quizId) +& questionDAO.frameByIdEither(questionId) +^ answerDAO.attempts(user.id, questionId)).flatMap{ _ match {
      case Left(notFoundResult) => Future.successful(notFoundResult)
      case Right((course, (course2Quiz, quiz), question, attempts)) =>

        AnswerCreate.form.bindFromRequest.fold(
          errors =>
            Future.successful(BadRequest(views.html.errors.formErrorPage(errors))),
          form => {
            AnswerCreate.answerFormat.reads(Json.parse(form)) match {
              case JsError(errors) => Future.successful(BadRequest(views.html.errors.jsonErrorPage(errors)))
              case JsSuccess(value, path) => {
                val protoAnswerFrame = AnswerFrame(question, value, user.id)

                if(protoAnswerFrame.correctUnknown) { // Here we are unable to determine if the question was answered correctly so we go back to the page
                  Future.successful( Ok(views.html.quiz.viewQuestionForCourse(Own, course, quiz, question, AnswerJson(protoAnswerFrame), attempts)) )
                } else {
                  answerDAO.updateSkillCounts(user.id, questionId, protoAnswerFrame.answer.correct).flatMap( updated => { // Keep track of the in/correct counts for each skill
                    answerDAO.insert(protoAnswerFrame).map(answerFrame => {

                    if (answerFrame.answer.correct) { // Here everything was correct so go back to the quiz itself
                      Redirect(controllers.quiz.routes.QuizController.view(organizationId, course.id, quizId, Some(answerFrame.answer.id)))
                    } else { // Here an answer was wrong so give the user another chance to answer
                      Redirect(controllers.quiz.routes.QuestionController.view(organizationId, course.id, quizId, questionId, Some(answerFrame.answer.id)))
                    }

                    })
                  })
                }

              }
            }
          }
        )

      }
    }

  } } } }


  def createSelfQuizCourseSubmit(organizationId: OrganizationId, courseId: CourseId, questionId: QuestionId) = RequireAccess(Edit, to=organizationId) { Secure(ApplicationInfo.defaultSecurityClients, "Access").async { authenticatedRequest => Consented(authenticatedRequest, userDAO) { implicit user => Action.async { implicit request =>

    (courseDAO(organizationId, courseId) +& questionDAO.frameByIdEither(questionId) +^ answerDAO.attempts(user.id, questionId)).flatMap{ _ match {
      case Left(notFoundResult) => Future.successful(notFoundResult)
      case Right((course, questionFrame, attempts)) =>

        AnswerCreate.form.bindFromRequest.fold(
          errors =>
            Future.successful(BadRequest(views.html.errors.formErrorPage(errors))),
          form => {
            AnswerCreate.answerFormat.reads(Json.parse(form)) match {
              case JsError(errors) => Future.successful(BadRequest(views.html.errors.jsonErrorPage(errors)))
              case JsSuccess(value, path) => {
                val protoAnswerFrame = AnswerFrame(questionFrame, value, user.id)

                if(protoAnswerFrame.correctUnknown) { // Here we are unable to determine if the question was answered correctly so we go back to the page
                  Future.successful( Ok(views.html.organization.studentSelfQuestionForCourse(null, course, questionFrame, AnswerJson(protoAnswerFrame), attempts)) )
                } else {
                  answerDAO.updateSkillCounts(user.id, questionId, protoAnswerFrame.answer.correct).flatMap( updated => { // Keep track of the in/correct counts for each skill
                    answerDAO.insert(protoAnswerFrame).map(answerFrame => {
                      Redirect(controllers.organization.routes.CourseController.studentSelfQuestion(organizationId, course.id, questionFrame.question.id, Some(answerFrame.answer.id)))
                    })
                  })
                }

              }
            }
          }
        )

    }
    }

  } } } }

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
  val partType = "partType"
  val choiceIndex = "choiceIndex"
  val functions = "functions"
  val sequences = "sequences"

  // Parts
  val functionRaw = "functionRaw"
  val functionMath = "functionMath"
  val sequenceStr = "sequenceStr"

  implicit val answerPartFunctionFormat = Json.format[AnswerPartFunctionJson]
  implicit val answerPartSequenceFormat = Json.format[AnswerPartSequenceJson]
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
case class AnswerSectionJson(choiceIndex: Int, functions: Vector[AnswerPartFunctionJson], sequences: Vector[AnswerPartSequenceJson], correct: Int)

object AnswerSectionJson {
  val rand = new Random(System.currentTimeMillis())

  def blank(sectionFrame: QuestionSectionFrame): AnswerSectionJson =
    AnswerSectionJson(choiceIndex = sectionFrame.choiceSize.map(v => rand.nextInt(v)).getOrElse(AnswerJson.noChoiceSelected), functions=AnswerPartFunctionJson.blank(sectionFrame.parts), sequences = AnswerPartSequenceJson.blank(sectionFrame.parts), correct = AnswerJson.correctBlank)

  def apply(correct: Int, choiceIndex: Int): AnswerSectionJson =
    AnswerSectionJson(choiceIndex, Vector(), Vector(), correct)

  def apply(correct: Int, choiceIndex: Int, functionParts: Vector[AnswerPartFunctionJson], sequenceParts: Vector[AnswerPartSequenceJson]) : AnswerSectionJson =
    AnswerSectionJson(choiceIndex, Vector(functionParts:_*), Vector(sequenceParts:_*), correct)

  def apply(correct: Int, choiceIndex: Int, functionParts: AnswerPartFunctionJson*) : AnswerSectionJson =
    AnswerSectionJson(choiceIndex, Vector(functionParts:_*), Vector(), correct)

  def apply(answerSectionFrame: AnswerSectionFrame) : AnswerSectionJson =
    AnswerSectionJson(
      choiceIndex = answerSectionFrame.answerSection.choice.getOrElse(AnswerJson.noChoiceSelected).toShort,
      functions = answerSectionFrame.functionParts.map(p => AnswerPartFunctionJson(p)),
      sequences = answerSectionFrame.sequenceParts.map(p => AnswerPartSequenceJson(p)),
      correct = answerSectionFrame.answerSection.correctNum )

}

// === AnswerPartFunctionJson
case class AnswerPartFunctionJson(functionRaw: String, functionMath: String, correct: Int)

object AnswerPartFunctionJson {

  def blank(functionParts: OneOfThree[_, Vector[QuestionPartFunction], _]): Vector[AnswerPartFunctionJson] = functionParts match {
    case First(_) => Vector()
    case Second(fps) => fps.map(p => AnswerPartFunctionJson("", "", AnswerJson.correctBlank))
    case Third(_) => Vector()
  }

  def apply(function: String, correct: Int) : AnswerPartFunctionJson =
    AnswerPartFunctionJson(function, MathML(function).get.toString, correct)

  def apply(answerPartFunction: AnswerPartFunction) : AnswerPartFunctionJson =
    AnswerPartFunctionJson(answerPartFunction.functionRaw, answerPartFunction.functionMath.toString, answerPartFunction.correctNum)

}

// === AnswerPartSequenceJson
case class AnswerPartSequenceJson(sequenceStr: String, correct: Int)

object AnswerPartSequenceJson {

  def blank(sequenceParts: OneOfThree[_, _, Vector[QuestionPartSequence]]): Vector[AnswerPartSequenceJson] = sequenceParts match {
    case First(_) => Vector()
    case Second(_) => Vector()
    case Third(seq) => seq.map(p => AnswerPartSequenceJson("", AnswerJson.correctBlank))
  }

  def apply(answerPartSequence: AnswerPartSequence) : AnswerPartSequenceJson =
    AnswerPartSequenceJson(answerPartSequence.sequenceStr, answerPartSequence.correctNum)

}
