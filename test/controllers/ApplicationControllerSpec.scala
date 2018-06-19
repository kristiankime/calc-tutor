package controllers

import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers.{contentAsString, _}
import java.nio.charset.StandardCharsets
import java.util.Base64

import org.scalatestplus.play.guice.GuiceOneAppPerTest

import play.api.cache.CacheApi
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Cookie
import play.api.test.FakeRequest

import scala.concurrent.Future

import org.scalatestplus.play._

import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._

import play.api.test.Helpers.{ GET => GET_REQUEST, _ }

import org.scalatest._
import org.scalatestplus.play._

import play.api.test._
import play.api.test.Helpers.{ GET => GET_REQUEST, _ }
// #scalafunctionaltest-imports

import play.api.mvc._

import play.api.test.Helpers.{ GET => GET_REQUEST, _ }
import play.api.test.Helpers.GET
import play.api.libs.ws._
import play.api.inject.guice._
import play.api.routing._
import play.api.routing.sird._


/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 *
 * For more information, see https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
 */
class ApplicationControllerSpec extends SecurityPlaySpec with GuiceOneAppPerTest {

  "ApplicationController GET" should {

    "render the index page from the application" in {
      val controller = app.injector.instanceOf[ApplicationController]
      val home = controller.index().apply(FakeRequest())

      status(home) mustBe OK
      contentType(home) mustBe Some("text/html")
      contentAsString(home) must include ("Index")
    }

    "render the index page from the router" in {
      // Need to specify Host header to get through AllowedHostsFilter
      val request = FakeRequest(GET, "/").withHeaders("Host" -> "localhost")
      val home = route(app, request).get

      status(home) mustBe OK
      contentType(home) mustBe Some("text/html")
      contentAsString(home) must include ("Index")
    }

    "render the secure page from the router when authorized" in {
      // TODO there is no more secure page test with another page or add back?
      val page = route(app, FakeRequest(GET, "/secure").withHeaders("Host" -> "localhost").withCookies(authenticate("john", "john"))).get

      status(page) mustBe OK
      contentType(page) mustBe Some("text/html")
      contentAsString(page) must include ("Secure")
    }

    "fail to render the secure page from the router when not authorized" in {
      // TODO there is no more secure page test with another page or add back?
      val page = route(app, FakeRequest(GET, "/secure").withHeaders("Host" -> "localhost")).get

      status(page) mustBe SEE_OTHER
    }
  }


}
