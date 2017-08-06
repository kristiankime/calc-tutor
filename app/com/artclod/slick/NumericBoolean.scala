package com.artclod.slick

//import play.api.db.slick.Config.driver.simple._

/**
 * Most of the aggregation functions in the DB do not work on db booleans.
 * It can be useful to have a boolean treated as a number in the db so max and min for example
 * can be used to determine if there are any of the values are true or false.
 */
object NumericBoolean {
  val T : Short = 1
  val F : Short = 0

  def apply(s: Short) = s match {
    case 0 => false
    case 1 => true
    case _ => throw new IllegalStateException("Converting short to correct value was [" + s + "] must be in { 0 -> false, 1 -> true }, coding error")
  }

  def apply(b: Boolean) : Short = if(b) 1 else 0

  def asDouble(b: Boolean) : Double = if(b) 1d else 0d

//  implicit def boolean2DBNumber = MappedColumnType.base[Boolean, Short](
//    bool => if(bool) 1 else 0,
//    dbShort => if(dbShort == 1){ true } else if(dbShort == 0) { false } else { throw new IllegalStateException("DB returned [" + dbShort + "] for boolean, must be either 0 or 1")}
//  )

}
