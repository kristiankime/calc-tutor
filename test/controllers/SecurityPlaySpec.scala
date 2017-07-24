package controllers

import java.nio.charset.StandardCharsets
import java.util.Base64

import play.api.Application
import play.api.mvc.{AnyContent, Cookie}
import play.api.test.Helpers.{AUTHORIZATION, GET, cookies, route}
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.concurrent.TimeUnit

import akka.util.Timeout
import org.scalatestplus.play.{BaseOneAppPerSuite, FakeApplicationFactory, PlaySpec}
import fakes.FakeCache
import play.api.cache.CacheApi
import play.api.http.Writeable
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Cookie
import play.api.test.{FakeRequest, Writeables}
import play.api.{Application, Configuration, Environment, Play}

trait SecurityPlaySpec extends PlaySpec with Writeables /*with BaseOneAppPerSuite with FakeApplicationFactory*/ {


  //  val configuration = Configuration.load(Environment.simple())
  //
  ////  override def fakeApplication(): Application = new GuiceApplicationBuilder().overrides(bind[CacheApi].to[FakeCache]).in(Environment.simple()).build()
  //
  //  implicit val app : Application =
  //    new GuiceApplicationBuilder()
  //      .loadConfig(configuration)
  //      .overrides(bind[CacheApi].to[FakeCache])
  //      .in(Environment.simple()).build()
  //
  //  Play.start(app)


  //  val configuration = Configuration.load(Environment.simple())
//  override def fakeApplication(): Application = new GuiceApplicationBuilder().loadConfig(configuration).overrides(bind[CacheApi].to[FakeCache]).in(Environment.simple()).build()
//  implicit val app : Application = new GuiceApplicationBuilder().loadConfig(configuration).overrides(bind[CacheApi].to[FakeCache]).in(Environment.simple()).build()

//  Play.start(app)

  def authenticate(user: String, pass: String)(implicit app: Application): Cookie = {
    val request = FakeRequest(GET, "/callback?client_name=IndirectBasicAuthClient")
    val headers = request.withHeaders("Host" -> "localhost").withHeaders(getAuthHeader(user, pass))
    val resp = route(app, headers)(writeableOf_AnyContentAsEmpty).get
    val c = cookies(resp)(Timeout.apply(1000, TimeUnit.MINUTES)).get("PLAY_SESSION").get
    c
  }

  def getAuthHeader(user: String, pass: String) = {
    val encoding = new String(Base64.getEncoder.encode(s"$user:$pass".getBytes()), StandardCharsets.UTF_8)
    AUTHORIZATION -> s"Basic $encoding"
  }

}
