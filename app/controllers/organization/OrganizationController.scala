package controllers.organization

import javax.inject._

import _root_.controllers.support.{Consented, RequireAccess}
import dao.organization.OrganizationDAO
import dao.user.UserDAO
import models.organization.Organization
import models.{Non, OrganizationId}
import org.pac4j.core.config.Config
import org.pac4j.core.profile.{CommonProfile, ProfileManager}
import org.pac4j.play.PlayWebContext
import org.pac4j.play.scala.Security
import org.pac4j.play.store.PlaySessionStore
import play.api.mvc._
import play.libs.concurrent.HttpExecutionContext

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class OrganizationController @Inject()(val config: Config, val playSessionStore: PlaySessionStore, override val ec: HttpExecutionContext, userDAO: UserDAO, organizationDAO: OrganizationDAO)(implicit executionContext: ExecutionContext) extends Controller with Security[CommonProfile]  {

  def view(organizationId: OrganizationId) = Secure("RedirectUnauthenticatedClient") { profiles => Consented(profiles, userDAO) { user => Action.async { request =>
    organizationDAO(organizationId).map{ organizationEither =>
      organizationEither match {
        case Left(notFoundResult) => notFoundResult
        case Right(organization) => Ok(views.html.organization.organizationView(organization, List()))
      }
    }
  } } }

  def list() = Secure("RedirectUnauthenticatedClient") { profiles => Consented(profiles, userDAO) { user => Action.async { request =>
    organizationDAO.allEnsureAnOrg().map{ organizations => Ok(views.html.organization.organizationList(organizations)) }
  } } }

}