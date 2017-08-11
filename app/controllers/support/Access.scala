package controllers.support

import play.api.Logger

import scala.concurrent.Future
import play.api.mvc._

case class AddAccess[A](item: String, level: String)(action: Action[A]) extends Action[A] {

  def apply(request: Request[A]): Future[Result] = {
    val newRequest = new WrappedRequest[A](request) {
      override def headers = request.headers.add(("ITEM", item), ("LEVEL", level))
    }
    action(newRequest)
  }

  lazy val parser = action.parser
}