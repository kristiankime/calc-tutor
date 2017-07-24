package models.auth

case class NamePassLogin(id: String, userName: String, password: String, linkedId: Option[String], serializedprofile: Option[String])