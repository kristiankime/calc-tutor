package dao.organization

import javax.inject.Inject
import javax.inject.Singleton

import dao.ColumnTypeMappings
import dao.quiz.QuizDAO
import dao.user.UserDAO
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
class CourseDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider, protected val organizationDAO: OrganizationDAO, protected val userDAO: UserDAO, protected val quizDAO: QuizDAO)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] with ColumnTypeMappings {
  // ====
  //  import profile.api._ // Use this after upgrading slick
  import dbConfig.driver.api._
  // ====

  val Courses = lifted.TableQuery[CourseTable]
  val User2Courses = lifted.TableQuery[User2CourseTable]
  val Courses2Quizzes = lifted.TableQuery[Course2QuizTable]

  def all(): Future[Seq[Course]] = db.run(Courses.result)

  def insert(course: Course): Future[Course] = db.run(
    (Courses returning Courses.map(_.id) into ((needsId, id) => needsId.copy(id = id))) += course
  )

  def byId(id : CourseId): Future[Option[Course]] = db.run(Courses.filter(_.id === id).result.headOption)

  def byIds(organizationId: OrganizationId, id : CourseId): Future[Option[Course]] = db.run(Courses.filter(c => c.id === id && c.organizationId === organizationId).result.headOption)

  def access(userId: UserId, courseId : CourseId): Future[Access] = db.run {
    val ownerAccess = (for(c <- Courses if c.ownerId === userId && c.id === courseId) yield c).result.headOption.map(_ match { case Some(_) => Own case None => Non})
    val directAccess = (for(u2c <- User2Courses if u2c.userId === userId && u2c.courseId === courseId) yield u2c.access).result.headOption.map(_.getOrElse(Non))
    ownerAccess.flatMap(oa => directAccess.map( da => oa max da))
  }

  def grantAccess(user: User, course: Course, access: Access) = db.run(User2Courses += User2Course(user.id, course.id, access)).map { _ => () }

  def attach(course: Course, quiz: Quiz) = db.run(Courses2Quizzes += Course2Quiz(course.id, quiz.id, None, None)).map { _ => () }

  def coursesFor(organizationId: OrganizationId) : Future[Seq[Course]] = db.run(Courses.filter(_.organizationId === organizationId).result)

  def quizzesFor(courseId: CourseId) : Future[Seq[Quiz]] = db.run {
    (for(c2z <- Courses2Quizzes; z <- quizDAO.Quizzes if c2z.courseId === courseId && c2z.quizId === z.id) yield z).result
  }

  def apply(courseId: CourseId): Future[Either[Result, Course]] = byId(courseId).map { _ match {
    case None => Left(NotFound(views.html.errors.notFoundPage("There was no course for id=["+courseId+"]")))
    case Some(course) => Right(course)
  } }

  def apply(organizationId: OrganizationId, courseId: CourseId): Future[Either[Result, Course]] = byIds(organizationId, courseId).map { _ match {
    case None => Left(NotFound(views.html.errors.notFoundPage("There was no course for id=["+courseId+"] which also had organizationid [" + OrganizationId + "]")))
    case Some(course) => Right(course)
  } }

  class CourseTable(tag: Tag) extends Table[Course](tag, "course") {
    def id = column[CourseId]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def organizationId = column[OrganizationId]("organization_id")
    def ownerId = column[UserId]("owner_id")
    def editCode = column[String]("edit_code")
    def viewCode = column[Option[String]]("view_code")
    def creationDate = column[DateTime]("creation_date")
    def updateDate = column[DateTime]("update_date")

    def ownerIdFK = foreignKey("course_fk__owner_id", ownerId, userDAO.Users)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def organizationIdFK = foreignKey("course_fk__course", organizationId, organizationDAO.Organizations)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

    def * = (id, name, organizationId, ownerId, editCode, viewCode, creationDate, updateDate) <> (Course.tupled, Course.unapply)
  }

  class User2CourseTable(tag: Tag) extends Table[User2Course](tag, "app_user_2_course") {
    def userId = column[UserId]("user_id")
    def courseId = column[CourseId]("course_id")
    def access = column[Access]("access")

    def pk = primaryKey("app_user_2_course_pk", (userId, courseId))

    def userIdFK = foreignKey("app_user_2_course_fk__user_id", userId, userDAO.Users)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def courseIdFK = foreignKey("app_user_2_course_fk__course_id", courseId, Courses)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

    def * = (userId, courseId, access) <> (User2Course.tupled, User2Course.unapply)
  }

  class Course2QuizTable(tag: Tag) extends Table[Course2Quiz](tag, "course_2_quiz") {
    def courseId = column[CourseId]("course_id")
    def quizId = column[QuizId]("quiz_id")
    def startDate = column[Option[DateTime]]("start_date")
    def endDate = column[Option[DateTime]]("end_date")

    def pk = primaryKey("course_2_quiz_pk", (courseId, quizId))

    def quizIdFK = foreignKey("course_2_quiz_fk__user_id", quizId, quizDAO.Quizzes)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def courseIdFK = foreignKey("course_2_quiz_fk__course_id", courseId, Courses)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

    def * = (courseId, quizId, startDate, endDate) <> (Course2Quiz.tupled, Course2Quiz.unapply)
  }

}

