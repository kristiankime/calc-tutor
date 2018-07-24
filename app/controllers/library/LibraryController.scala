package controllers.library

import javax.inject.{Inject, Singleton}

import com.artclod.mathml.MathML
import com.artclod.slick.JodaUTC
import controllers.Application
import controllers.organization.CourseJoin
import controllers.quiz.{AnswerCreate, AnswerJson, QuestionCreate, QuizAvailability}
import controllers.support.{Consented, RequireAccess}
import dao.organization.{CourseDAO, OrganizationDAO}
import dao.quiz._
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
import controllers.library.QuestionLibrary.QuestionLibraryResponse
import models.quiz._
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.twirl.api.Html
import views.html.library.list.libraryList
import views.html.library.catalog

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Random, Right, Success}

@Singleton
class LibraryController @Inject()(val config: Config, val playSessionStore: PlaySessionStore, override val ec: HttpExecutionContext, userDAO: UserDAO, organizationDAO: OrganizationDAO, courseDAO: CourseDAO, quizDAO: QuizDAO, skillDAO: SkillDAO, questionDAO: QuestionDAO, answerDAO: AnswerDAO)(implicit executionContext: ExecutionContext) extends Controller with Security[CommonProfile] {

  def list() = Secure("RedirectUnauthenticatedClient", "Access") { profiles => Consented(profiles, userDAO) { implicit user => Action.async { implicit request =>
    skillDAO.allSkills.flatMap(skills => { questionDAO.questionSearchSet("%", Seq(), Seq()).map(qsl => {
        Ok(views.html.library.catalog(skills, QuestionLibraryResponses(qsl), views.html.library.list.libraryList.apply(skills)))
      })})
    }}}

  def createQuestionView() = Secure("RedirectUnauthenticatedClient", "Access") { profiles => Consented(profiles, userDAO) { implicit user => Action.async { implicit request =>
    skillDAO.allSkills.map(skills => Ok(views.html.library.createQuestion(skills)))
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
    (questionDAO.frameByIdEither(questionId) +& answerDAO.frameByIdEither(questionId, answerIdOp) +^ answerDAO.attempts(user.id, questionId)).map( _ match {
      case Left(notFoundResult) => notFoundResult
      case Right((question, answerOp, attempts)) => {
        val answerJson : AnswerJson = answerOp.map(a => AnswerJson(a)).getOrElse(controllers.quiz.AnswerJson.blank(question))
        Ok(views.html.library.viewQuestion(question, answerJson, attempts))
      }
    })
  }}}


  def answerQuestion(questionId: QuestionId) = Secure("RedirectUnauthenticatedClient", "Access") { profiles => Consented(profiles, userDAO) { implicit user => Action.async { implicit request =>

    (questionDAO.frameByIdEither(questionId) +^ answerDAO.attempts(user.id, questionId)).flatMap{ _ match {
      case Left(notFoundResult) => Future.successful(notFoundResult)
      case Right( (question, attempts) ) =>
        AnswerCreate.form.bindFromRequest.fold(
          errors => Future.successful(BadRequest(views.html.errors.formErrorPage(errors))),
          form => {
            AnswerCreate.answerFormat.reads(Json.parse(form)) match {
              case JsError(errors) => Future.successful(BadRequest(views.html.errors.jsonErrorPage(errors)))
              case JsSuccess(value, path) => {
                val protoAnswerFrame = AnswerFrame(question, value, user.id)

                if(protoAnswerFrame.correctUnknown) { // Here we are unable to determine if the question was answered correctly so we go back to the page
                  Future.successful( Ok(views.html.library.viewQuestion(question, AnswerJson(protoAnswerFrame), attempts)) )
                } else {
                  answerDAO.updateSkillCounts(user.id, questionId, protoAnswerFrame.answer.correct).flatMap( updated => { // Keep track of the in/correct counts for each skill
                    answerDAO.insert(protoAnswerFrame).map(answerFrame => {
                      Redirect(controllers.library.routes.LibraryController.viewQuestion(questionId, Some(answerFrame.answer.id)))
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
  import QuestionLibrary.QuestionLibraryRequest
  import QuestionLibrary.QuestionLibraryResponse
  implicit val formatQuestionListRequest = QuestionLibrary.formatQuestionLibraryRequest;
  implicit val formatQuestionListResponse = QuestionLibrary.formatQuestionLibraryResponse;

  def questionListAjax() = Action.async { request =>
    request.body.asJson.map { jsonBody =>
      jsonBody.validate[QuestionLibraryRequest].map { qLR =>
        qLR.student match {
          case None => questionDAO.questionSearchSet(qLR.titleQuery, qLR.requiredSkills, qLR.bannedSkills).map(qsl => { Ok(Json.toJson(QuestionLibraryResponses(qsl))) })
          case Some(studentId) => {
            skillDAO.skillCountsMaps(UserId(studentId)).flatMap(skillCounts => {
              questionDAO.questionSearchSet(qLR.titleQuery, qLR.requiredSkills, qLR.bannedSkills).map(qsl => {
                Ok(Json.toJson(QuestionLibraryResponses(skillCounts, qsl)))
              })
            })
          }
        }
      }.recoverTotal { e => Future.successful(BadRequest("Json Parse Error:" + JsError.toJson(e))) }
    }.getOrElse( Future.successful(BadRequest("Expecting Json data")))
  }

}

object QuestionLibraryResponses {

  def apply(qsl:  Seq[(Question, Set[Skill])]): Seq[QuestionLibraryResponse] = {
    qsl.map(qs => QuestionLibraryResponse(qs._1.id.v, qs._1.title, qs._2.map(_.name), None))
  }

  def apply(countsMap: Map[SkillId, UserAnswerCount], qsl: Seq[(Question, Set[Skill])]) = {
    qsl.map(qs => {
      val prob = PFAComputations.pfaProbability(countsMap, qs._2)
      QuestionLibraryResponse(qs._1.id.v, qs._1.title, qs._2.map(_.name), Some(prob))

    })
  }

}

object QuestionLibrary {
  // Request Fields
  val titleQuery = "titleQuery"
  val requiredSkills = "requiredSkills"
  val bannedSkills = "bannedSkills"
  val student = "student"
  // Response Fields
  val id = "id"
  val title = "title"
  val skills = "skills"
  val chance = "chance"

  case class QuestionLibraryRequest(titleQuery: String, requiredSkills: Seq[String], bannedSkills: Seq[String], student: Option[Long])
  case class QuestionLibraryResponse(id: Long, title: String, skills: Set[String], chance: Option[Double])

  implicit val formatQuestionLibraryRequest = Json.format[QuestionLibraryRequest]
  implicit val formatQuestionLibraryResponse = Json.format[QuestionLibraryResponse]
}
