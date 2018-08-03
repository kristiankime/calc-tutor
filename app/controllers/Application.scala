package controllers

import java.util.concurrent.TimeUnit

object Application {
  val version = Version(0, 0, 0)

  val appTimeoutNum = 300 // TODO change for production
  val appTimeoutUnit = TimeUnit.SECONDS
  val appTimeout = scala.concurrent.duration.Duration(Application.appTimeoutNum, Application.appTimeoutUnit)

//  val defaultSecurityClients = "RedirectUnauthenticatedClient"
  val defaultSecurityClients = "FormClient"
}

object Version {
  def apply(major: Int, minor: Int) : Version = Version(major, minor, None)

  def apply(major: Int, minor: Int, build: Int) : Version = Version(major, minor, Some(build))
}

case class Version(major: Int, minor: Int, build: Option[Int]){
  override def toString = "v" + major + "." + minor + (build match {
    case None => ""
    case Some(b) => "." + b
  })
}