package controllers.organization

import javax.inject._

import _root_.controllers.support.{Consented, RequireAccess}
import com.artclod.slick.JodaUTC
import dao.organization.{CourseDAO, OrganizationDAO}
import dao.user.UserDAO
import models.{CourseId, Edit, OrganizationId, View}
import org.pac4j.core.config.Config
import org.pac4j.core.profile.CommonProfile
import org.pac4j.play.scala.Security
import org.pac4j.play.store.PlaySessionStore
import play.api.mvc._
import play.libs.concurrent.HttpExecutionContext
import com.artclod.util._
import models.organization.Course
import play.api.data.Form
import play.api.data.Forms._
import com.artclod.random._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Random, Right}


@Singleton
class CourseController @Inject()(val config: Config, val playSessionStore: PlaySessionStore, override val ec: HttpExecutionContext, userDAO: UserDAO, organizationDAO: OrganizationDAO, courseDAO: CourseDAO)(implicit executionContext: ExecutionContext) extends Controller with Security[CommonProfile]  {
  implicit val randomEngine = new Random(JodaUTC.now.getMillis())
  val codeRange = (0 to 100000).toVector

  def list(organizationId: OrganizationId) = RequireAccess(View, to=organizationId) { Secure("RedirectUnauthenticatedClient", "Access") { profiles => Consented(profiles, userDAO) { user => Action.async { implicit request =>

    (organizationDAO(organizationId) +^ courseDAO.coursesFor(organizationId)).map{ _ match {
        case Left(notFoundResult) => notFoundResult
        case Right((organization, courses)) => Ok(views.html.organization.courseList(organization, courses))
      }
    }

  } } } }

  def createForm(organizationId: OrganizationId) = RequireAccess(Edit, to=organizationId) { Secure("RedirectUnauthenticatedClient", "Access") { profiles => Consented(profiles, userDAO) { user => Action.async { implicit request =>

    organizationDAO(organizationId).map{ _ match {
        case Left(notFoundResult) => notFoundResult
        case Right(organization) => Ok(views.html.organization.courseCreate(organization))
      }
    }

  } } } }

  def createSubmit(organizationId: OrganizationId) = RequireAccess(Edit, to=organizationId) { Secure("RedirectUnauthenticatedClient", "Access") { profiles => Consented(profiles, userDAO) { user => Action.async { implicit request =>

    organizationDAO(organizationId).flatMap{ _ match {
        case Left(notFoundResult) => Future.successful(notFoundResult)
        case Right(organization) =>
          CourseCreate.form.bindFromRequest.fold(
            errors => Future.successful(BadRequest(views.html.errors.formErrorPage(errors))),
            form => {
              val (editNum, viewNum) = codeRange.pick2From
              val now = JodaUTC.now
              val viewOp = if(form._2){ None } else { Some("CO-V-" + viewNum) }
              val courseFuture = courseDAO.insert(Course(null, form._1, organization.id, user.id, "CO-E-" + editNum, viewOp, now, now))
              courseFuture.map(course =>  Redirect(controllers.organization.routes.CourseController.view(organization.id, course.id)))
            })
      }
    }

  } } } }

  def view(organizationId: OrganizationId, courseId: CourseId) = RequireAccess(View, to=organizationId) { Secure("RedirectUnauthenticatedClient", "Access") { profiles => Consented(profiles, userDAO) { user => Action.async { implicit request =>

    (organizationDAO(organizationId) +& courseDAO(organizationId, courseId) +^ courseDAO.access(user.id, courseId)).map{ _ match {
       case Left(notFoundResult) => notFoundResult
       case Right((organization, course, accesss)) => Ok(views.html.organization.courseView(organization, course, accesss))
      }
    }

  } } } }

  def join(organizationId: OrganizationId, courseId: CourseId) = RequireAccess(View, to=organizationId) { Secure("RedirectUnauthenticatedClient", "Access") { profiles => Consented(profiles, userDAO) { user => Action.async { implicit request =>

    courseDAO(organizationId, courseId).flatMap{ _ match {
      case Left(notFoundResult) => Future.successful(notFoundResult)
      case Right(course) =>
          CourseJoin.form.bindFromRequest.fold(
              errors => Future.successful(BadRequest(views.html.errors.formErrorPage(errors))),
              form => {
                val granted = if (course.editCode == form) { courseDAO.grantAccess(user, course, Edit) }
                else if (course.viewCode == None || course.viewCode.get == form) { courseDAO.grantAccess(user, course, View) }
                else { Future.successful()}
                granted.map( na => Redirect(controllers.organization.routes.CourseController.view(organizationId, courseId)) ) // TODO indicate access was not granted in a better fashion
              })
    }

  } } } } }

}

object CourseCreate {
  val name = "name"
  val anyStudents = "any_student"
  val form = Form(tuple(name -> nonEmptyText, anyStudents -> boolean))
}

object CourseJoin {
  val code = "code"
  val form = Form(code -> text)
}