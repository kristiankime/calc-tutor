package controllers


import java.util.concurrent.TimeUnit

import javax.inject._
import org.pac4j.core.config.Config
import org.pac4j.core.profile.{CommonProfile, ProfileManager}
import org.pac4j.play.scala.{Security, SecurityComponents}
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
class ApplicationController @Inject()(/*override val config: Config, override val playSessionStore: PlaySessionStore, override val ec: HttpExecutionContext,*/ val controllerComponents: SecurityComponents, userDAO: UserDAO) extends BaseController with Security[CommonProfile]  {

  def index = Action { implicit request =>
    Ok(views.html.index())
  }

//  def secure = RequireAccess(Non, to=OrganizationId(0)) { Secure("RedirectUnauthenticatedClient", "Access").async{ authenticatedRequest => Consented(authenticatedRequest, userDAO) { implicit user => Action { implicit request =>
//    val webContext = new PlayWebContext(request, playSessionStore)
//    val profileManager = new ProfileManager[CommonProfile](webContext)
//    val profile = profileManager.get(true)
//
//    Ok(views.html.secure())
//  } } } }

  def javascriptRoutes = Action { implicit request =>
    Ok(
      JavaScriptReverseRouter("jsRoutes")(
        _root_.controllers.library.routes.javascript.LibraryController.viewQuestion,
        _root_.controllers.library.routes.javascript.LibraryController.questionListAjax,
        _root_.controllers.quiz.routes.javascript.QuizController.attachAjax,
        _root_.controllers.quiz.routes.javascript.QuestionController.view,
        _root_.controllers.quiz.routes.javascript.QuestionController.removeAjax,
        _root_.controllers.organization.routes.javascript.CourseController.studentSelfQuestion
      )
    ).as("text/javascript")
  }

}