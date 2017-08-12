package models.user

import models.UserId
import org.joda.time.DateTime

case class User(id: UserId = null, loginId: String, name: String, email: Option[String], consented: Boolean = false, allowAutoMatch: Boolean = false, seenHelp: Boolean = false, emailUpdates: Boolean = false, lastAccess : DateTime)
