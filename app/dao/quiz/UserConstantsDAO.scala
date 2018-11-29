package dao.quiz

import java.util.Objects
import java.util.concurrent.TimeUnit

import javax.inject.{Inject, Singleton}
import com.artclod.mathml.scalar.MathMLElem
import dao.ColumnTypeMappings
import dao.quiz.table.{QuestionTables, SkillTables}
import dao.user.UserDAO
import dao.user.table.UserTables
import models._
import models.organization.{Course, Course2Quiz}
import models.quiz.{AnswerPartFunction, _}
import org.joda.time.DateTime
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.mvc.Result
import play.api.mvc.Results._
import slick.lifted
import com.artclod.util._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

// ====
import slick.driver.JdbcProfile
//import slick.jdbc.JdbcProfile // Use this after upgrading slick
// ====

@Singleton
class UserConstantsDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider, protected val questionTables: QuestionTables)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] with ColumnTypeMappings {

  // ====
  //  import profile.api._ // Use this after upgrading slick
  import dbConfig.driver.api._

  // * ====== TABLE INSTANCES ====== *
  import questionTables.{QuestionUserConstantIntegers, QuestionUserConstantDecimals, QuestionUserConstantSets }

  // * ====== QUERIES ====== *

  // ====== FIND ======
  def allUserConstants(questionId: QuestionId): Future[(Seq[QuestionUserConstantInteger], Seq[QuestionUserConstantDecimal], Seq[QuestionUserConstantSet])] = {
    val intsFuture = db.run(QuestionUserConstantIntegers.filter(_.questionId === questionId).result)
    val decsFuture = db.run(QuestionUserConstantDecimals.filter(_.questionId === questionId).result)
    val setsFuture = db.run(QuestionUserConstantSets.filter(_.questionId === questionId).result)

    intsFuture.flatMap(ints =>
      decsFuture.flatMap( decs =>
        setsFuture.map(sets =>
          (ints, decs, sets)
        )))
  }

  // ====== Create ======
  def insert(questionUserConstantsFrame: QuestionUserConstantsFrame) : Future[QuestionUserConstantsFrame] = {
    val intsFuture = insertIntegers(questionUserConstantsFrame.integers)
    val decsFuture = insertDecimals(questionUserConstantsFrame.decimals)
    val setsFuture = insertSets(questionUserConstantsFrame.sets)
    intsFuture.flatMap(ints => decsFuture.flatMap(decs => setsFuture.map(sets => QuestionUserConstantsFrame(ints.toVector, decs.toVector, sets.toVector) )))
  }

  def insertIntegers(userConstants: Seq[QuestionUserConstantInteger]): Future[Seq[QuestionUserConstantInteger]] = db.run {
    (QuestionUserConstantIntegers returning QuestionUserConstantIntegers.map(_.id) into ((needsId, id) => needsId.copy(id = id))) ++= userConstants
  }

  def insertDecimals(userConstants: Seq[QuestionUserConstantDecimal]): Future[Seq[QuestionUserConstantDecimal]] = db.run {
    (QuestionUserConstantDecimals returning QuestionUserConstantDecimals.map(_.id) into ((needsId, id) => needsId.copy(id = id))) ++= userConstants
  }

  def insertSets(userConstants: Seq[QuestionUserConstantSet]): Future[Seq[QuestionUserConstantSet]] = db.run {
    (QuestionUserConstantSets returning QuestionUserConstantSets.map(_.id) into ((needsId, id) => needsId.copy(id = id))) ++= userConstants
  }

}
