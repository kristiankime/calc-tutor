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
  val version = Version(0, 0, 0)

  def index = Action { implicit request =>
    Ok(views.html.index())
  }

  def secure = Secure("RedirectUnauthenticatedClient") { profiles =>
    Action { request =>
      Ok(views.html.secure())
    }
  }

}

object Version {
  def apply(major: Int, minor: Int) : Version = Version(major, minor, None)

  def apply(major: Int, minor: Int, build: Int) : Version = Version(major, minor, Some(build))
}

case class Version(major: Int, minor: Int, build: Option[Int]){
  override def toString = "v" + major + "." + minor + (build match {
    case None => ""
    case Some(b) => "." + b
  })
}