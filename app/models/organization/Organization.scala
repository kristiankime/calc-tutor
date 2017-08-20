package models.organization

import models.OrganizationId
import org.joda.time.DateTime

case class Organization(id: OrganizationId = null, name: String, creationDate: DateTime, updateDate: DateTime)