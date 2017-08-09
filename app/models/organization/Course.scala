package models.organization

import dao.binders.HasId
import models.{CourseId, OrganizationId, Secured, UserId}
import org.joda.time.DateTime

case class Course(id: CourseId, name: String, organizationId: OrganizationId, ownerId: UserId, editCode: String, viewCode: Option[String], creationDate: DateTime, updateDate: DateTime) extends Secured with HasId[CourseId]