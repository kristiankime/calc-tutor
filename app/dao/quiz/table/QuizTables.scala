package dao.quiz.table

import javax.inject.{Inject, Singleton}
import com.artclod.slick.JodaUTC
import dao.ColumnTypeMappings
import dao.organization.CourseDAO
import dao.organization.table.CourseTables
import dao.user.UserDAO
import dao.user.table.UserTables
import models._
import models.organization.{Course, Course2Quiz}
import models.quiz.{User2Quiz, _}
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
class QuizTables @Inject()(protected val dbConfigProvider: DatabaseConfigProvider, protected val userTables: UserTables, protected val courseTables: CourseTables, protected val questionTables: QuestionTables)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] with ColumnTypeMappings {

  // ====
  //  import profile.api._ // Use this after upgrading slick
  import dbConfig.driver.api._

  // * ====== TABLE INSTANCES ====== *
  val Quizzes = lifted.TableQuery[QuizTable]
  val User2Quizzes = lifted.TableQuery[User2QuizTable]
  val Courses2Quizzes = lifted.TableQuery[Course2QuizTable]
  val Question2Quizzes = lifted.TableQuery[Question2QuizTable]

  // * ====== TABLE CLASSES ====== *
  class QuizTable(tag: Tag) extends Table[Quiz](tag, "quiz") {
    def id = column[QuizId]("id", O.PrimaryKey, O.AutoInc)
    def ownerId = column[UserId]("owner_id")
    def name = column[String]("name")
    def descriptionRaw = column[String]("description_raw")
    def descriptionHtml = column[Html]("description_html")
    def creationDate = column[DateTime]("creation_date")
    def updateDate = column[DateTime]("update_date")

    def ownerIdFK = foreignKey("quiz_fk__owner_id", ownerId, userTables.Users)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

    def * = (id, ownerId, name, descriptionRaw, descriptionHtml, creationDate, updateDate) <> (Quiz.tupled, Quiz.unapply)
  }

  class User2QuizTable(tag: Tag) extends Table[User2Quiz](tag, "app_user_2_quiz") {
    def userId = column[UserId]("user_id")
    def quizId = column[QuizId]("quiz_id")
    def access = column[Access]("access")

    def pk = primaryKey("course_2_quiz_pk", (userId, quizId))

    def userIdFK = foreignKey("app_user_2_quiz_fk__user_id", userId, userTables.Users)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def quizIdFK = foreignKey("app_user_2_quiz_fk__quiz_id", quizId, Quizzes)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

    def * = (userId, quizId, access) <> (User2Quiz.tupled, User2Quiz.unapply)
  }

  class Course2QuizTable(tag: Tag) extends Table[Course2Quiz](tag, "course_2_quiz") {
    def courseId = column[CourseId]("course_id")
    def quizId = column[QuizId]("quiz_id")
    def viewHide = column[Boolean]("view_hide")
    def startDate = column[Option[DateTime]]("start_date")
    def endDate = column[Option[DateTime]]("end_date")

    def pk = primaryKey("course_2_quiz_pk", (courseId, quizId))

    def quizIdFK = foreignKey("course_2_quiz_fk__user_id", quizId, Quizzes)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def courseIdFK = foreignKey("course_2_quiz_fk__course_id", courseId, courseTables.Courses)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

    def * = (courseId, quizId, viewHide, startDate, endDate) <> (Course2Quiz.tupled, Course2Quiz.unapply)
  }

  class Question2QuizTable(tag: Tag) extends Table[Question2Quiz](tag, "question_2_quiz") {
    def questionId = column[QuestionId]("question_id")
    def quizId = column[QuizId]("quiz_id")
    def ownerId = column[UserId]("owner_id")
    def creationDate = column[DateTime]("creation_date")
    def order = column[Int]("question_order")

    def pk = primaryKey("course_2_quiz_pk", (questionId, quizId))

    def questionIdFK = foreignKey("question_2_quiz_fk__question_id", questionId, questionTables.Questions)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def quizIdFK = foreignKey("question_2_quiz_fk__quiz_id", quizId, Quizzes)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

    def * = (questionId, quizId, ownerId, creationDate, order) <> (Question2Quiz.tupled, Question2Quiz.unapply)
  }

}

