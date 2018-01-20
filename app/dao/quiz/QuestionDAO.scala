package dao.quiz

import javax.inject.{Inject, Singleton}

import com.artclod.mathml.scalar.MathMLElem
import controllers.quiz.{QuestionPartChoiceJson, QuestionPartFunctionJson}
import dao.ColumnTypeMappings
import dao.quiz.table.{QuestionTables, SkillTables}
import dao.user.UserDAO
import dao.user.table.UserTables
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
class QuestionDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider, protected val userTables: UserTables, protected val questionTables: QuestionTables, protected val skillDAO: SkillDAO, protected val skillTables: SkillTables)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] with ColumnTypeMappings {
  // ====
  //  import profile.api._ // Use this after upgrading slick
  import dbConfig.driver.api._

  // * ====== TABLE INSTANCES ====== *
  val Questions = questionTables.Questions
  val QuestionSections = questionTables.QuestionSections
  val QuestionPartChoices = questionTables.QuestionPartChoices
  val QuestionPartFunctions = questionTables.QuestionPartFunctions

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
    val skillsFuture = skillDAO.skillsFor(id)

    questionFuture.flatMap(questionOp => { sectionsFuture.flatMap(sections => { choicePartsFuture.flatMap( choiceParts => { functionPartsFuture.flatMap( functionParts => { skillsFuture.map( skills => {
      val secId2Fp = functionParts.groupBy(p => p.sectionId)
      val secId2Cp = choiceParts.groupBy(p => p.sectionId)

      val sectionFrames = sections.map(section => QuestionSectionFrame(section, secId2Cp.getOrElse(section.id, Seq()), secId2Fp.getOrElse(section.id, Seq())))

      val questionFrameOp : Option[QuestionFrame] = questionOp.map(question => {
        sectionFrames.nonEmpty match {
          case false => throw new IllegalArgumentException("There were no sections for id = " + id)
          case true => QuestionFrame(question, Vector(sectionFrames:_*).sorted, Vector(skills:_*))
        }
      })

      questionFrameOp

    }) }) }) }) })
  }

  //--- Skills for question
  def skillsFor(id : QuestionId) = {
    null
  }

  def skillsForAll(): Future[Seq[(Question, Skill)]] = db.run({
    (for (q <- Questions; q2s <- skillTables.Skills2Questions; s <- skillTables.Skills if q.id === q2s.questionId && q2s.skillId === s.id) yield (q, s)).result
  })

  def skillsForAllSet(): Future[Seq[(Question, Set[Skill])]] =
    skillsForAll().map(s => s.groupBy(_._1).mapValues(_.map(_._2)).mapValues(_.toSet).toSeq )

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
        val skillsFuture = skillDAO.addSkills(question, questionFrame.skills)
        val sectionsFutures : Seq[Future[QuestionSectionFrame]] = questionFrame.id(question.id).sections.map(section => insert(section))
//        val futureOfSections : Future[Vector[SectionFrame]] = sectionsFutures.foldLeft(Future.successful(Vector[SectionFrame]()))((cur, add) => cur.flatMap(c => add.map(a => c :+ a)) ).map(_.sorted)
        val futureOfSections : Future[Vector[QuestionSectionFrame]] = com.artclod.concurrent.raiseFuture(sectionsFutures).map(_.sorted)
//        futureOfSections.map(sections => QuestionFrame(question, sections, questionFrame.skills))
        skillsFuture.flatMap(skillCount => futureOfSections.map(sections =>
        QuestionFrame(question, sections, questionFrame.skills)
      ))
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

}

