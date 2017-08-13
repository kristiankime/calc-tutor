package controllers.auth

import java.util.concurrent.TimeUnit

import controllers.Application
import dao.organization.{CourseDAO, OrganizationDAO}
import dao.user.UserDAO
import models._
import org.pac4j.core.authorization.authorizer.ProfileAuthorizer
import org.pac4j.core.context.WebContext
import org.pac4j.core.profile.CommonProfile
import play.api.Logger

import scala.concurrent.{Await, Future}

object AccessAuthorizer {
  val ITEM_ID = "ITEM_ID"
  val ACCESS_LEVEL = "ACCESS_LEVEL"
}

class AccessAuthorizer(userDAO: UserDAO, organizationDAO: OrganizationDAO, courseDAO : CourseDAO) extends ProfileAuthorizer[CommonProfile] {

  def isAuthorized(context: WebContext, profiles: java.util.List[CommonProfile]): Boolean = {
    return isAnyAuthorized(context, profiles)
  }

  import scala.concurrent.ExecutionContext.Implicits.global // TODO Make sure global usage is okay here (see http://docs.scala-lang.org/overviews/core/futures.html)

  def isProfileAuthorized(context: WebContext, profile: CommonProfile): Boolean = {
    val itemIdStr  = context.getRequestHeader(AccessAuthorizer.ITEM_ID)
    val requiredLevelStr = context.getRequestHeader(AccessAuthorizer.ACCESS_LEVEL)

    if(itemIdStr != null && requiredLevelStr != null) {
      val itemId = AccessibleId.fromStr(itemIdStr)
      val requireLevel = Access.fromNum(requiredLevelStr.toShort)

      val allowAccessFuture = userDAO.ensureByLoginId(profile).flatMap(user => authorized(user.id, itemId, requireLevel))
      Await.result(allowAccessFuture, scala.concurrent.duration.Duration(Application.appTimeoutNum, Application.appTimeoutUnit))
    } else {
      true
    }
  }

  def authorized(userId: UserId, accessibleId: AccessibleId, requiredLevel : Access) : Future[Boolean] = accessibleId match {
    case id : OrganizationId => organizationDAO.access(userId, id).map( userLevel => userLevel >= requiredLevel)
    case id : CourseId => courseDAO.access(userId, id).map( userLevel => userLevel >= requiredLevel)
    case _ => throw new RuntimeException("Did not know how to compute access level for [" + accessibleId + "]")
  }

}