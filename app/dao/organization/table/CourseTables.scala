package dao.organization.table

import javax.inject.{Inject, Singleton}

import dao.ColumnTypeMappings
import dao.user.UserDAO
import dao.user.table.UserTables
import models._
import models.organization.{Course, User2Course}
import models.user.User
import org.joda.time.DateTime
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.mvc.Result
import play.api.mvc.Results.NotFound
import slick.lifted

import scala.concurrent.{ExecutionContext, Future}

// ====
import slick.driver.JdbcProfile
//import slick.jdbc.JdbcProfile // Use this after upgrading slick
// ====

@Singleton
class CourseTables @Inject()(protected val dbConfigProvider: DatabaseConfigProvider, protected val userTables: UserTables, protected val organizationTables: OrganizationTables)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] with ColumnTypeMappings {
  // ====
  //  import profile.api._ // Use this after upgrading slick
  import dbConfig.driver.api._
  // ====

  // * ====== TABLE INSTANCES ====== *
  val Courses = lifted.TableQuery[CourseTable]
  val User2Courses = lifted.TableQuery[User2CourseTable]

  // * ====== TABLE CLASSES ====== *
  class CourseTable(tag: Tag) extends Table[Course](tag, "course") {
    def id = column[CourseId]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def organizationId = column[OrganizationId]("organization_id")
    def ownerId = column[UserId]("owner_id")
    def editCode = column[String]("edit_code")
    def viewCode = column[Option[String]]("view_code")
    def creationDate = column[DateTime]("creation_date")
    def updateDate = column[DateTime]("update_date")

    def ownerIdFK = foreignKey("course_fk__owner_id", ownerId, userTables.Users)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def organizationIdFK = foreignKey("course_fk__course", organizationId, organizationTables.Organizations)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

    def * = (id, name, organizationId, ownerId, editCode, viewCode, creationDate, updateDate) <> (Course.tupled, Course.unapply)
  }

  class User2CourseTable(tag: Tag) extends Table[User2Course](tag, "app_user_2_course") {
    def userId = column[UserId]("user_id")
    def courseId = column[CourseId]("course_id")
    def access = column[Access]("access")

    def pk = primaryKey("app_user_2_course_pk", (userId, courseId))

    def userIdFK = foreignKey("app_user_2_course_fk__user_id", userId, userTables.Users)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def courseIdFK = foreignKey("app_user_2_course_fk__course_id", courseId, Courses)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

    def * = (userId, courseId, access) <> (User2Course.tupled, User2Course.unapply)
  }

}

