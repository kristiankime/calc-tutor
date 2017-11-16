package dao.organization.table

import javax.inject.{Inject, Singleton}

import com.artclod.slick.JodaUTC
import dao.ColumnTypeMappings
import dao.user.UserDAO
import models._
import models.organization.Organization
import org.joda.time.DateTime
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.mvc.Result
import play.api.mvc.Results._
import slick.lifted

import scala.concurrent.{ExecutionContext, Future}

// ====
import slick.driver.JdbcProfile
//import slick.jdbc.JdbcProfile // Use this after upgrading slick
// ====

@Singleton
class OrganizationTables @Inject()(protected val dbConfigProvider: DatabaseConfigProvider, protected val userDAO: UserDAO)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] with ColumnTypeMappings {
  // ====
  //  import profile.api._ // Use this after upgrading slick
  import dbConfig.driver.api._

  // * ====== TABLE INSTANCES ====== *
  val Organizations = lifted.TableQuery[OrganizationTable]

  // * ====== TABLE CLASSES ====== *
  class OrganizationTable(tag: Tag) extends Table[Organization](tag, "organization") {
    def id = column[OrganizationId]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def creationDate = column[DateTime]("creation_date")
    def updateDate = column[DateTime]("update_date")

    def * = (id, name, creationDate, updateDate) <> (Organization.tupled, Organization.unapply)
  }
}

