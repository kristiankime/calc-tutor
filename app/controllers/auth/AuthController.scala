package controllers.auth

import java.util.UUID
import javax.inject._

import play.api._
import play.api.mvc._
import org.pac4j.core.client.{Clients, IndirectClient}
import play.api.mvc._
import org.pac4j.core.profile._
import org.pac4j.play.PlayWebContext
import org.pac4j.play.scala._
import org.pac4j.core.credentials.Credentials
import javax.inject.Inject

import dao.auth.LoginDAO
import play.libs.concurrent.HttpExecutionContext
import org.pac4j.core.config.Config
import org.pac4j.core.context.Pac4jConstants
import org.pac4j.play.store.PlaySessionStore
import org.pac4j.play.scala.Security
import views.html.helper.CSRF

import scala.collection.JavaConversions._
import org.pac4j.http.client.indirect.FormClient
import org.pac4j.sql.profile.DbProfile
import org.pac4j.sql.profile.service.DbProfileService
import play.api.data._
import play.api.data.Forms._

import scala.concurrent.ExecutionContext

/**
 * This controller handles actions specifically related to Authentication and Authorization
 */
@Singleton
class AuthController @Inject()(val config: Config, val playSessionStore: PlaySessionStore, val dbProfileService: DbProfileService, val loginDAO: LoginDAO, override val ec: HttpExecutionContext)(implicit val executionContext: ExecutionContext) extends Controller with Security[CommonProfile] {

  def signIn = Action { implicit request =>
    Ok(views.html.auth.signIn.render())
  }

  def signUp = Action { implicit request =>
    Ok(views.html.auth.signUp.render(UserData.form, request))
  }

  def createLogin = Action { implicit request =>
    UserData.form.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.auth.signUp(formWithErrors, request))
      },
      userData => {
        val profile = new DbProfile()
        profile.setId(UUID.randomUUID())
        profile.addAttribute(Pac4jConstants.USERNAME, userData.name)
        dbProfileService.create(profile, userData.password)
        Redirect(routes.AuthController.signIn())
      }
    )

    Ok(views.html.index.render())
  }

  def viewLoginDb = Action.async { implicit request =>
    loginDAO.all().map { logins =>
      Ok(views.html.auth.viewDB.render(logins))
    }
  }

  def formClient = Secure("FormClient") { profiles =>
    Action { implicit request =>
      Redirect(controllers.routes.ApplicationController.secure)
    }
  }

  def googleClient = Secure("OidcClient") { profiles =>
    Action { implicit request =>
      Redirect(controllers.routes.ApplicationController.secure)
    }
  }
    
  def loginForm = Action { implicit request =>
    val formClient = config.getClients.findClient("FormClient").asInstanceOf[FormClient]
    Ok(views.html.auth.loginForm(formClient, request))
  }

}

object UserData {
  case class Values(name: String, password: String)

  val NAME = "name"
  val PASSWORD = "password"

  val form = Form(
    mapping(
      NAME -> nonEmptyText,
      PASSWORD -> nonEmptyText
    )(Values.apply)(Values.unapply)
  )
}


