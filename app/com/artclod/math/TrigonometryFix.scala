package com.artclod.math

import scala.math.{Pi => π, _}

/**
 * Due to floating point precision Java's (and thus Scala's by extension) trigonometric functions
 * return approximations of the actual mathematical values the functions "should" produce.
 *
 * In particular:
 *
 * cos/sin do not output 0 when expected.
 * eg while sin(Pi/2) is 1 sin(Pi) is very small but not 0.
 * While this is understandable, and fine for some purposes, it occasionally useful to have
 * these function output an actual 0.
 *
 * tan does not output Infinity at Pi/2 etc...
 */
object TrigonometryFix {

  def cos0(x: Double) =
    sin(x) match {
      case -1d => 0d
      case 1d => 0d
      case _ => cos(x)
    }

  def sin0(x: Double) =
    cos(-x) match {
      case -1d => 0d
      case 1d => 0d
      case _ => sin(x)
    }

  def tan0(x: Double) =
    sin0(x) match {
      case -1d => Double.PositiveInfinity
      case 1d => Double.PositiveInfinity
      case 0d => 0d
      case _ => tan(x)
    }

  def cot0(x: Double) = 1d / tan0(x)

  def csc0(x: Double) = 1d / sin0(x)

  def sec0(x: Double) = 1d / cos0(x)
  
}

