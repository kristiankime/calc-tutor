package dao.user

import javax.inject.Inject

import models.auth.NamePassLogin
import models.user.User
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile
import slick.lifted

import scala.concurrent.{ExecutionContext, Future}

// ====
import slick.driver.JdbcProfile
//import slick.jdbc.JdbcProfile // Use this after upgrading slick
// ====

class UserDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
  // ====
  //  import profile.api._ // Use this after upgrading slick
  import dbConfig.driver.api._
  // ====

  val Users = lifted.TableQuery[UserTable]

  def all(): Future[Seq[User]] = db.run(Users.result)

  def insert(cat: User): Future[Unit] = db.run(Users += cat).map { _ => () }

  def byId(id : String): Future[Option[User]] = db.run(Users.filter(_.id === id).result.headOption)

  class UserTable(tag: Tag) extends Table[User](tag, "app_user") {
    def id = column[String]("id", O.PrimaryKey)
    def name = column[String]("name")
    def email = column[Option[String]]("email")

    def * = (id, name, email) <> (User.tupled, User.unapply)
  }
}

