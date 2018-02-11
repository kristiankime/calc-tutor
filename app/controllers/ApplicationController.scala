package controllers


import java.util.concurrent.TimeUnit
import javax.inject._

import org.pac4j.core.config.Config
import org.pac4j.core.profile.{CommonProfile, ProfileManager}
import org.pac4j.play.scala.Security
import org.pac4j.play.store.PlaySessionStore
import play.api._
import play.api.mvc._
import play.libs.concurrent.HttpExecutionContext
import javax.inject._
import javax.inject._
import play.api.routing._

import _root_.controllers.support.RequireAccess
import _root_.controllers.support.Consented
import dao.user.UserDAO
import models.{Non, OrganizationId}
import org.pac4j.play.PlayWebContext


@Singleton
class ApplicationController @Inject()(val config: Config, val playSessionStore: PlaySessionStore, override val ec: HttpExecutionContext, userDAO: UserDAO) extends Controller with Security[CommonProfile]  {

  def index = Action { implicit request =>
    Ok(views.html.index())
  }

  def secure = RequireAccess(Non, to=OrganizationId(0)) { Secure("RedirectUnauthenticatedClient", "Access") { profiles => Consented(profiles, userDAO) { implicit user => Action { implicit request =>
    val webContext = new PlayWebContext(request, playSessionStore)
    val profileManager = new ProfileManager[CommonProfile](webContext)
    val profile = profileManager.get(true)

    Ok(views.html.secure())
  } } } }

  def javascriptRoutes = Action { implicit request =>
    Ok(
      JavaScriptReverseRouter("jsRoutes")(
        _root_.controllers.library.routes.javascript.LibraryController.viewQuestion
      )
    ).as("text/javascript")
  }

}