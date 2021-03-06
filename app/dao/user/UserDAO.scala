package dao.user

import javax.inject.Inject

import com.artclod.slick.JodaUTC
import dao.ColumnTypeMappings
import dao.organization.table.CourseTables
import dao.user.table.UserTables
import models.{Access, UserId}
import models.organization.Course
import models.user.User
import org.joda.time.DateTime
import org.pac4j.core.profile.CommonProfile
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.lifted
import play.api.mvc.Result
import play.api.mvc.Results.NotFound

import scala.concurrent.{ExecutionContext, Future}

// ====
import slick.driver.JdbcProfile
//import slick.jdbc.JdbcProfile // Use this after upgrading slick
// ====

class UserDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider, protected val userTables: UserTables, protected val courseTables: CourseTables)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] with ColumnTypeMappings {
  // ====
  //  import profile.api._ // Use this after upgrading slick
  import dbConfig.driver.api._
  // ====

  val Users = userTables.Users

  def all(): Future[Seq[User]] = db.run(Users.result)

//  def insert(user: User): Future[Unit] = db.run(Users += user).map { _ => () }
  def insert(user: User): Future[User] = db.run(
    (Users returning Users.map(_.id) into ((needsId, id) => needsId.copy(id = id))) += user
  )

  def updateConsent(user: User, consent: Boolean): Future[Int] = db.run(Users.insertOrUpdate(user.copy(consented = consent)))

  def updateSettings(user: User, name: Option[String], emailUpdates: Boolean): Future[Int] = {
    val userUpdate = name match {
      case Some(n) => user.copy(name = n, emailUpdates = emailUpdates)
      case None => user.copy(emailUpdates = emailUpdates)
    }
    db.run(Users.insertOrUpdate(userUpdate))
  }

  def byId(id : UserId): Future[Option[User]] = db.run(Users.filter(_.id === id).result.headOption)

  def byIdEither(id : UserId): Future[Either[Result, User]] = db.run(Users.filter(_.id === id).result.headOption.map(_ match {
    case Some(user) => Right(user)
    case None => Left(NotFound(views.html.errors.notFoundPage("There was no user for user [" + id + "]" )))
  }))

  def ensureByLoginId(profiles: List[CommonProfile]): Future[User] = ensureByLoginId(profiles.head) // TODO handle multiple profiles

  def ensureByLoginId(p: CommonProfile): Future[User] =
      db.run(Users.filter(_.loginId === p.getId).result.headOption).flatMap { optionUser =>
        optionUser match {
            case Some(user) => Future(user)
            case None => insert(User(loginId = p.getId, name = Option(p.getUsername).getOrElse("Player"), email = Option(p.getEmail), lastAccess = JodaUTC.now))
          }
      }

  def students(): Future[Seq[User]] = db.run({
    (for (u2c <- courseTables.User2Courses; u <- Users
          if u2c.access === Access.view && u2c.userId === u.id
    ) yield u).sortBy(_.name).result
  })

  def studentIds(): Future[Seq[UserId]] = db.run({
    (for (u2c <- courseTables.User2Courses; u <- Users
          if u2c.access === Access.view && u2c.userId === u.id
    ) yield u.id).result
  })
}

