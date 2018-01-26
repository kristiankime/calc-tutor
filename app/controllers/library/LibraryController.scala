package controllers.library

import javax.inject.{Inject, Singleton}

import com.artclod.mathml.MathML
import com.artclod.slick.JodaUTC
import controllers.Application
import controllers.organization.CourseJoin
import controllers.quiz.{AnswerCreate, AnswerJson, QuestionCreate, QuizAvailability}
import controllers.support.{Consented, RequireAccess}
import dao.organization.{CourseDAO, OrganizationDAO}
import dao.quiz.{AnswerDAO, QuestionDAO, QuizDAO, SkillDAO}
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
import models.quiz.{AnswerFrame, Question, QuestionFrame, Skill}
import play.api.libs.json.{JsError, JsSuccess, Json}

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Random, Right, Success}

@Singleton
class LibraryController @Inject()(val config: Config, val playSessionStore: PlaySessionStore, override val ec: HttpExecutionContext, userDAO: UserDAO, organizationDAO: OrganizationDAO, courseDAO: CourseDAO, quizDAO: QuizDAO, skillDAO: SkillDAO, questionDAO: QuestionDAO, answerDAO: AnswerDAO)(implicit executionContext: ExecutionContext) extends Controller with Security[CommonProfile] {

  def list() = Secure("RedirectUnauthenticatedClient", "Access") { profiles => Consented(profiles, userDAO) { implicit user => Action.async { implicit request =>
    skillDAO.allSkills.flatMap(skills => { questionDAO.skillsForAllSet().flatMap(questionsAndSkills => { questionDAO.questionSearchSet("%", Seq()).map(qsl => {
            Ok(views.html.library.list(skills, questionsAndSkills, QuestionListResponses(qsl)))
      })})})
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
                  Redirect(controllers.library.routes.LibraryController.list())
                })})
              }
            }
          }
        )

  } } }

  def viewQuestion(questionId: QuestionId, answerIdOp: Option[AnswerId]) = Secure("RedirectUnauthenticatedClient", "Access") { profiles => Consented(profiles, userDAO) { implicit user => Action.async { implicit request =>
    (questionDAO.frameByIdEither(questionId) +& answerDAO.frameByIdEither(questionId, answerIdOp)).map( _ match {
      case Left(notFoundResult) => notFoundResult
      case Right((question, answerOp)) => {
        val answerJson : AnswerJson = answerOp.map(a => AnswerJson(a)).getOrElse(controllers.quiz.AnswerJson.blank(question))
        Ok(views.html.library.viewQuestion(question, answerJson))
      }
    })
  }}}


  def answerQuestion(questionId: QuestionId) = Secure("RedirectUnauthenticatedClient", "Access") { profiles => Consented(profiles, userDAO) { implicit user => Action.async { implicit request =>

    questionDAO.frameByIdEither(questionId).flatMap{ _ match {
      case Left(notFoundResult) => Future.successful(notFoundResult)
      case Right(question) =>
        AnswerCreate.form.bindFromRequest.fold(
          errors => Future.successful(BadRequest(views.html.errors.formErrorPage(errors))),
          form => {
            AnswerCreate.answerFormat.reads(Json.parse(form)) match {
              case JsError(errors) => Future.successful(BadRequest(views.html.errors.jsonErrorPage(errors)))
              case JsSuccess(value, path) => {
                val protoAnswerFrame = AnswerFrame(question, value, user.id)

                if(protoAnswerFrame.correctUnknown) { // Here we are unable to determine if the question was answered correctly so we go back to the page
                  Future.successful( Ok(views.html.library.viewQuestion(question, AnswerJson(protoAnswerFrame))) )
                } else {
                  answerDAO.updateSkillCounts(user.id, questionId, protoAnswerFrame.answer.correct).flatMap( updated => { // Keep track of the in/correct counts for each skill
                    answerDAO.insert(protoAnswerFrame).map(answerFrame => {
                      Redirect(controllers.library.routes.LibraryController.viewQuestion(questionId, Some(answerFrame.answer.id)))
//                      if (answerFrame.answer.correct) { // Here everything was correct
//                        Redirect(controllers.library.routes.LibraryController.viewQuestion(questionId, Some(answerFrame.answer.id)))
//                      } else { // Here an answer was wrong so give the user another chance to answer
//                        Redirect(controllers.library.routes.LibraryController.viewQuestion(questionId, Some(answerFrame.answer.id)))
//                      }

                    })
                  })
                }

              }
            }
          }
        )

    } }

  } } }


  // ============== Questions List (Json Ajax) ============
  import QuestionList.QuestionListRequest
  import QuestionList.QuestionListResponse
  implicit val formatQuestionListRequest = QuestionList.formatQuestionListRequest;
  implicit val formatQuestionListResponse = QuestionList.formatQuestionListResponse;

  object QuestionListResponses {
    def apply(qsl:  Seq[(Question, Set[Skill])]): Seq[QuestionListResponse] = {
      qsl.map(qs => QuestionListResponse(qs._1.id.v, qs._1.title, qs._2.map(_.name)))
    }
  }

  def questionListAjax() = Action.async { request =>
    request.body.asJson.map { jsonBody =>
      jsonBody.validate[QuestionListRequest].map { questionListRequest =>
        questionDAO.questionSearchSet(questionListRequest.titleQuery, questionListRequest.skillQuery).map(qsl => {
          Ok(Json.toJson(QuestionListResponses(qsl)))
        })
      }.recoverTotal { e => Future.successful(BadRequest("Detected error:" + JsError.toJson(e))) }
    }.getOrElse( Future.successful(BadRequest("Expecting Json data")))
  }

}

object QuestionList {
  val titleQuery = "titleQuery"
  val skillQuery = "skillQuery"
  val id = "id"
  val title = "title"
  val skills = "skills"

  case class QuestionListRequest(titleQuery: String, skillQuery: Seq[String])
  case class QuestionListResponse(id: Long, title: String, skills: Set[String])

  implicit val formatQuestionListRequest = Json.format[QuestionListRequest]
  implicit val formatQuestionListResponse = Json.format[QuestionListResponse]
}
