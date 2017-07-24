package controllers

import javax.inject._
import org.pac4j.core.config.Config
import org.pac4j.core.profile.CommonProfile
import org.pac4j.play.scala.Security
import org.pac4j.play.store.PlaySessionStore
import play.api._
import play.api.mvc._
import play.libs.concurrent.HttpExecutionContext

@Singleton
class ApplicationController @Inject()(val config: Config, val playSessionStore: PlaySessionStore, override val ec: HttpExecutionContext) extends Controller with Security[CommonProfile] {

  def index = Action { implicit request =>
    Ok(views.html.index())
  }

  def secure = Secure("RedirectUnauthenticatedClient") { profiles =>
    Action { request =>
      Ok(views.html.secure())
    }
  }

}
