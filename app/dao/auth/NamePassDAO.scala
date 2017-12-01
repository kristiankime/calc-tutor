package dao.auth

import javax.inject.Inject
import javax.inject.Singleton

import dao.auth.table.NamePassTable
import models.auth.NamePassLogin
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.lifted

import scala.concurrent.{ExecutionContext, Future}

// ====
import slick.driver.JdbcProfile
//import slick.jdbc.JdbcProfile // Use this after upgrading slick
// ====

@Singleton
class NamePassDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider, protected val namePassTable: NamePassTable)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
  // ====
  //  import profile.api._ // Use this after upgrading slick
  import dbConfig.driver.api._
  // ====

  val Logins = namePassTable.Logins

  def all(): Future[Seq[NamePassLogin]] = db.run(Logins.result)

  def insert(cat: NamePassLogin): Future[Unit] = db.run(Logins += cat).map { _ => () }

  def byId(id : String): Future[Option[NamePassLogin]] = db.run(Logins.filter(_.id === id).result.headOption)

  def byName(name : String): Future[Option[NamePassLogin]] = db.run(Logins.filter(_.userName === name).result.headOption)

}

