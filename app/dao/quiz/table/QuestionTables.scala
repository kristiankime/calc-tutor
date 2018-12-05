package dao.quiz.table

import javax.inject.{Inject, Singleton}
import com.artclod.mathml.scalar.MathMLElem
import dao.ColumnTypeMappings
import dao.user.UserDAO
import dao.user.table.UserTables
import models._
import models.quiz._
import models.quiz.util.{SequenceTokenOrMath, SetOfNumbers}
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
class QuestionTables @Inject()(protected val dbConfigProvider: DatabaseConfigProvider, protected val userTables: UserTables)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] with ColumnTypeMappings {
  // ====
  //  import profile.api._ // Use this after upgrading slick
  import dbConfig.driver.api._

  // * ====== TABLE INSTANCES ====== *
  val Questions = lifted.TableQuery[QuestionTable]
  val QuestionUserConstantIntegers = lifted.TableQuery[QuestionUserConstantIntegerTable]
  val QuestionUserConstantDecimals = lifted.TableQuery[QuestionUserConstantDecimalTable]
  val QuestionUserConstantSets = lifted.TableQuery[QuestionUserConstantSetTable]
  val QuestionSections = lifted.TableQuery[QuestionSectionTable]
  val QuestionPartChoices = lifted.TableQuery[QuestionPartChoiceTable]
  val QuestionPartFunctions = lifted.TableQuery[QuestionPartFunctionTable]
  val QuestionPartSequences = lifted.TableQuery[QuestionPartSequenceTable]

  // * ====== TABLE CLASSES ====== *
  class QuestionTable(tag: Tag) extends Table[Question](tag, "question") {
    def id = column[QuestionId]("id", O.PrimaryKey, O.AutoInc)
    def ownerId = column[UserId]("owner_id")
    def title = column[String]("title")
    def descriptionRaw = column[String]("description_raw")
    def descriptionHtml = column[Html]("description_html")
    def archivedNum = column[Short]("archived")
    def creationDate = column[DateTime]("creation_date")

    def ownerIdFK = foreignKey("question_fk__owner_id", ownerId, userTables.Users)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

    def * = (id, ownerId, title, descriptionRaw, descriptionHtml, archivedNum, creationDate) <> (Question.tupled, Question.unapply)
  }

  class QuestionUserConstantIntegerTable(tag: Tag) extends Table[QuestionUserConstantInteger](tag, "question_uc_integer") {
    def id = column[QuestionUserConstantId]("id", O.PrimaryKey, O.AutoInc)
    def questionId = column[QuestionId]("question_id")
    def name = column[String]("name")
    def lower = column[Int]("lower")
    def upper = column[Int]("upper")

    def questionIdFK = foreignKey("question_uc_integer_fk__question_id", questionId, Questions)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

    def * = (id, questionId, name, lower, upper) <> (QuestionUserConstantInteger.tupled, QuestionUserConstantInteger.unapply)
  }

  class QuestionUserConstantDecimalTable(tag: Tag) extends Table[QuestionUserConstantDecimal](tag, "question_uc_decimal") {
    def id = column[QuestionUserConstantId]("id", O.PrimaryKey, O.AutoInc)
    def questionId = column[QuestionId]("question_id")
    def name = column[String]("name")
    def lower = column[Double]("lower")
    def upper = column[Double]("upper")
    def precision = column[Int]("precision")

    def questionIdFK = foreignKey("question_uc_decimal_fk__question_id", questionId, Questions)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

    def * = (id, questionId, name, lower, upper, precision) <> (QuestionUserConstantDecimal.tupled, QuestionUserConstantDecimal.unapply)
  }

  class QuestionUserConstantSetTable(tag: Tag) extends Table[QuestionUserConstantSet](tag, "question_uc_set") {
    def id = column[QuestionUserConstantId]("id", O.PrimaryKey, O.AutoInc)
    def questionId = column[QuestionId]("question_id")
    def name = column[String]("name")
    def valuesRaw = column[String]("values_raw")
    def valuesMath = column[SetOfNumbers]("values_math")

    def questionIdFK = foreignKey("question_uc_set_fk__question_id", questionId, Questions)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

    def * = (id, questionId, name, valuesRaw, valuesMath) <> (QuestionUserConstantSet.tupled, QuestionUserConstantSet.unapply)
  }

  class QuestionSectionTable(tag: Tag) extends Table[QuestionSection](tag, "question_section") {
    def id = column[QuestionSectionId]("id", O.PrimaryKey, O.AutoInc)
    def questionId = column[QuestionId]("question_id")
    def explanationRaw = column[String]("explanation_raw")
    def explanationHtml = column[Html]("explanation_html")
    def order = column[Short]("section_order")

    def questionIdFK = foreignKey("question_section_fk__question_id", questionId, Questions)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

    def * = (id, questionId, explanationRaw, explanationHtml, order) <> (QuestionSection.tupled, QuestionSection.unapply)
  }

  class QuestionPartChoiceTable(tag: Tag) extends Table[QuestionPartChoice](tag, "question_part_choice") {
    def id = column[QuestionPartId]("id", O.PrimaryKey, O.AutoInc)
    def sectionId = column[QuestionSectionId]("section_id")
    def questionId = column[QuestionId]("question_id")
    def summaryRaw = column[String]("summary_raw")
    def summaryHtml = column[Html]("summary_html")
    def correctChoice = column[Short]("correct_choice")
    def order = column[Short]("part_order")

    def sectionIdFK = foreignKey("question_part_choice_fk__section_id", sectionId, QuestionSections)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def questionIdFK = foreignKey("question_part_choice_fk__question_id", questionId, Questions)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

    def * = (id, sectionId, questionId, summaryRaw, summaryHtml, correctChoice, order) <> (QuestionPartChoice.tupled, QuestionPartChoice.unapply)
  }

  class QuestionPartFunctionTable(tag: Tag) extends Table[QuestionPartFunction](tag, "question_part_function") {
    def id = column[QuestionPartId]("id", O.PrimaryKey, O.AutoInc)
    def sectionId = column[QuestionSectionId]("section_id")
    def questionId = column[QuestionId]("question_id")
    def summaryRaw = column[String]("summary_raw")
    def summaryHtml = column[Html]("summary_html")
    def functionRaw = column[String]("function_raw")
    def functionMath = column[MathMLElem]("function_math")
    def order = column[Short]("part_order")

    def sectionIdFK = foreignKey("question_part_function_fk__section_id", sectionId, QuestionSections)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def questionIdFK = foreignKey("question_part_function_fk__question_id", questionId, Questions)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

    def * = (id, sectionId, questionId, summaryRaw, summaryHtml, functionRaw, functionMath, order) <> (QuestionPartFunction.tupled, QuestionPartFunction.unapply)
  }

  class QuestionPartSequenceTable(tag: Tag) extends Table[QuestionPartSequence](tag, "question_part_sequence") {
    def id = column[QuestionPartId]("id", O.PrimaryKey, O.AutoInc)
    def sectionId = column[QuestionSectionId]("section_id")
    def questionId = column[QuestionId]("question_id")
    def summaryRaw = column[String]("summary_raw")
    def summaryHtml = column[Html]("summary_html")
    def sequenceStr = column[String]("sequence_str")
    def sequenceMath = column[SequenceTokenOrMath]("sequence_math")
    def order = column[Short]("part_order")

    def sectionIdFK = foreignKey("question_part_sequence_fk__section_id", sectionId, QuestionSections)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def questionIdFK = foreignKey("question_part_sequence_fk__question_id", questionId, Questions)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

    def * = (id, sectionId, questionId, summaryRaw, summaryHtml, sequenceStr, sequenceMath, order) <> (QuestionPartSequence.tupled, QuestionPartSequence.unapply)
  }
}

