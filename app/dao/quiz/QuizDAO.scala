package dao.quiz

import javax.inject.{Inject, Singleton}

import com.artclod.mathml.scalar.MathMLElem
import com.artclod.slick.JodaUTC
import dao.ColumnTypeMappings
import dao.organization.CourseDAO
import dao.user.UserDAO
import models.quiz.{User2Quiz, _}
import models._
import models.organization.{Course, Course2Quiz, User2Course}
import models.user.User
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
class QuizDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider, protected val userDAO: UserDAO, protected val courseDAO: CourseDAO, protected val questionDAO: QuestionDAO)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] with ColumnTypeMappings {
  // ====
  //  import profile.api._ // Use this after upgrading slick
  import dbConfig.driver.api._

  // * ====== TABLE INSTANCES ====== *
  val Quizzes = lifted.TableQuery[QuizTable]
  val User2Quizzes = lifted.TableQuery[User2QuizTable]
  val Courses2Quizzes = lifted.TableQuery[Course2QuizTable]
  val Question2Quizzes = lifted.TableQuery[Question2QuizTable]

  // * ====== QUERIES ====== *

  // ====== FIND ======
  def all(): Future[Seq[Quiz]] = db.run(Quizzes.result)

  def byId(id : QuizId): Future[Option[Quiz]] = db.run(Quizzes.filter(_.id === id).result.headOption)

  def apply(quizId: QuizId): Future[Either[Result, Quiz]] = byId(quizId).map { quizOp => quizOp match {
    case None => Left(NotFound(views.html.errors.notFoundPage("There was no quiz for id=["+quizId+"]")))
    case Some(quiz) => Right(quiz)
  } }

  def byIds(courseId: CourseId, quizId: QuizId): Future[Option[Quiz]] = db.run{
    (for(z <- Quizzes; c2z <- Courses2Quizzes if c2z.courseId === courseId && c2z.quizId === quizId ) yield z).result.headOption
  }

  def apply(courseId: CourseId, quizId: QuizId): Future[Either[Result, Quiz]] = byIds(courseId, quizId).map { _ match {
    case None => Left(NotFound(views.html.errors.notFoundPage("There was no Quiz for id=["+quizId+"] which also had Course Id [" + courseId + "]")))
    case Some(course) => Right(course)
  } }

  def quizzesFor(courseId: CourseId) : Future[Seq[Quiz]] = db.run {
    (for(c2z <- Courses2Quizzes; z <- Quizzes if c2z.courseId === courseId && c2z.quizId === z.id) yield z).result
  }
  // ====== Access ======
  def access(userId: UserId, quizId : QuizId): Future[Access] = db.run {
    val ownerAccess = (for(z <- Quizzes if z.ownerId === userId && z.id === quizId) yield z).result.headOption.map(_ match { case Some(_) => Own case None => Non})
    val directAccess = (for(u2z <- User2Quizzes if u2z.userId === userId && u2z.quizId === quizId) yield u2z.access).result.headOption.map(_.getOrElse(Non))
    ownerAccess.flatMap(oa => directAccess.map( da => oa max da))
  }

  def attach(course: Course, quiz: Quiz) = db.run(Courses2Quizzes += Course2Quiz(course.id, quiz.id, None, None)).map { _ => () }

  def grantAccess(user: User, quiz: Quiz, access: Access) = db.run(User2Quizzes += User2Quiz(user.id, quiz.id, access)).map { _ => () }

  // ====== Create ======
  def insert(quiz: Quiz): Future[Quiz] = db.run(
    (Quizzes returning Quizzes.map(_.id) into ((needsId, id) => needsId.copy(id = id))) += quiz
  )

  def updateName(quiz: Quiz, name: String): Future[Int] = db.run {
    (for { z <- Quizzes if z.id === quiz.id } yield z.name ).update(name)
  }

  def questionSummariesFor(quiz: Quiz): Future[Seq[Question]] = db.run {
    (for(q2z <- Question2Quizzes; q <- questionDAO.Questions if q2z.quizId === quiz.id && q2z.questionId === q.id) yield q).result
  }

  // * ====== TABLE CLASSES ====== *
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

  class Course2QuizTable(tag: Tag) extends Table[Course2Quiz](tag, "course_2_quiz") {
    def courseId = column[CourseId]("course_id")
    def quizId = column[QuizId]("quiz_id")
    def startDate = column[Option[DateTime]]("start_date")
    def endDate = column[Option[DateTime]]("end_date")

    def pk = primaryKey("course_2_quiz_pk", (courseId, quizId))

    def quizIdFK = foreignKey("course_2_quiz_fk__user_id", quizId, Quizzes)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def courseIdFK = foreignKey("course_2_quiz_fk__course_id", courseId, courseDAO.Courses)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

    def * = (courseId, quizId, startDate, endDate) <> (Course2Quiz.tupled, Course2Quiz.unapply)
  }

  class Question2QuizTable(tag: Tag) extends Table[Question2Quiz](tag, "question_2_quiz") {
    def questionId = column[QuestionId]("question_id")
    def quizId = column[QuizId]("quiz_id")
    def ownerId = column[UserId]("owner_id")
    def creationDate = column[DateTime]("creation_date")
    def order = column[Int]("section_order")

    def pk = primaryKey("course_2_quiz_pk", (questionId, quizId))

    def questionIdFK = foreignKey("question_2_quiz_fk__question_id", questionId, questionDAO.Questions)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def quizIdFK = foreignKey("question_2_quiz_fk__quiz_id", quizId, Quizzes)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

    def * = (questionId, quizId, ownerId, creationDate, order) <> (Question2Quiz.tupled, Question2Quiz.unapply)
  }

}

