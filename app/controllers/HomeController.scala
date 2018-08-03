package controllers

import javax.inject._
import _root_.controllers.support.{Consented, RequireAccess}
import dao.organization.CourseDAO
import dao.quiz.SkillDAO
import dao.user.UserDAO
import models.user.User
import models.{Non, OrganizationId}
import org.pac4j.core.config.Config
import org.pac4j.core.profile.{CommonProfile, ProfileManager}
import org.pac4j.play.PlayWebContext
import org.pac4j.play.scala.{Security, SecurityComponents}
import org.pac4j.play.store.PlaySessionStore
import play.api.mvc._
import play.libs.concurrent.HttpExecutionContext

import scala.concurrent.ExecutionContext


@Singleton
class HomeController @Inject()(/*val config: Config, val playSessionStore: PlaySessionStore, override val ec: HttpExecutionContext,*/ val controllerComponents: SecurityComponents, userDAO: UserDAO, courseDAO: CourseDAO, skillDAO: SkillDAO)(implicit executionContext: ExecutionContext) extends BaseController with Security[CommonProfile]  {

  def index = Action { implicit request =>
    Ok(views.html.index())
  }

  def home = Secure(Application.defaultSecurityClients, "Access").async { authenticatedRequest => Consented(authenticatedRequest, userDAO) { implicit user => Action.async { implicit request =>
        courseDAO.coursesAndAccessFor(user).map(courses => Ok(views.html.home(courses)) )
  } } }

  def userInfo = Secure(Application.defaultSecurityClients, "Access").async { authenticatedRequest => Consented(authenticatedRequest, userDAO) { implicit user => Action.async { implicit request =>
      val studentIdsFuture = userDAO.studentIds()
      val skillsFuture = studentIdsFuture.flatMap(ids => skillDAO.skillsLevelFor(user.id, ids))
      val coursesAndAccessFuture = courseDAO.coursesAndAccessFor(user)
      coursesAndAccessFuture.flatMap(courses => skillsFuture.map(skills =>
        Ok(views.html.user.userInfo(courses, skills._2, skills._1))
      ))
  } } }

}