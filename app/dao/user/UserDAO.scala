package dao.user

import javax.inject.Inject

import dao.ColumnTypeMappings
import models.UserId
import models.user.User
import org.joda.time.DateTime
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.lifted

import scala.concurrent.{ExecutionContext, Future}

// ====
import slick.driver.JdbcProfile
//import slick.jdbc.JdbcProfile // Use this after upgrading slick
// ====

class UserDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] with ColumnTypeMappings {
  // ====
  //  import profile.api._ // Use this after upgrading slick
  import dbConfig.driver.api._
  // ====

  val Users = lifted.TableQuery[UserTable]

  def all(): Future[Seq[User]] = db.run(Users.result)

//  def insert(user: User): Future[Unit] = db.run(Users += user).map { _ => () }
  def insert(user: User): Future[User] = db.run(
    (Users returning Users.map(_.id) into ((needsId, id) => needsId.copy(id = id))) += user
  )

  def byId(id : UserId): Future[Option[User]] = db.run(Users.filter(_.id === id).result.headOption)

  class UserTable(tag: Tag) extends Table[User](tag, "app_user") {
    def id = column[UserId]("id", O.PrimaryKey, O.AutoInc)
    def loginId = column[String]("login_id")
    def name = column[String]("name")
    def email = column[Option[String]]("email")
    def consented = column[Boolean]("consented")
    def allowAutoMatch = column[Boolean]("allow_auto_match")
    def seenHelp = column[Boolean]("seen_help")
    def emailUpdates = column[Boolean]("email_updates")
    def lastAccess = column[DateTime]("last_access")

    def * = (id, loginId, name, email, consented, allowAutoMatch, seenHelp, emailUpdates, lastAccess) <> (User.tupled, User.unapply)
  }
}

