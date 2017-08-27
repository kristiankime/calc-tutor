package dao

import java.util.concurrent.TimeUnit

import com.artclod.slick.JodaUTC
import models.UserId
import models.organization.{Course, Organization}
import models.user.User
import org.joda.time.DateTime

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Awaitable}

object TestData {
  val duration = Duration(5, TimeUnit.SECONDS)

  def await[A](awaitable: Awaitable[A]) = Await.result(awaitable, duration)
  def await[A, B](a1: Awaitable[A], a2: Awaitable[B]) = (Await.result(a1, duration), Await.result(a2, duration))
  def await[A, B, C](a1: Awaitable[A], a2: Awaitable[B], a3: Awaitable[C]) = (Await.result(a1, duration), Await.result(a2, duration), Await.result(a3, duration))
  def await[A, B, C, D](a1: Awaitable[A], a2: Awaitable[B], a3: Awaitable[C], a4: Awaitable[D]) = (Await.result(a1, duration), Await.result(a2, duration), Await.result(a3, duration), Await.result(a4, duration))
  def await[A, B, C, D, E](a1: Awaitable[A], a2: Awaitable[B], a3: Awaitable[C], a4: Awaitable[D], a5: Awaitable[E]) = (Await.result(a1, duration), Await.result(a2, duration), Await.result(a3, duration), Await.result(a4, duration), Await.result(a5, duration))

  def user(id: Int) = User(id = null, loginId = "loginId" + id , name = "name" + id, email = None, lastAccess = JodaUTC(0))

  def organization(id: Int) = Organization(name= "name" + id, creationDate=JodaUTC(0), updateDate=JodaUTC(0))

  def course(id: Int, organization : Organization, owner : User) = Course(name="name"+id, organizationId=organization.id, ownerId=owner.id, editCode = "", viewCode = None, creationDate = JodaUTC(0), updateDate = JodaUTC(0))

}
