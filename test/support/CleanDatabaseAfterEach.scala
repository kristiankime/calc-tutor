package support

import dao.TestData
import dao.user.UserDAO
import dao.util.CleanupDAO
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.Application
import play.api.db.evolutions.Evolutions
import play.api.inject.guice.GuiceApplicationBuilder

// https://stackoverflow.com/questions/33392905/how-to-apply-manually-evolutions-in-tests-with-slick-and-play-2-4#33399278
trait CleanDatabaseAfterEach extends PlaySpec with BeforeAndAfterEach with BeforeAndAfterAll /*with GuiceOneAppPerTest*/ {

  lazy val app: Application = new GuiceApplicationBuilder().build()
  lazy val injector = app.injector
  lazy val databaseApi = injector.instanceOf[play.api.db.DBApi]

  override def beforeEach() = {
    Evolutions.applyEvolutions(databaseApi.database("default"))
  }

  override def afterEach() = {
    Evolutions.cleanupEvolutions(databaseApi.database("default"))
  }

  override def afterAll = {
    TestData.await(app.stop())
  }

//  override def beforeEach() {
//    val cleanupDAO = app.injector.instanceOf1[CleanupDAO]
//
//    val userDAO = app.injector.instanceOf1[UserDAO]
//
//    val beforeUsers = TestData.await(userDAO.all())
//
//    TestData.await(cleanupDAO.clear())
//
//    val afterUsers = TestData.await(userDAO.all())
//  }

//  override def afterEach() {
//    val cleanupDAO = app.injector.instanceOf1[CleanupDAO]
//
//    val userDAO = app.injector.instanceOf1[UserDAO]
//
//    val beforeUsers = TestData.await(userDAO.all())
//
//    TestData.await(cleanupDAO.clear())
//
//    val afterUsers = TestData.await(userDAO.all())
//  }

}
