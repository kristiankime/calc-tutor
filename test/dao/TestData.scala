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

  def data[T](awaitable: Awaitable[T]) = Await.result(awaitable, Duration(5, TimeUnit.SECONDS))

  def user(id: Int) = User(id = null, loginId = "loginId" + id , name = "name" + id, email = None, lastAccess = JodaUTC(0))

  def organization(id: Int) = Organization(name= "name" + id, creationDate=JodaUTC(0), updateDate=JodaUTC(0))

  def course(id: Int, organization : Organization, owner : User) = Course(name="name"+id, organizationId=organization.id, ownerId=owner.id, editCode = "", viewCode = None, creationDate = JodaUTC(0), updateDate = JodaUTC(0))

}
