package controllers.support

import controllers.auth.AccessAuthorizer
import models.{Access, AccessibleId}
import play.api.Logger

import scala.concurrent.Future
import play.api.mvc._

case class RequireAccess[A](access: Access, to: AccessibleId)(action: Action[A]) extends Action[A] {

  lazy val parser: BodyParser[A] = action.parser

  def apply(request: Request[A]): Future[Result] = {
    val newRequest = new WrappedRequest[A](request) {
      override def headers = request.headers.add((AccessAuthorizer.ITEM_ID, to.toString), (AccessAuthorizer.ACCESS_LEVEL, access.v.toString))
    }
    action(newRequest)
  }

}