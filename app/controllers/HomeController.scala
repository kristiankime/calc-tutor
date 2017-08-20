package controllers

import javax.inject._

import _root_.controllers.support.{RequireAccess, Consented}
import dao.user.UserDAO
import models.{Non, OrganizationId}
import org.pac4j.core.config.Config
import org.pac4j.core.profile.{CommonProfile, ProfileManager}
import org.pac4j.play.PlayWebContext
import org.pac4j.play.scala.Security
import org.pac4j.play.store.PlaySessionStore
import play.api.mvc._
import play.libs.concurrent.HttpExecutionContext


@Singleton
class HomeController @Inject()(val config: Config, val playSessionStore: PlaySessionStore, override val ec: HttpExecutionContext, userDAO: UserDAO) extends Controller with Security[CommonProfile]  {

  def index = Action { implicit request =>
    Ok(views.html.index())
  }

  def home = RequireAccess(Non, to=OrganizationId(0)) { Secure("RedirectUnauthenticatedClient", "Access") { profiles => Consented(profiles, userDAO) { user => Action { request =>
      Ok(views.html.home(List()))
  } } } }

}