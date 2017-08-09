package models.user

import models.UserId
import org.joda.time.DateTime

case class User(id: UserId, loginId: String, name: String, email: Option[String], consented: Boolean = true, allowAutoMatch: Boolean = true, seenHelp: Boolean = false, emailUpdates: Boolean = true, lastAccess : DateTime)
//case class User(id: UserId, loginId: String, name: String, email: Option[String])