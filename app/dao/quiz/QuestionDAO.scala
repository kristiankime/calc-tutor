package dao.quiz

import javax.inject.{Inject, Singleton}

import com.artclod.mathml.scalar.MathMLElem
import controllers.quiz.{QuestionPartChoiceJson, QuestionPartFunctionJson}
import dao.ColumnTypeMappings
import dao.user.UserDAO
import models._
import models.quiz.{User2Quiz, _}
import models.user.User
import org.joda.time.DateTime
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.mvc.Result
import play.api.mvc.Results._
import play.twirl.api.Html
import slick.lifted

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

// ====
import slick.driver.JdbcProfile
//import slick.jdbc.JdbcProfile // Use this after upgrading slick
// ====

@Singleton
class QuestionDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider, protected val userDAO: UserDAO)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] with ColumnTypeMappings {
  // ====
  //  import profile.api._ // Use this after upgrading slick
  import dbConfig.driver.api._

  // * ====== TABLE INSTANCES ====== *
  val Questions = lifted.TableQuery[QuestionTable]
  val QuestionSections = lifted.TableQuery[QuestionSectionTable]
  val QuestionPartChoices = lifted.TableQuery[QuestionPartChoiceTable]
  val QuestionPartFunctions = lifted.TableQuery[QuestionPartFunctionTable]

  // * ====== QUERIES ====== *

  // ====== FIND ======
  def byId(id : QuestionId): Future[Option[Question]] = db.run(Questions.filter(_.id === id).result.headOption)

  def sectionsById(id : QuestionId): Future[Seq[QuestionSection]] = db.run(QuestionSections.filter(_.questionId === id).result)

  def choicePartsId(id : QuestionId): Future[Seq[QuestionPartChoice]] = db.run(QuestionPartChoices.filter(_.questionId === id).result)

  def functionPartsId(id : QuestionId): Future[Seq[QuestionPartFunction]] = db.run(QuestionPartFunctions.filter(_.questionId === id).result)

  def frameById(id : QuestionId): Future[Option[QuestionFrame]] = {
    val questionFuture = byId(id)
    val sectionsFuture = sectionsById(id)
    val choicePartsFuture = choicePartsId(id)
    val functionPartsFuture = functionPartsId(id)

    questionFuture.flatMap(questionOp => { sectionsFuture.flatMap(sections => { choicePartsFuture.flatMap( choiceParts => { functionPartsFuture.map( functionParts => {
      val secId2Fp = functionParts.groupBy(p => p.sectionId)
      val secId2Cp = choiceParts.groupBy(p => p.sectionId)

      val sectionFrames = sections.map(section => QuestionSectionFrame(section, secId2Cp.getOrElse(section.id, Seq()), secId2Fp.getOrElse(section.id, Seq())))

      questionOp.map(question => {
        sectionFrames.nonEmpty match {
          case false => throw new IllegalArgumentException("There were no sections for id = " + id)
          case true => QuestionFrame(question, Vector(sectionFrames:_*).sorted)
        }
      })

    }) }) }) })
 }

  // ---
  def apply(questionId: QuestionId): Future[Either[Result, Question]] = byId(questionId).map { _ match {
    case None => Left(NotFound(views.html.errors.notFoundPage("There was no question for id=["+questionId+"]")))
    case Some(question) => Right(question)
  } }

  def frameByIdEither(questionId : QuestionId): Future[Either[Result, QuestionFrame]] = frameById(questionId).map { _ match {
    case None => Left(NotFound(views.html.errors.notFoundPage("There was no question for id=["+questionId+"]")))
    case Some(questionFrame) => Right(questionFrame)
  } }

  // ====== Create ======
  def insert(questionFrame: QuestionFrame) : Future[QuestionFrame] = {
    insert(questionFrame.question).flatMap{ question => {
        val sectionsFutures : Seq[Future[QuestionSectionFrame]] = questionFrame.id(question.id).sections.map(section => insert(section))
//        val futureOfSections : Future[Vector[SectionFrame]] = sectionsFutures.foldLeft(Future.successful(Vector[SectionFrame]()))((cur, add) => cur.flatMap(c => add.map(a => c :+ a)) ).map(_.sorted)
        val futureOfSections : Future[Vector[QuestionSectionFrame]] = com.artclod.concurrent.raiseFuture(sectionsFutures).map(_.sorted)
        futureOfSections.map(sections => QuestionFrame(question, sections))
      }
    }
  }

  def insert(sectionFrame: QuestionSectionFrame) : Future[QuestionSectionFrame] = {
    insert(sectionFrame.section).flatMap(section => {
      (sectionFrame.id(section.id).parts match {
        case Left(ps) => insertChoices(ps).map(p => Left(Vector(p:_*).sorted))
        case Right(ps) => insertFunctions(ps).map(p => Right(Vector(p:_*).sorted))
      }).map(parts => QuestionSectionFrame(section = section, parts = parts) )
    })
  }

  def insert(question: Question): Future[Question] = db.run(
    (Questions returning Questions.map(_.id) into ((needsId, id) => needsId.copy(id = id))) += question
  )

  def insert(questionSection: QuestionSection): Future[QuestionSection] = db.run(
    (QuestionSections returning QuestionSections.map(_.id) into ((needsId, id) => needsId.copy(id = id))) += questionSection
  )

  def insertChoices(questionPartChoices: Seq[QuestionPartChoice]): Future[Seq[QuestionPartChoice]] = db.run {
    (QuestionPartChoices returning QuestionPartChoices.map(_.id) into ((needsId, id) => needsId.copy(id = id))) ++= questionPartChoices
  }

  def insertFunctions(questionPartFunctions: Seq[QuestionPartFunction]): Future[Seq[QuestionPartFunction]] = db.run {
    (QuestionPartFunctions returning QuestionPartFunctions.map(_.id) into ((needsId, id) => needsId.copy(id = id))) ++= questionPartFunctions
  }

  // === Support ===


  // * ====== TABLE CLASSES ====== *
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
    def explanationRaw = column[String]("explanation_raw")
    def explanationHtml = column[Html]("explanation_html")
    def correctChoice = column[Short]("correct_choice")
    def order = column[Short]("part_order")

    def sectionIdFK = foreignKey("question_part_choice_fk__section_id", sectionId, QuestionSections)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def questionIdFK = foreignKey("question_part_choice_fk__question_id", questionId, Questions)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

    def * = (id, sectionId, questionId, explanationRaw, explanationHtml, correctChoice, order) <> (QuestionPartChoice.tupled, QuestionPartChoice.unapply)
  }

  class QuestionPartFunctionTable(tag: Tag) extends Table[QuestionPartFunction](tag, "question_part_function") {
    def id = column[QuestionPartId]("id", O.PrimaryKey, O.AutoInc)
    def sectionId = column[QuestionSectionId]("section_id")
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

