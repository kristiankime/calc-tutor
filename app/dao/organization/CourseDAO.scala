package dao.organization

import javax.inject.Inject

import dao.ColumnTypeMappings
import dao.user.UserDAO
import models.{Access, CourseId, OrganizationId, UserId}
import models.organization.{Course, User2Course}
import org.joda.time.DateTime
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.lifted

import scala.concurrent.{ExecutionContext, Future}

// ====
import slick.driver.JdbcProfile
//import slick.jdbc.JdbcProfile // Use this after upgrading slick
// ====

class CourseDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider, protected val organizationDAO: OrganizationDAO, protected val userDAO: UserDAO)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] with ColumnTypeMappings {
  // ====
  //  import profile.api._ // Use this after upgrading slick
  import dbConfig.driver.api._
  // ====

  val Courses = lifted.TableQuery[CourseTable]
  val User2Courses = lifted.TableQuery[CourseTable]

  def all(): Future[Seq[Course]] = db.run(Courses.result)

  def insert(course: Course): Future[Unit] = db.run(Courses += course).map { _ => () }

  def byId(id : CourseId): Future[Option[Course]] = db.run(Courses.filter(_.id === id).result.headOption)

  class CourseTable(tag: Tag) extends Table[Course](tag, "course") {
    def id = column[CourseId]("id", O.PrimaryKey)
    def name = column[String]("name")
    def organizationId = column[OrganizationId]("organization_id")
    def ownerId = column[UserId]("owner_id")
    def editCode = column[String]("edit_code")
    def viewCode = column[Option[String]]("view_code")
    def creationDate = column[DateTime]("creation_date")
    def updateDate = column[DateTime]("update_date")

    def organizationIdFK = foreignKey("course_fk__organization_id", organizationId, organizationDAO.Organizations)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

    def * = (id, name, organizationId, ownerId, editCode, viewCode, creationDate, updateDate) <> (Course.tupled, Course.unapply)
  }

  class User2CourseTable(tag: Tag) extends Table[User2Course](tag, "app_user_2_course") {
    def userId = column[UserId]("user_id")
    def courseId = column[CourseId]("course_id")
    def access = column[Access]("access")

    def userIdFK = foreignKey("app_user_2_course_fk__userId", userId, userDAO.Users)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def courseIdFK = foreignKey("app_user_2_course_fk__courseId", courseId, Courses)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

    def * = (userId, courseId, access) <> (User2Course.tupled, User2Course.unapply)
  }
}

