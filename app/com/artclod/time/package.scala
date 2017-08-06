package com.artclod

import org.joda.time.DateTime

import scala.language.implicitConversions

package object time {

  implicit def java2JodaDate(javaDate: java.util.Date) : DateTime = if(javaDate == null){null}else{new DateTime(javaDate)}

  implicit def java2JodaOptionDate(javaDate: Option[java.util.Date]) : Option[DateTime] = javaDate.map(java2JodaDate(_))

}
