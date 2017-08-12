package controllers

import java.util.concurrent.TimeUnit

object Application {
  val version = Version(0, 0, 0)

  val appTimeoutNum = 30
  val appTimeoutUnit = TimeUnit.SECONDS
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