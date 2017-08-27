package dao.organization

import javax.inject.Inject
import javax.inject.Singleton

import com.artclod.slick.JodaUTC
import dao.ColumnTypeMappings
import dao.user.UserDAO
import models._
import models.organization.{Course, Organization}
import models.user.User
import org.joda.time.DateTime
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.mvc.Result
import slick.lifted
import play.api.mvc._
import play.api.mvc.Results._

import scala.concurrent.{ExecutionContext, Future}

// ====
import slick.driver.JdbcProfile
//import slick.jdbc.JdbcProfile // Use this after upgrading slick
// ====

@Singleton
class OrganizationDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider, protected val userDAO: UserDAO)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] with ColumnTypeMappings {
  // ====
  //  import profile.api._ // Use this after upgrading slick
  import dbConfig.driver.api._

  // * ====== TABLE INSTANCES ====== *
  val Organizations = lifted.TableQuery[OrganizationTable]

  // * ====== QUERIES ====== *

  // ====== FIND ======
  def all(): Future[Seq[Organization]] = db.run(Organizations.result)

  def byId(id : OrganizationId): Future[Option[Organization]] = db.run(Organizations.filter(_.id === id).result.headOption)

  def apply(organizationId: OrganizationId): Future[Either[Result, Organization]] = byId(organizationId).map { organizationOp => organizationOp match {
    case None => Left(NotFound(views.html.errors.notFoundPage("There was no organization for id=["+organizationId+"]")))
    case Some(organization) => Right(organization)
  } }

  // ====== Access ======
  def access(userId: UserId, organizationId : OrganizationId): Future[Access] = Future(Edit)  // TODO

  // ====== Create ======
  def insert(organization: Organization): Future[Organization] = db.run(
    (Organizations returning Organizations.map(_.id) into ((needsId, id) => needsId.copy(id = id))) += organization
  )

  def allEnsureAnOrg() : Future[Seq[Organization]] = all().flatMap{ organizations =>
    if(organizations.isEmpty) {
      val now = JodaUTC.now
      insert(Organization(name="Brandeis", creationDate=now, updateDate=now)).map(o => Seq(o))
    } else {
      Future.successful(organizations)
    }
  }

  // * ====== TABLE CLASSES ====== *
  class OrganizationTable(tag: Tag) extends Table[Organization](tag, "organization") {
    def id = column[OrganizationId]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def creationDate = column[DateTime]("creation_date")
    def updateDate = column[DateTime]("update_date")

    def * = (id, name, creationDate, updateDate) <> (Organization.tupled, Organization.unapply)
  }
}

