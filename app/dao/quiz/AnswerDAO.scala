package dao.quiz

import javax.inject.{Inject, Singleton}

import com.artclod.mathml.scalar.MathMLElem
import com.artclod.slick.NumericBoolean
import dao.ColumnTypeMappings
import dao.organization.CourseDAO
import dao.organization.table.CourseTables
import dao.quiz.table.{AnswerTables, QuestionTables, QuizTables}
import dao.user.UserDAO
import dao.user.table.UserTables
import models._
import models.quiz.{AnswerPartFunction, _}
import models.support.HasOrder
import models.user.User
import org.joda.time.DateTime
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.mvc.Result
import play.api.mvc.Results._
import play.twirl.api.Html
import slick.lifted
import models.View
import models.organization.Course

import scala.concurrent.{ExecutionContext, Future}

// ====
import slick.driver.JdbcProfile
//import slick.jdbc.JdbcProfile // Use this after upgrading slick
// ====

@Singleton
class AnswerDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider, protected val userDAO: UserDAO, protected val questionDAO: QuestionDAO, protected val skillDAO: SkillDAO, protected val courseDAO: CourseDAO, protected val quizDAO: QuizDAO, protected val answerTables: AnswerTables, protected val quizTables: QuizTables, protected val questionTables: QuestionTables, protected val courseTables: CourseTables, protected val userTables: UserTables)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] with ColumnTypeMappings {
  // ====
  //  import profile.api._ // Use this after upgrading slick
  import dbConfig.driver.api._

  // * ====== TABLE INSTANCES ====== *
  val Answers = answerTables.Answers
  val AnswerSections = answerTables.AnswerSections
  val AnswerPartsFunctions = answerTables.AnswerPartFunctions
  val AnswerPartSequences = answerTables.AnswerPartSequences

  // * ====== QUERIES ====== *

  // ====== FIND ======
  def byId(id : AnswerId): Future[Option[Answer]] = db.run(Answers.filter(_.id === id).result.headOption)

  def sectionsById(id : AnswerId): Future[Seq[AnswerSection]] = db.run(AnswerSections.filter(_.answerId === id).result)

  def functionPartsId(id : AnswerId): Future[Seq[AnswerPartFunction]] = db.run(AnswerPartsFunctions.filter(_.answerId === id).result)
  def sequencePartsId(id : AnswerId): Future[Seq[AnswerPartSequence]] = db.run(AnswerPartSequences.filter(_.answerId === id).result)

  def frameById(id : AnswerId): Future[Option[AnswerFrame]] = {
    val answerFuture = byId(id)
    val sectionsFuture = sectionsById(id)
    val functionPartsFuture = functionPartsId(id)
    val sequencePartsFuture = sequencePartsId(id)

    answerFuture.flatMap(answerOp => { sectionsFuture.flatMap(sections => { functionPartsFuture.flatMap( functionParts => { sequencePartsFuture.map( sequenceParts => {
      val secId2FunctionParts: Map[AnswerSectionId, Seq[AnswerPartFunction]] = functionParts.groupBy(p => p.answerSectionId)
      val secId2SequenceParts: Map[AnswerSectionId, Seq[AnswerPartSequence]] = sequenceParts.groupBy(p => p.answerSectionId)

      val sectionFrames = sections.map(section => AnswerSectionFrame(section,
        secId2FunctionParts.getOrElse(section.id, Seq()).toVector.sorted,
        secId2SequenceParts.getOrElse(section.id, Seq()).toVector.sorted,
        false))

      answerOp.map(answer => {
        sectionFrames.nonEmpty match {
          case false => throw new IllegalArgumentException("There were no sections for id = " + id)
          case true => AnswerFrame(answer, Vector(sectionFrames:_*).sorted, false)
        }
      })

    }) }) }) })
  }

//  def correctOrLatest(questionId: QuestionId, userId: UserId): Future[Option[(Answer, Seq[Answer])]] =
//    db.run(Answers.filter(a => a.questionId === questionId && a.ownerId === userId).sortBy(_.creationDate).result).map(answers => {
//      if(answers.length == 0) {
//        return None()
//      } else {
//      answers.find(_.correct) match {
//        case Some(firstCorrectAnswer) => Some(firstCorrectAnswer, answers)
//        case None => Some(answers.head, answers)
//      }}
//    })

  def correctOrLatestEither(questionId: QuestionId, userId: UserId): Future[Either[Result,(Answer, Seq[Answer])]] =
    db.run(Answers.filter(a => a.questionId === questionId && a.ownerId === userId).sortBy(_.creationDate.reverse).result).map(answers => {
      if(answers.length == 0) {
        return Future.successful( Left(NotFound(views.html.errors.notFoundPage("There was no answers for questionId=["+questionId+"] by user [" + userId + "]" ))) )
      } else {
        answers.find(_.correct) match {
          case Some(firstCorrectAnswer) => Right( (firstCorrectAnswer, answers) )
          case None => Right( (answers.head, answers) )
        }}
    })

  // ---
  def numberOfAttempts(userId: UserId, questionId: QuestionId): Future[Int] =
    db.run(Answers.filter(a => a.ownerId === userId && a.questionId === questionId).length.result)

  def attempts(userId: UserId, questionId: QuestionId): Future[Seq[Answer]] =
    db.run(Answers.filter(a => a.ownerId === userId && a.questionId === questionId).result)

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

  def frameByIdEither(answerIdOp: Option[AnswerId]): Future[Either[Result, Option[AnswerFrame]]] =
    answerIdOp match {
      case Some(answerId) => frameById(answerId).map { _ match {
        case None => Left(NotFound(views.html.errors.notFoundPage("There was no question for id=["+answerId+"]")))
        case Some(answer) => Right(Some(answer))
      } }
      case None => Future.successful(Right(None))
    }

  def frameByIdEither(questionId: QuestionId, answerIdOp: Option[AnswerId]): Future[Either[Result, Option[AnswerFrame]]] =
    answerIdOp match {
      case Some(answerId) => frameByIdEither(questionId, answerId).map(_.right.map(Some(_)) )
      case None => Future.successful(Right(None))
    }

  def frameByIdEither(questionId: QuestionId, answerId: AnswerId): Future[Either[Result, AnswerFrame]] =
    frameById(answerId).map { _ match {
        case None => Left(NotFound(views.html.errors.notFoundPage("There was no question for id=["+answerId+"]")))
        case Some(answer) =>
          if(answer.answer.questionId != questionId) {
            Left(NotFound(views.html.errors.notFoundPage("The answer for id=["+answerId+"] does not match the question for id=["+questionId+"]")))
          } else {
            Right(answer)
          }
      }
    }

  // ====== Create ======
  def insert(answerFrame: AnswerFrame) : Future[AnswerFrame] = {
    insert(answerFrame.answer).flatMap(answer => {
      val sectionsFutures : Seq[Future[AnswerSectionFrame]] = answerFrame.id(answer.id).sections.map(section => insert(section))
      val futureOfSections : Future[Vector[AnswerSectionFrame]] = com.artclod.concurrent.raiseFuture(sectionsFutures).map(_.sorted)
      futureOfSections.map(sections => AnswerFrame(answer, sections, false))
    })
  }

  def insert(sectionFrame: AnswerSectionFrame) : Future[AnswerSectionFrame] = {
    insert(sectionFrame.answerSection).flatMap(section => {
      insertFunction(sectionFrame.id(section.id).functionParts).flatMap(functionParts =>
        insertSequence(sectionFrame.id(section.id).sequenceParts).map(sequenceParts =>

          AnswerSectionFrame(answerSection = section,
            functionParts = functionParts.toVector.sorted,
            sequenceParts = sequenceParts.toVector.sorted,
            false)))

    })
  }

  def insert(answer: Answer): Future[Answer] = db.run(
    (Answers returning Answers.map(_.id) into ((needsId, id) => needsId.copy(id = id))) += answer
  )

  def insert(answerSection: AnswerSection): Future[AnswerSection] = db.run(
    (AnswerSections returning AnswerSections.map(_.id) into ((needsId, id) => needsId.copy(id = id))) += answerSection
  )

  def insertFunction(answerParts: Seq[AnswerPartFunction]): Future[Seq[AnswerPartFunction]] = db.run {
    (AnswerPartsFunctions returning AnswerPartsFunctions.map(_.id) into ((needsId, id) => needsId.copy(id = id))) ++= answerParts
  }

  def insertSequence(answerParts: Seq[AnswerPartSequence]): Future[Seq[AnswerPartSequence]] = db.run {
    (AnswerPartSequences returning AnswerPartSequences.map(_.id) into ((needsId, id) => needsId.copy(id = id))) ++= answerParts
  }

  // ----
  def updateSkillCounts(userId: UserId, questionId: QuestionId, correct: Boolean): Future[Boolean] =
    numberOfAttempts(userId, questionId).flatMap(num => num match {
      case 0 => skillDAO.incrementsCounts(userId, questionId, if(correct){1}else{0}, if(correct){0}else{1}).map(_ => true)
      case _ => Future.successful(false)
    })


  // ======= Results ======

  // ---- Table of users to questions ----
  def results(userIds: Seq[UserId], questionIds: Seq[QuestionId]): Future[Seq[(UserId, QuestionId, Option[Short])]] = db.run(
    Answers.filter(a => a.ownerId.inSet(userIds) && a.questionId.inSet(questionIds)).
      groupBy(a => (a.ownerId, a.questionId)).map{ case(ids, group) => (ids._1, ids._2, group.map(_.correct).max )}.result
  )

  def resultsTable(users: Seq[User], questions: Seq[Question]): Future[QuizResultTable] = {
    results(users.map(_.id), questions.map(_.id)).map(rs => {
      val resultsMap: Map[(UserId, QuestionId), Option[Short]] = rs.groupBy(r => (r._1, r._2)).mapValues(_.head._3)
      val rows: Seq[QuizResultTableRow] = users.map(u => QuizResultTableRow(u, questions.map(q => resultsMap.getOrElse((u.id, q.id), None).map(NumericBoolean(_)))))
      QuizResultTable(questions, rows)
    })
  }

  def resultsTable(users: Seq[User], quiz: Quiz): Future[QuizResultTable] =
    quizDAO.questionSummariesFor(quiz).flatMap( qs => resultsTable(users, qs) )

  def resultsTable(course: Course, quiz: Quiz): Future[QuizResultTable] =
    courseDAO.studentsIn(course).flatMap(users => resultsTable(users, quiz) )

  // ---- Single user summary for multiple questions ----
//  def results(userId: UserId, questionIds: Seq[QuestionId]) = db.run(
//    Answers.filter(a => a.ownerId === userId && a.questionId.inSet(questionIds)).
//      groupBy(a => (a.ownerId, a.questionId)).map{ case(ids, group) => (ids._1, ids._2, group.map(_.correct).max )}.result
//  )


}

case class QuizResultTable(questions: Seq[Question], rows : Seq[QuizResultTableRow]) {
  for(row <- rows) { if(questions.size != row.results.size) { throw new IllegalArgumentException("Rows did not match header size") } }
}
case class QuizResultTableRow(user: User, results: Seq[Option[Boolean]])
