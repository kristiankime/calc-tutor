package com.artclod.slick

import org.joda.time.{DateTime, DateTimeZone, Duration}
//import play.api.db.slick.Config.driver.simple._

object JodaUTC {

//  implicit def long2Duration = MappedColumnType.base[Duration, Long](
//    duration => duration.getMillis,
//    long => Duration.millis(long))
//
//  implicit def timestamp2DateTime = MappedColumnType.base[DateTime, java.sql.Timestamp](
//    dateTime => if(dateTime == null) { null } else { new java.sql.Timestamp(dateTime.getMillis()) },
//    date => if(date == null) { null } else { new DateTime(date, DateTimeZone.UTC) } )

  def now = DateTime.now(DateTimeZone.UTC)

  def apply(l: Long) = new DateTime(l, DateTimeZone.UTC)

  val zero = new DateTime(0L, DateTimeZone.UTC)
}
