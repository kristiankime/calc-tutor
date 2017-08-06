package com.artclod.math

import com.google.common.base.Strings
import org.apache.commons.lang3.math.NumberUtils

import scala.util.Try

private object IntervalSupport {
  val fmt = new java.text.DecimalFormat("#.##");

  private val num = "[-+]?[0-9]*\\.?[0-9]+"
  private val numOrInf = (DoubleParse.allInfForRegex + num).mkString("(?:", "|", ")")
  private val w = "[\\s]*"
  private val numGroup = w + "(" + numOrInf + ")" + w
  private val full = ("^\\(" + numGroup + "," + numGroup + "\\)$")
  val reg = full.r
}

object Interval {
  def apply(lower: Int, upper: Int) : Interval = Interval(lower.toDouble, upper.toDouble)

  def apply(str: String) : Option[Interval] = {
    str.trim.toLowerCase match {
      case IntervalSupport.reg(lower, upper) => {
        val loTry = DoubleParse(lower).toOption
        val upTry = DoubleParse(upper).toOption
        loTry.flatMap(lo => upTry.map(up => Interval(lo, up)))
      }
      case _ => None
    }
  }

  def overlap(intervals: Vector[Interval]) : Boolean =
   if(intervals.isEmpty || intervals.size == 1) {
     false
   } else {
     val sorted = intervals.sortBy(_.lower)
     (for( pair <- sorted.tail.zip(sorted.init) ) yield {
       pair._1.overlap(pair._2)
     }).reduce(_ || _)
  }

}

/**
  * Represent and (open) interval between two numbers (represented as Doubles).
  * The lower number must be strictly < the upper number so the empty interval (lower == upper) is not allowed.
  *
  * @param lower The small number in the interval
  * @param upper The larger number in the interval
  */
case class Interval(lower: Double, upper: Double) {
  if(lower >= upper){ throw new IllegalArgumentException("Lower cannot be >= upper [" + lower + ", " + upper + "]")}
  val l = lower
  val u = upper

  override def toString  = "(" + IntervalSupport.fmt.format(lower) + "," + IntervalSupport.fmt.format(upper) + ")"

  private def lowerBetween(i: Interval) = l >= i.l && l < i.u

  private def upperBetween(i: Interval) = u >  i.l && u <= i.u

  def overlap(o: Interval) = lowerBetween(o) || upperBetween(o) || o.lowerBetween(this) || o.lowerBetween(this)
}