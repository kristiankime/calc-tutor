package controllers.organization

import javax.inject._

import _root_.controllers.support.{Consented, RequireAccess}
import dao.organization.{CourseDAO, OrganizationDAO}
import dao.user.UserDAO
import models.{CourseId, View, OrganizationId}
import org.pac4j.core.config.Config
import org.pac4j.core.profile.CommonProfile
import org.pac4j.play.scala.Security
import org.pac4j.play.store.PlaySessionStore
import play.api.mvc._
import play.libs.concurrent.HttpExecutionContext
import com.artclod.util._

import scala.concurrent.ExecutionContext


@Singleton
class CourseController @Inject()(val config: Config, val playSessionStore: PlaySessionStore, override val ec: HttpExecutionContext, userDAO: UserDAO, organizationDAO: OrganizationDAO, courseDAO: CourseDAO)(implicit executionContext: ExecutionContext) extends Controller with Security[CommonProfile]  {

  def list(organizationId: OrganizationId) = RequireAccess(View, to=organizationId) { Secure("RedirectUnauthenticatedClient", "Access") { profiles => Consented(profiles, userDAO) { user => Action.async { request =>

    (organizationDAO(organizationId) +^ courseDAO.coursesFor(organizationId)).map{ dataEither =>
      dataEither match {
        case Left(notFoundResult) => notFoundResult
        case Right((organization, courses)) => Ok(views.html.organization.courseList(organization, courses))
      }
    }

  } } } }

//  def view(courseId: CourseId) = Secure("RedirectUnauthenticatedClient") { profiles => Consented(profiles, userDAO) { user => Action.async { request =>
//    courseDAO(courseId).map{ organizationEither =>
//      organizationEither match {
//        case Left(notFoundResult) => notFoundResult
//        case Right(course) => Ok(views.html.organization.courseView(course, List()))
//      }
//    }
//  } } }
//

}