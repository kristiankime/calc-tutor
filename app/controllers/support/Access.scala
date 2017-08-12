package controllers.support

import controllers.auth.AccessAuthorizer
import models.{Access, AccessibleId}
import play.api.Logger

import scala.concurrent.Future
import play.api.mvc._

case class AddAccess[A](item: AccessibleId, level: Access)(action: Action[A]) extends Action[A] {

  lazy val parser = action.parser

  def apply(request: Request[A]): Future[Result] = {
    val newRequest = new WrappedRequest[A](request) {
      override def headers = request.headers.add((AccessAuthorizer.ITEM_ID, item.toString), (AccessAuthorizer.ACCESS_LEVEL, level.v.toString))
    }
    action(newRequest)
  }

}