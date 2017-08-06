package com.artclod.mathml

import com.artclod.mathml.scalar.MathMLElem

import scala.util.{Success, Failure, Try}

object MathMLRange {

  def percentInRange(variableName: String, eq1: MathMLElem, min: Double, max: Double) : Double = percentInRange(variableName, eq1, min, max, MathMLEq.valsTight)

  def percentInRange(variable: String, eq1: MathMLElem, min: Double, max: Double, vals: Seq[Double]): Double = {
    val inRangeCount = vals.map(v => inRange(eq1.evalT((variable -> v)), min, max))
    inRangeCount.sum / inRangeCount.size.toDouble
  }

  private def inRange(valueTry: Try[Double], min: Double, max: Double) : Double =
    valueTry match {
      case Failure(_) => 1d
      case Success(value) => if(!value.isNaN && !value.isInfinite && value >= min && value <= max) { 1d } else { 0d }
    }

}
