package dao.organization

import javax.inject.Inject

import dao.ColumnTypeMappings
import models.OrganizationId
import models.organization.Organization
import models.user.User
import org.joda.time.DateTime
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.lifted

import scala.concurrent.{ExecutionContext, Future}

// ====
import slick.driver.JdbcProfile
//import slick.jdbc.JdbcProfile // Use this after upgrading slick
// ====

class OrganizationDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] with ColumnTypeMappings {
  // ====
  //  import profile.api._ // Use this after upgrading slick
  import dbConfig.driver.api._
  // ====

  val Organizations = lifted.TableQuery[OrganizationTable]

  def all(): Future[Seq[Organization]] = db.run(Organizations.result)

//  def insert(cat: Organization): Future[Unit] = db.run(Organizations += cat).map { _ => () }

  def insert(organization: Organization): Future[Organization] = db.run(
    (Organizations returning Organizations.map(_.id) into ((needsId, id) => needsId.copy(id = id))) += organization
  )

  def byId(id : OrganizationId): Future[Option[Organization]] = db.run(Organizations.filter(_.id === id).result.headOption)

  class OrganizationTable(tag: Tag) extends Table[Organization](tag, "organization") {
    def id = column[OrganizationId]("id", O.PrimaryKey)
    def name = column[String]("name")
    def creationDate = column[DateTime]("creation_date")
    def updateDate = column[DateTime]("update_date")

    def * = (id, name, creationDate, updateDate) <> (Organization.tupled, Organization.unapply)
  }
}

