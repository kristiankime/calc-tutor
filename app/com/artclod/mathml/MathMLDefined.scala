package com.artclod.mathml

import com.artclod.mathml.scalar.MathMLElem
import com.artclod.slick.NumericBoolean
import scala.util.Failure
import scala.util.Success

object MathMLDefined {

  def isDefinedFor(function: MathMLElem, threshold: Double, variableName: String = "x") : Boolean = {
    val sum = MathMLEq.vals.map(v => isDefinedAt(function, (variableName, v))).map(b => NumericBoolean.asDouble(b)).sum
    val percent = sum / MathMLEq.vals.size.toDouble
    percent >= threshold
  }

  def isDefinedAt(function: MathMLElem, boundVariables: (String, Double)*) : Boolean =
    function.evalT(boundVariables: _*) match {
      case Failure(_) => false
      case Success(v) =>
        if (v.isNaN || v.isInfinite) { false }
        else { true }
    }

}
