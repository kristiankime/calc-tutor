package dao.auth.table

import javax.inject.{Inject, Singleton}

import models.auth.NamePassLogin
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.lifted

import scala.concurrent.{ExecutionContext, Future}

// ====
import slick.driver.JdbcProfile
//import slick.jdbc.JdbcProfile // Use this after upgrading slick
// ====

@Singleton
class NamePassTable @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
  // ====
  //  import profile.api._ // Use this after upgrading slick
  import dbConfig.driver.api._
  // ====

  val Logins = lifted.TableQuery[LoginTable]

  class LoginTable(tag: Tag) extends Table[NamePassLogin](tag, "name_pass_login") {
    def id = column[String]("id", O.PrimaryKey)
    def userName = column[String]("user_name")
    def password = column[String]("password")
    def linkedId = column[Option[String]]("linkedid")
    def serializedprofile = column[Option[String]]("serializedprofile")

    def * = (id, userName, password, linkedId, serializedprofile) <> (NamePassLogin.tupled, NamePassLogin.unapply)
  }
}

