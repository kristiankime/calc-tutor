package dao.quiz.table

import javax.inject.{Inject, Singleton}

import com.artclod.mathml.scalar.MathMLElem
import dao.ColumnTypeMappings
import dao.user.UserDAO
import dao.user.table.UserTables
import models._
import models.quiz.{AnswerPart, _}
import org.joda.time.DateTime
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.mvc.Result
import play.api.mvc.Results._
import slick.lifted

import scala.concurrent.{ExecutionContext, Future}

// ====
import slick.driver.JdbcProfile
//import slick.jdbc.JdbcProfile // Use this after upgrading slick
// ====

@Singleton
class AnswerTables @Inject()(protected val dbConfigProvider: DatabaseConfigProvider, protected val userTables: UserTables, protected val questionTables: QuestionTables)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] with ColumnTypeMappings {
  // ====
  //  import profile.api._ // Use this after upgrading slick
  import dbConfig.driver.api._

  // * ====== TABLE INSTANCES ====== *
  val Answers = lifted.TableQuery[AnswerTable]
  val AnswerSections = lifted.TableQuery[AnswerSectionTable]
  val AnswerParts = lifted.TableQuery[AnswerPartTable]

  // * ====== TABLE CLASSES ====== *
  class AnswerTable(tag: Tag) extends Table[Answer](tag, "answer") {
    def id = column[AnswerId]("id", O.PrimaryKey, O.AutoInc)
    def ownerId = column[UserId]("owner_id")
    def questionId = column[QuestionId]("question_id")
    def correct = column[Short]("correct")
    def creationDate = column[DateTime]("creation_date")

    def ownerIdFK = foreignKey("answer_fk__owner_id", ownerId, userTables.Users)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def questionIdFK = foreignKey("answer_section_fk__question_id", questionId, questionTables.Questions)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

    def * = (id, ownerId, questionId, correct, creationDate) <> (Answer.tupled, Answer.unapply)
  }

  class AnswerSectionTable(tag: Tag) extends Table[AnswerSection](tag, "answer_section") {
    // answer related ids
    def id = column[AnswerSectionId]("id", O.PrimaryKey, O.AutoInc)
    def answerId = column[AnswerId]("answer_id")
    // question related ids
    def questionSectionId = column[QuestionSectionId]("question_section_id")
    def questionId = column[QuestionId]("question_id")
    // non ids
    def choice = column[Option[Short]]("choice")
    def correctNum = column[Short]("correct")
    def order = column[Short]("section_order")

    def answerIdFK = foreignKey("answer_section_fk__answer_id", answerId, Answers)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def questionSectionIdFK = foreignKey("answer_section_fk__question_section_id", questionSectionId, questionTables.QuestionSections)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def questionIdFK = foreignKey("answer_section_fk__question_id", questionId, questionTables.Questions)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

    def * = (id, answerId, questionSectionId, questionId, choice, correctNum, order) <> (AnswerSection.tupled, AnswerSection.unapply)
  }

  class AnswerPartTable(tag: Tag) extends Table[AnswerPart](tag, "answer_part") {
    // answer related ids
    def id = column[AnswerPartId]("id", O.PrimaryKey, O.AutoInc)
    def answerSectionId = column[AnswerSectionId]("answer_section_id")
    def answerId = column[AnswerId]("answer_id")
    // question related ids
    def questionPartId = column[QuestionPartId]("question_part_id")
    def questionSectionId = column[QuestionSectionId]("question_section_id")
    def questionId = column[QuestionId]("question_id")
    // non ids
    def functionRaw = column[String]("function_raw")
    def functionMath = column[MathMLElem]("function_math")
    def correctNum = column[Short]("correct")
    def order = column[Short]("part_order")

    def answerSectionIdFK = foreignKey("answer_part_fk__answer_section_id", questionSectionId, questionTables.QuestionSections)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def answerIdFK = foreignKey("answer_part_fk__answer_id", answerId, Answers)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def questionPartIdFK = foreignKey("answer_part_fk__question_part_id", questionPartId, questionTables.QuestionPartFunctions)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def questionSectionIdFK = foreignKey("answer_part_fk__question_section_id", questionSectionId, questionTables.QuestionSections)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def questionIdFK = foreignKey("answer_part_fk__question_id", questionId, questionTables.Questions)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

    def * = (id, answerSectionId, answerId, questionPartId, questionSectionId, questionId, functionRaw, functionMath, correctNum, order) <> (AnswerPart.tupled, AnswerPart.unapply)
  }

}

