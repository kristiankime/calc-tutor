package dao.quiz

import javax.inject.{Inject, Singleton}

import com.artclod.mathml.scalar.MathMLElem
import com.artclod.slick.JodaUTC
import dao.ColumnTypeMappings
import dao.organization.CourseDAO
import dao.organization.table.CourseTables
import dao.quiz.table.{QuestionTables, QuizTables}
import dao.user.UserDAO
import dao.user.table.UserTables
import models.quiz.{User2Quiz, _}
import models.{organization, _}
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
class QuizDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider, protected val userTables: UserTables, protected val courseTables: CourseTables, protected val quizTables: QuizTables, protected val questionTables: QuestionTables, protected val questionDAO: QuestionDAO)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] with ColumnTypeMappings {
  // ====
  //  import profile.api._ // Use this after upgrading slick
  import dbConfig.driver.api._

  // * ====== TABLE INSTANCES ====== *
  val Quizzes = quizTables.Quizzes
  val User2Quizzes = quizTables.User2Quizzes
  val Courses2Quizzes = quizTables.Courses2Quizzes
  val Question2Quizzes = quizTables.Question2Quizzes

  // * ====== QUERIES ====== *

  // ====== FIND ======
  def all(): Future[Seq[Quiz]] = db.run(Quizzes.result)

  def byId(id : QuizId): Future[Option[Quiz]] = db.run(Quizzes.filter(_.id === id).result.headOption)

  def apply(quizId: QuizId): Future[Either[Result, Quiz]] = byId(quizId).map { quizOp => quizOp match {
    case None => Left(NotFound(views.html.errors.notFoundPage("There was no quiz for id=["+quizId+"]")))
    case Some(quiz) => Right(quiz)
  } }

  def byIds(courseId: CourseId, quizId: QuizId): Future[Option[Quiz]] = db.run{
    (for(z <- Quizzes; c2z <- Courses2Quizzes if c2z.courseId === courseId && c2z.quizId === quizId && z.id === quizId ) yield z).result.headOption
  }

  def apply(courseId: CourseId, quizId: QuizId): Future[Either[Result, Quiz]] = byIds(courseId, quizId).map { _ match {
    case None => Left(NotFound(views.html.errors.notFoundPage("There was no Quiz for id=["+quizId+"] which also had Course Id [" + courseId + "]")))
    case Some(quiz) => Right(quiz)
  } }

  def quizzesFor(courseId: CourseId) : Future[Seq[Quiz]] = db.run {
    (for(c2z <- Courses2Quizzes; z <- Quizzes if c2z.courseId === courseId && c2z.quizId === z.id) yield z).result
  }

  def questionSummariesFor(quiz: Quiz): Future[Seq[Question]] = db.run {
    (for(q2z <- Question2Quizzes; q <- questionTables.Questions if q2z.quizId === quiz.id && q2z.questionId === q.id) yield (q2z, q))
      .sortBy(_._1.order).map(_._2) // sort and return Question
      .result
  }

  def frameById(id : QuizId): Future[Option[QuizFrame]] = {
    val quizFuture = byId(id)
    quizFuture.flatMap(_ match {
      case Some(quiz) => {
        questionSummariesFor(quiz).flatMap(questions => {
          val questionFrameFutures: Seq[Future[QuestionFrame]] = questions.map(q => questionDAO.frameById(q.id).map(_.get) )
          val futureQuestionFrames: Future[Vector[QuestionFrame]] = com.artclod.concurrent.raiseFuture(questionFrameFutures)
          futureQuestionFrames.map(questionFrames => Some(QuizFrame(quiz, questionFrames)))
        })
      }
      case None => Future(None)
    })
  }

  // --
  def frameByIdEither(quizId : QuizId): Future[Either[Result, QuizFrame]] = frameById(quizId).map { _ match {
    case None => Left(NotFound(views.html.errors.notFoundPage("There was no quiz for id=["+quizId+"]")))
    case Some(quizFrame) => Right(quizFrame)
  } }

  // ====== Access ======
  def access(userId: UserId, quizId : QuizId): Future[Access] = db.run {
    val ownerAccess = (for(z <- Quizzes if z.ownerId === userId && z.id === quizId) yield z).result.headOption.map(_ match { case Some(_) => Own case None => Non})
    val directAccess = (for(u2z <- User2Quizzes if u2z.userId === userId && u2z.quizId === quizId) yield u2z.access).result.headOption.map(_.getOrElse(Non))
    val courseAccess = (for(u2c <- courseTables.User2Courses; c2z <- Courses2Quizzes if u2c.userId === userId && u2c.courseId === c2z.courseId && c2z.quizId === quizId) yield u2c.access).result.headOption.map(_.getOrElse(Non));

    ownerAccess.flatMap(owner => directAccess.flatMap(direct => courseAccess.map(course => owner max direct max course)))
  }

  def attach(course: Course, quiz: Quiz) = db.run(Courses2Quizzes += Course2Quiz(course.id, quiz.id, None, None)).map { _ => () }

  def attach(question: Question, quiz: Quiz, userId: UserId) = {
    val lastOrder = db.run( Question2Quizzes.filter(_.quizId === quiz.id).map(_.order).max.result)
    lastOrder.flatMap( lo => {
      val nextOrder = (lo.getOrElse(-1) + 1).toShort
      db.run(Question2Quizzes += Question2Quiz(question.id, quiz.id, userId, JodaUTC.now, nextOrder)).map { _ => () }
    })
  }

  // TODO should we try to reorder?
  def detach(question: Question, quiz: Quiz): Future[Int] = db.run(Question2Quizzes.filter(q2q => q2q.quizId === quiz.id && q2q.questionId === question.id).delete)

  def grantAccess(user: User, quiz: Quiz, access: Access) = db.run(User2Quizzes += User2Quiz(user.id, quiz.id, access)).map { _ => () }

  // ====== Create ======
  def insert(quiz: Quiz): Future[Quiz] = db.run(
    (Quizzes returning Quizzes.map(_.id) into ((needsId, id) => needsId.copy(id = id))) += quiz
  )

  def insert(quizFrame: QuizFrame): Future[QuizFrame] = {
    val quizFuture = insert(quizFrame.quiz)
    val questionFrameFutures: Seq[Future[QuestionFrame]] = quizFrame.questions.map(qf => questionDAO.insert(qf))
    val futureQuestionFrames: Future[Vector[QuestionFrame]] = com.artclod.concurrent.raiseFuture(questionFrameFutures)
    quizFuture.flatMap(quiz => futureQuestionFrames.map(qfs =>  QuizFrame(quiz, qfs)))
  }

  def updateName(quiz: Quiz, name: String): Future[Int] = db.run {
    (for { z <- Quizzes if z.id === quiz.id } yield z.name ).update(name)
  }

}

