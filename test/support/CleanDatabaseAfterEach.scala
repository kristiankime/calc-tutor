package support

import dao.TestData
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatestplus.play.PlaySpec
import play.api.Application
import play.api.db.evolutions.Evolutions
import play.api.inject.guice.GuiceApplicationBuilder

// https://stackoverflow.com/questions/33392905/how-to-apply-manually-evolutions-in-tests-with-slick-and-play-2-4#33399278
trait CleanDatabaseAfterEach extends PlaySpec with BeforeAndAfterEach with BeforeAndAfterAll {

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

}
