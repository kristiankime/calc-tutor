package dao.quiz.table

import javax.inject.{Inject, Singleton}

import dao.ColumnTypeMappings
import dao.quiz.{AnswerDAO, QuestionDAO}
import dao.user.UserDAO
import dao.user.table.UserTables
import models._
import models.quiz._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.lifted

import scala.concurrent.{ExecutionContext, Future}

// ====
import slick.driver.JdbcProfile
//import slick.jdbc.JdbcProfile // Use this after upgrading slick
// ====

@Singleton
class SkillTables @Inject()(protected val dbConfigProvider: DatabaseConfigProvider, protected val userTables: UserTables, protected val questionTables: QuestionTables, protected val answerTables: AnswerTables)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] with ColumnTypeMappings {
  // ====
  //  import profile.api._ // Use this after upgrading slick
  import dbConfig.driver.api._

  // * ====== TABLE INSTANCES ====== *
  val Skills = lifted.TableQuery[SkillTable]
  val Skills2Questions = lifted.TableQuery[Skill2QuestionTable]
  val UserAnswerCounts = lifted.TableQuery[UserAnswerCountTable]

  // * ====== TABLE CLASSES ====== *
  class SkillTable(tag: Tag) extends Table[Skill](tag, "skill") {
    def id = column[SkillId]("id", O.AutoInc)
    def name = column[String]("name", O.PrimaryKey)
    def shortName = column[String]("short_name")
    def intercept = column[Double]("intercept")
    def correct = column[Double]("correct")
    def incorrect = column[Double]("incorrect")

    def idIdx = index("skill_idx__id", id, unique = true)

    def * = (id, name, shortName, intercept, correct, incorrect) <> (Skill.tupled, Skill.unapply)
  }

  class Skill2QuestionTable(tag: Tag) extends Table[Skill2Question](tag, "skill_2_question") {
    def skillId = column[SkillId]("skill_id")
    def questionId = column[QuestionId]("question_id")

    def pk = primaryKey("skill_2_question_pk", (skillId, questionId))

    def skillIdFK = foreignKey("skill_2_question_fk__skill_id", skillId, Skills)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def questionIdFK = foreignKey("skill_2_question_fk__question_id", questionId, questionTables.Questions)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

    def * = (skillId, questionId) <> (Skill2Question.tupled, Skill2Question.unapply)
  }

  class UserAnswerCountTable(tag: Tag) extends Table[UserAnswerCount](tag, "user_answer_count") {
    def userId = column[UserId]("user_id")
    def skillId = column[SkillId]("skill_id")
    def correct = column[Int]("correct")
    def incorrect = column[Int]("incorrect")

    def pk = primaryKey("skill_2_question_pk", (userId, skillId))

    def userIdFK = foreignKey("skill_2_question_fk__user_id", userId, userTables.Users)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def skillIdFK = foreignKey("skill_2_question_fk__skill_id", skillId, Skills)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

    def * = (userId, skillId, correct, incorrect) <> (UserAnswerCount.tupled, UserAnswerCount.unapply)
  }

}

