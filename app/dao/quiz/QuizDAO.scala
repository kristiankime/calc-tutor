package dao.quiz

import javax.inject.{Inject, Singleton}

import com.artclod.mathml.scalar.MathMLElem
import com.artclod.slick.JodaUTC
import dao.ColumnTypeMappings
import dao.user.UserDAO
import models.quiz.{User2Quiz, _}
import models._
import models.organization.User2Course
import org.joda.time.DateTime
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.mvc.Result
import play.api.mvc.Results._
import play.twirl.api.Html
import slick.lifted

import scala.concurrent.{ExecutionContext, Future}

// ====
import slick.driver.JdbcProfile
//import slick.jdbc.JdbcProfile // Use this after upgrading slick
// ====

@Singleton
class QuizDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider, protected val userDAO: UserDAO)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] with ColumnTypeMappings {
  // ====
  //  import profile.api._ // Use this after upgrading slick
  import dbConfig.driver.api._
  // ====

  val Quizzes = lifted.TableQuery[QuizTable]
  val User2Quizzes = lifted.TableQuery[User2QuizTable]
  val Questions = lifted.TableQuery[QuestionTable]
  val QuestionSections = lifted.TableQuery[QuestionSectionTable]
  val QuestionPartChoices = lifted.TableQuery[QuestionPartChoiceTable]
  val QuestionPartFunctions = lifted.TableQuery[QuestionPartFunctionTable]

  def all(): Future[Seq[Quiz]] = db.run(Quizzes.result)

  def insert(quiz: Quiz): Future[Quiz] = db.run(
    (Quizzes returning Quizzes.map(_.id) into ((needsId, id) => needsId.copy(id = id))) += quiz
  )

  def updateName(quiz: Quiz, name: String): Future[Int] = db.run {
    (for { z <- Quizzes if z.id === quiz.id } yield z.name ).update(name)
  }

  def byId(id : QuizId): Future[Option[Quiz]] = db.run(Quizzes.filter(_.id === id).result.headOption)

  def access(userId: UserId, quizId : QuizId): Future[Access] = db.run {
    val ownerAccess = (for(z <- Quizzes if z.ownerId === userId && z.id === quizId) yield z).result.headOption.map(_ match { case Some(_) => Own case None => Non})
    val directAccess = (for(u2z <- User2Quizzes if u2z.userId === userId && u2z.quizId === quizId) yield u2z.access).result.headOption.map(_.getOrElse(Non))
    ownerAccess.flatMap(oa => directAccess.map( da => oa max da))
  }

  def apply(quizId: QuizId): Future[Either[Result, Quiz]] = byId(quizId).map { quizOp => quizOp match {
    case None => Left(NotFound(views.html.errors.notFoundPage("There was no quiz for id=["+quizId+"]")))
    case Some(quiz) => Right(quiz)
  } }

  class QuizTable(tag: Tag) extends Table[Quiz](tag, "quiz") {
    def id = column[QuizId]("id", O.PrimaryKey, O.AutoInc)
    def ownerId = column[UserId]("owner_id")
    def name = column[String]("name")
    def creationDate = column[DateTime]("creation_date")
    def updateDate = column[DateTime]("update_date")

    def ownerIdFK = foreignKey("quiz_fk__owner_id", ownerId, userDAO.Users)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

    def * = (id, ownerId, name, creationDate, updateDate) <> (Quiz.tupled, Quiz.unapply)
  }

  class User2QuizTable(tag: Tag) extends Table[User2Quiz](tag, "app_user_2_quiz") {
    def userId = column[UserId]("user_id")
    def quizId = column[QuizId]("quiz_id")
    def access = column[Access]("access")

    def pk = primaryKey("course_2_quiz_pk", (userId, quizId))

    def userIdFK = foreignKey("app_user_2_quiz_fk__user_id", userId, userDAO.Users)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def quizIdFK = foreignKey("app_user_2_quiz_fk__quiz_id", quizId, Quizzes)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

    def * = (userId, quizId, access) <> (User2Quiz.tupled, User2Quiz.unapply)
  }

  class QuestionTable(tag: Tag) extends Table[Question](tag, "question") {
    def id = column[QuestionId]("id", O.PrimaryKey, O.AutoInc)
    def ownerId = column[UserId]("owner_id")
    def title = column[String]("title")
    def descriptionRaw = column[String]("description_raw")
    def descriptionHtml = column[Html]("description_html")
    def creationDate = column[DateTime]("creation_date")

    def ownerIdFK = foreignKey("question_fk__owner_id", ownerId, userDAO.Users)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

    def * = (id, ownerId, title, descriptionRaw, descriptionHtml, creationDate) <> (Question.tupled, Question.unapply)
  }

  class QuestionSectionTable(tag: Tag) extends Table[QuestionSection](tag, "question_section") {
    def id = column[SectionId]("id", O.PrimaryKey, O.AutoInc)
    def questionId = column[QuestionId]("question_id")
    def explanationRaw = column[String]("explanation_raw")
    def explanationHtml = column[Html]("explanation_html")
    def order = column[Short]("section_order")

    def questionIdFK = foreignKey("question_section_fk__question_id", questionId, Questions)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

    def * = (id, questionId, explanationRaw, explanationHtml, order) <> (QuestionSection.tupled, QuestionSection.unapply)
  }

  class QuestionPartChoiceTable(tag: Tag) extends Table[QuestionPartChoice](tag, "question_part_choice") {
    def id = column[PartId]("id", O.PrimaryKey, O.AutoInc)
    def sectionId = column[SectionId]("section_id")
    def questionId = column[QuestionId]("question_id")
    def explanationRaw = column[String]("explanation_raw")
    def explanationHtml = column[Html]("explanation_html")
    def correctChoice = column[Short]("correct_choice")
    def order = column[Short]("part_order")

    def sectionIdFK = foreignKey("question_part_choice_fk__section_id", sectionId, QuestionSections)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def questionIdFK = foreignKey("question_part_choice_fk__question_id", questionId, Questions)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

    def * = (id, sectionId, questionId, explanationRaw, explanationHtml, correctChoice, order) <> (QuestionPartChoice.tupled, QuestionPartChoice.unapply)
  }

  class QuestionPartFunctionTable(tag: Tag) extends Table[QuestionPartFunction](tag, "question_part_function") {
    def id = column[PartId]("id", O.PrimaryKey, O.AutoInc)
    def sectionId = column[SectionId]("section_id")
    def questionId = column[QuestionId]("question_id")
    def explanationRaw = column[String]("explanation_raw")
    def explanationHtml = column[Html]("explanation_html")
    def functionRaw = column[String]("function_raw")
    def functionMath = column[MathMLElem]("function_math")
    def order = column[Short]("part_order")

    def sectionIdFK = foreignKey("question_part_function_fk__section_id", sectionId, QuestionSections)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def questionIdFK = foreignKey("question_part_function_fk__question_id", questionId, Questions)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

    def * = (id, sectionId, questionId, explanationRaw, explanationHtml, functionRaw, functionMath, order) <> (QuestionPartFunction.tupled, QuestionPartFunction.unapply)
  }

}

