package dao.quiz

import javax.inject.{Inject, Singleton}
import com.artclod.mathml.scalar.MathMLElem
import com.artclod.util.OneOfThree
import com.artclod.util.ofthree.{First, Second, Third}
import controllers.quiz.{QuestionPartChoiceJson, QuestionPartFunctionJson}
import dao.ColumnTypeMappings
import dao.quiz.table.{QuestionTables, SkillTables}
import dao.user.UserDAO
import dao.user.table.UserTables
import models._
import models.organization.Course
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
class QuestionDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider, protected val userTables: UserTables, protected val questionTables: QuestionTables, protected val skillDAO: SkillDAO, protected val skillTables: SkillTables, protected val userConstantsDAO: UserConstantsDAO)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] with ColumnTypeMappings {
  // ====
  //  import profile.api._ // Use this after upgrading slick
  import dbConfig.driver.api._

  // * ====== TABLE INSTANCES ====== *
  val Questions = questionTables.Questions
  val QuestionSections = questionTables.QuestionSections
  val QuestionPartChoices = questionTables.QuestionPartChoices
  val QuestionPartFunctions = questionTables.QuestionPartFunctions
  val QuestionPartSequences = questionTables.QuestionPartSequences

  // * ====== QUERIES ====== *

  // ====== FIND ======
  def byId(id : QuestionId): Future[Option[Question]] = db.run(Questions.filter(_.id === id).result.headOption)

  def sectionsById(id : QuestionId): Future[Seq[QuestionSection]] = db.run(QuestionSections.filter(_.questionId === id).result)

  def choicePartsId(id : QuestionId): Future[Seq[QuestionPartChoice]] = db.run(QuestionPartChoices.filter(_.questionId === id).result)

  def functionPartsId(id : QuestionId): Future[Seq[QuestionPartFunction]] = db.run(QuestionPartFunctions.filter(_.questionId === id).result)

  def sequencePartsId(id : QuestionId): Future[Seq[QuestionPartSequence]] = db.run(QuestionPartSequences.filter(_.questionId === id).result)

  def frameById(id : QuestionId): Future[Option[QuestionFrame]] = {
    val questionFuture = byId(id)
    val userConstantsFuture = userConstantsDAO.allUserConstants(id)
    val sectionsFuture = sectionsById(id)
    val choicePartsFuture = choicePartsId(id)
    val functionPartsFuture = functionPartsId(id)
    val sequencePartsFuture = sequencePartsId(id)
    val skillsFuture = skillDAO.skillsFor(id)

    questionFuture.flatMap(questionOp => {
      userConstantsFuture.flatMap(userConstants => {
        sectionsFuture.flatMap(sections => {
          choicePartsFuture.flatMap( choiceParts => {
            functionPartsFuture.flatMap( functionParts => {
              sequencePartsFuture.flatMap( sequenceParts => {
                skillsFuture.map( skills => {
        val secId2Fp = functionParts.groupBy(p => p.sectionId)
        val secId2Cp = choiceParts.groupBy(p => p.sectionId)
        val secId2Sp = sequenceParts.groupBy(p => p.sectionId)

        val sectionFrames = sections.map(section => QuestionSectionFrame(section, secId2Cp.getOrElse(section.id, Seq()), secId2Fp.getOrElse(section.id, Seq()), secId2Sp.getOrElse(section.id, Seq())))

        val questionFrameOp : Option[QuestionFrame] = questionOp.map(question => {
          sectionFrames.nonEmpty match {
            case false => throw new IllegalArgumentException("There were no sections for id = " + id)
            case true => QuestionFrame(question, Vector(sectionFrames:_*).sorted, Vector(skills:_*), QuestionUserConstantsFrame(userConstants))
          }
        })

        questionFrameOp

    }) }) }) }) }) }) })
  }

  // === Access ===
  def access(userId: UserId, questionId: QuestionId): Future[Access] = db.run {
    val ownerAccess = (for(q <- Questions if q.ownerId === userId && q.id === questionId) yield q).result.headOption.map(_ match { case Some(_) => Own case None => Non})
    // Currently we only have owner access to a question
//    val directAccess = (for(u2z <- User2Quizzes if u2z.userId === userId && u2z.quizId === quizId) yield u2z.access).result.headOption.map(_.getOrElse(Non))
//    val courseAccess = (for(u2c <- courseTables.User2Courses; c2z <- Courses2Quizzes if u2c.userId === userId && u2c.courseId === c2z.courseId && c2z.quizId === quizId) yield u2c.access).result.headOption.map(_.getOrElse(Non));

//    ownerAccess.flatMap(owner => directAccess.flatMap(direct => courseAccess.map(course => owner max direct max course)))
    ownerAccess
  }

  //--- Skills for question
//  def skillsFor(id : QuestionId) = {
//    null
//  }

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

  // ====== Question Search =====
  private def questionSearch(titleQuery: String, userId: UserId) = db.run({
    (for (q <- Questions; q2s <- skillTables.Skills2Questions; s <- skillTables.Skills
          if ((q.title like titleQuery) && q.id === q2s.questionId && q2s.skillId === s.id) && q.archivedNum === 0.toShort // Don't show the question if it's been archived
    ) yield (q, s))
      .union(
    (for (q <- Questions; q2s <- skillTables.Skills2Questions; s <- skillTables.Skills
          if ((q.title like titleQuery) && q.id === q2s.questionId && q2s.skillId === s.id) && (q.archivedNum =!= 0.toShort && q.ownerId === userId) // Show archived questions if the user is the owner
    ) yield (q, s))
    ).result
  })

  def questionSearchSet(userId: UserId, nameQuery: String, requiredSkills: Seq[String], bannedSkills: Seq[String]): Future[Seq[(Question, Set[Skill])]] = {
    val req = requiredSkills.toSet
    val ban = bannedSkills.toSet

    val ret = questionSearch(nameQuery, userId).map(s => s.groupBy(_._1).mapValues(_.map(_._2)).mapValues(_.toSet).toSeq)

    ret.map(questionList => {
      questionList.filter(question => {
        val skillsForQuestion = question._2.map(_.name).toSet
        val reqPass = req.subsetOf(skillsForQuestion)
        val banPass = ban.intersect(skillsForQuestion).isEmpty
        reqPass && banPass
      })
    })
  }

//  def questionSearchSet(nameQuery: String, requiredSkills: Seq[String], bannedSkills: Seq[String]): Future[Seq[(Question, Set[Skill])]] = {
//    val req = requiredSkills.toSet
//    val ban = bannedSkills.toSet
//
//    val ret = questionSearch(nameQuery).map(s => s.groupBy(_._1).mapValues(_.map(_._2)).mapValues(_.toSet).toSeq)
//
//    ret.map(questionList => {
//      questionList.filter(question => {
//        val skillsForQuestion = question._2.map(_.name).toSet
//        val reqPass = req.subsetOf(skillsForQuestion)
//        val banPass = ban.intersect(skillsForQuestion).isEmpty
//        reqPass && banPass
//      })
//    })
//  }


  // ====== Create ======
  def insert(questionFrame: QuestionFrame) : Future[QuestionFrame] = {
    insert(questionFrame.question).flatMap{ question => {
      val skillsFuture = skillDAO.addSkills(question, questionFrame.skills) // Note we don't create new skills here they should already exist, but we need to "wait" on the future

      val userConstantsFuture = userConstantsDAO.insert(questionFrame.userConstants.questionId(question.id))

      val sectionsFutures : Seq[Future[QuestionSectionFrame]] = questionFrame.id(question.id).sections.map(section => insert(section))
      val futureOfSections : Future[Vector[QuestionSectionFrame]] = com.artclod.concurrent.raiseFuture(sectionsFutures).map(_.sorted)

      userConstantsFuture.flatMap(userConstants => skillsFuture.flatMap(skillCount => futureOfSections.map(sections =>
        QuestionFrame(question, sections, questionFrame.skills, userConstants)
      )))

      }
    }
  }

  def insert(sectionFrame: QuestionSectionFrame) : Future[QuestionSectionFrame] = {
    insert(sectionFrame.section).flatMap(section => {
      (sectionFrame.id(section.id).parts match {
        case First(ps)  => insertChoices(ps).map(p   => First[  Vector[QuestionPartChoice], Vector[QuestionPartFunction], Vector[QuestionPartSequence] ](Vector(p:_*).sorted))
        case Second(ps) => insertFunctions(ps).map(p => Second[ Vector[QuestionPartChoice], Vector[QuestionPartFunction], Vector[QuestionPartSequence] ](Vector(p:_*).sorted))
        case Third(ps)  => insertSequences(ps).map(p => Third[  Vector[QuestionPartChoice], Vector[QuestionPartFunction], Vector[QuestionPartSequence] ](Vector(p:_*).sorted))
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

  def insertSequences(questionPartSequence: Seq[QuestionPartSequence]): Future[Seq[QuestionPartSequence]] = db.run {
    (QuestionPartSequences returning QuestionPartSequences.map(_.id) into ((needsId, id) => needsId.copy(id = id))) ++= questionPartSequence
  }

  // ====== Update ======
  // https://stackoverflow.com/questions/16757368/how-do-you-update-multiple-columns-using-slick-lifted-embedding
  def update(question: Question, archived: Short) =
    db.run((for { q <- Questions if q.id === question.id } yield q ).map(q => (q.archivedNum) ).update( archived))

}

