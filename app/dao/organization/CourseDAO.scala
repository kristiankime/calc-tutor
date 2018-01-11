package dao.organization

import javax.inject.Inject
import javax.inject.Singleton

import dao.ColumnTypeMappings
import dao.organization.table.{CourseTables, OrganizationTables}
import dao.quiz.QuizDAO
import dao.user.UserDAO
import dao.user.table.UserTables
import models._
import models.organization.{Course, Course2Quiz, Organization, User2Course}
import models.quiz.Quiz
import models.user.User
import org.joda.time.DateTime
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.mvc.Result
import play.api.mvc.Results.NotFound
import slick.lifted
import slick.lifted.PrimaryKey
import models.organization.Course2Quiz

import scala.concurrent.{ExecutionContext, Future}

// ====
import slick.driver.JdbcProfile
//import slick.jdbc.JdbcProfile // Use this after upgrading slick
// ====

@Singleton
class CourseDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider, protected val userTables: UserTables, protected val organizationTables: OrganizationTables, protected val courseTables: CourseTables)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] with ColumnTypeMappings {
  // ====
  //  import profile.api._ // Use this after upgrading slick
  import dbConfig.driver.api._
  // ====

  // * ====== TABLE INSTANCES ====== *
  val Courses = courseTables.Courses
  val User2Courses = courseTables.User2Courses

  // * ====== QUERIES ====== *

  // ====== FIND ======
  def all(): Future[Seq[Course]] = db.run(Courses.result)

  def byId(id : CourseId): Future[Option[Course]] = db.run(Courses.filter(_.id === id).result.headOption)

  def apply(courseId: CourseId): Future[Either[Result, Course]] = byId(courseId).map { _ match {
    case None => Left(NotFound(views.html.errors.notFoundPage("There was no course for id=["+courseId+"]")))
    case Some(course) => Right(course)
  } }

  def byIds(organizationId: OrganizationId, id : CourseId): Future[Option[Course]] = db.run(Courses.filter(c => c.id === id && c.organizationId === organizationId).result.headOption)

  def apply(organizationId: OrganizationId, courseId: CourseId): Future[Either[Result, Course]] = byIds(organizationId, courseId).map { _ match {
    case None => Left(NotFound(views.html.errors.notFoundPage("There was no Course for id=["+courseId+"] which also had Organization Id [" + organizationId + "]")))
    case Some(course) => Right(course)
  } }

  def coursesFor(organizationId: OrganizationId) : Future[Seq[Course]] = db.run(Courses.filter(_.organizationId === organizationId).result)

  // ====== Access ======
  def access(userId: UserId, courseId : CourseId): Future[Access] = db.run {
    val ownerAccess = (for(c <- Courses if c.ownerId === userId && c.id === courseId) yield c).result.headOption.map(_ match { case Some(_) => Own case None => Non})
    val directAccess = (for(u2c <- User2Courses if u2c.userId === userId && u2c.courseId === courseId) yield u2c.access).result.headOption.map(_.getOrElse(Non))
    ownerAccess.flatMap(oa => directAccess.map( da => oa max da))
  }

  def grantAccess(user: User, course: Course, access: Access) =
    db.run(User2Courses += User2Course(user.id, course.id, access)).map { _ => () }

  def revokeAccess(user: User, course: Course) =
    db.run(User2Courses.filter(u2c => u2c.userId === user.id && u2c.courseId === course.id).delete)

  def coursesFor(user: User): Future[Seq[Course]] =
    db.run({
      (for(u2c <- User2Courses; c <- Courses if u2c.userId === user.id && u2c.courseId === c.id) yield c)
        .union(
        Courses.filter(_.ownerId === user.id)
      ).result })

  def coursesAndAccessFor(user: User): Future[Seq[(Course, Access)]] =
    db.run({
      (for(u2c <- User2Courses; c <- Courses if u2c.userId === user.id && u2c.courseId === c.id) yield (c, u2c.access))
        .union(
          Courses.filter(_.ownerId === user.id).map( (_, models.Own))
        ).result })

  // ====== Create ======
  def insert(course: Course): Future[Course] =
    db.run({ (Courses returning Courses.map(_.id) into ((needsId, id) => needsId.copy(id = id))) += course })

}

