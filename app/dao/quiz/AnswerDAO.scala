package dao.quiz

import javax.inject.{Inject, Singleton}

import com.artclod.mathml.scalar.MathMLElem
import dao.ColumnTypeMappings
import dao.user.UserDAO
import models._
import models.quiz.{AnswerPart, _}
import models.support.HasOrder
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
class AnswerDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider, protected val userDAO: UserDAO, protected val questionDAO: QuestionDAO)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] with ColumnTypeMappings {
  // ====
  //  import profile.api._ // Use this after upgrading slick
  import dbConfig.driver.api._

  // * ====== TABLE INSTANCES ====== *
  val Answers = lifted.TableQuery[AnswerTable]
  val AnswerSections = lifted.TableQuery[AnswerSectionTable]
  val AnswerParts = lifted.TableQuery[AnswerPartTable]

  // * ====== QUERIES ====== *

  // ====== FIND ======
  def byId(id : AnswerId): Future[Option[Answer]] = db.run(Answers.filter(_.id === id).result.headOption)

  def sectionsById(id : AnswerId): Future[Seq[AnswerSection]] = db.run(AnswerSections.filter(_.answerId === id).result)

  def partsId(id : AnswerId): Future[Seq[AnswerPart]] = db.run(AnswerParts.filter(_.answerId === id).result)

  def frameById(id : AnswerId): Future[Option[AnswerFrame]] = {
    val answerFuture = byId(id)
    val sectionsFuture = sectionsById(id)
    val partsFuture = partsId(id)

    answerFuture.flatMap(answerOp => { sectionsFuture.flatMap(sections => { partsFuture.map( parts => {
      val secId2parts: Map[AnswerSectionId, Seq[AnswerPart]] = parts.groupBy(p => p.answerSectionId)

      val sectionFrames = sections.map(section => AnswerSectionFrame(section, secId2parts.getOrElse(section.id, Seq()).toVector.sorted))

      answerOp.map(answer => {
        sectionFrames.nonEmpty match {
          case false => throw new IllegalArgumentException("There were no sections for id = " + id)
          case true => AnswerFrame(answer, Vector(sectionFrames:_*).sorted)
        }
      })

    }) }) })
  }
  // ---
  def apply(answerId: AnswerId): Future[Either[Result, Answer]] = byId(answerId).map { _ match {
    case None => Left(NotFound(views.html.errors.notFoundPage("There was no answer for id=["+answerId+"]")))
    case Some(answer) => Right(answer)
  } }

  def apply(answerIdOp: Option[AnswerId]): Future[Either[Result, Option[Answer]]] =
    answerIdOp match {
      case Some(answerId) => byId(answerId).map { _ match {
          case None => Left(NotFound(views.html.errors.notFoundPage("There was no question for id=["+answerId+"]")))
          case Some(answer) => Right(Some(answer))
        } }
      case None => Future.successful(Right(None))
    }

  def frameByIdEither(answerId : AnswerId): Future[Either[Result, AnswerFrame]] = frameById(answerId).map { _ match {
    case None => Left(NotFound(views.html.errors.notFoundPage("There was no answer for id=["+answerId+"]")))
    case Some(answerFrame) => Right(answerFrame)
  } }

  // ====== Create ======
  def insert(answerFrame: AnswerFrame) : Future[AnswerFrame] = {
    insert(answerFrame.answer).flatMap(answer => {
      val sectionsFutures : Seq[Future[AnswerSectionFrame]] = answerFrame.id(answer.id).sections.map(section => insert(section))
      val futureOfSections : Future[Vector[AnswerSectionFrame]] = com.artclod.concurrent.raiseFuture(sectionsFutures).map(_.sorted)
      futureOfSections.map(sections => AnswerFrame(answer, sections))
    })
  }

  def insert(sectionFrame: AnswerSectionFrame) : Future[AnswerSectionFrame] = {
    insert(sectionFrame.answerSection).flatMap(section => {
      insert(sectionFrame.id(section.id).parts).map(parts =>
        AnswerSectionFrame(answerSection = section, parts = parts.toVector.sorted) )
    })
  }

  def insert(answer: Answer): Future[Answer] = db.run(
    (Answers returning Answers.map(_.id) into ((needsId, id) => needsId.copy(id = id))) += answer
  )

  def insert(answerSection: AnswerSection): Future[AnswerSection] = db.run(
    (AnswerSections returning AnswerSections.map(_.id) into ((needsId, id) => needsId.copy(id = id))) += answerSection
  )

  def insert(answerParts: Seq[AnswerPart]): Future[Seq[AnswerPart]] = db.run {
    (AnswerParts returning AnswerParts.map(_.id) into ((needsId, id) => needsId.copy(id = id))) ++= answerParts
  }

  // === Support ===

  // * ====== TABLE CLASSES ====== *
  class AnswerTable(tag: Tag) extends Table[Answer](tag, "answer") {
    def id = column[AnswerId]("id", O.PrimaryKey, O.AutoInc)
    def ownerId = column[UserId]("owner_id")
    def questionId = column[QuestionId]("question_id")
    def allCorrectNum = column[Short]("all_correct")
    def creationDate = column[DateTime]("creation_date")

    def ownerIdFK = foreignKey("answer_fk__owner_id", ownerId, userDAO.Users)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def questionIdFK = foreignKey("answer_section_fk__question_id", questionId, questionDAO.Questions)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

    def * = (id, ownerId, questionId, allCorrectNum, creationDate) <> (Answer.tupled, Answer.unapply)
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
    def questionSectionIdFK = foreignKey("answer_section_fk__question_section_id", questionSectionId, questionDAO.QuestionSections)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def questionIdFK = foreignKey("answer_section_fk__question_id", questionId, questionDAO.Questions)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

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

    def answerSectionIdFK = foreignKey("answer_part_fk__answer_section_id", questionSectionId, questionDAO.QuestionSections)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def answerIdFK = foreignKey("answer_part_fk__answer_id", answerId, Answers)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def questionPartIdFK = foreignKey("answer_part_fk__question_part_id", questionPartId, questionDAO.QuestionPartFunctions)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def questionSectionIdFK = foreignKey("answer_part_fk__question_section_id", questionSectionId, questionDAO.QuestionSections)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def questionIdFK = foreignKey("answer_part_fk__question_id", questionId, questionDAO.Questions)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

    def * = (id, answerSectionId, answerId, questionPartId, questionSectionId, questionId, functionRaw, functionMath, correctNum, order) <> (AnswerPart.tupled, AnswerPart.unapply)
  }

}

