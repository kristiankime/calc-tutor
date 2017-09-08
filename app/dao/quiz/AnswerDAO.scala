package dao.quiz

import javax.inject.{Inject, Singleton}

import com.artclod.mathml.scalar.MathMLElem
import dao.ColumnTypeMappings
import dao.user.UserDAO
import models._
import models.quiz._
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
class AnswerDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider, protected val userDAO: UserDAO)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] with ColumnTypeMappings {
  // ====
  //  import profile.api._ // Use this after upgrading slick
  import dbConfig.driver.api._

  // * ====== TABLE INSTANCES ====== *

  // * ====== QUERIES ====== *

  // ====== FIND ======

  // ---

  // ====== Create ======


  // === Support ===


  // * ====== TABLE CLASSES ====== *


}

