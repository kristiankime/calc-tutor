package com.artclod.mathml.scalar.apply

import com.artclod.mathml._
import com.artclod.mathml.scalar._
import com.artclod.mathml.scalar.concept.Constant

import scala.util._

/**
 * ApplyMinusUnary
 */
case class ApplyMinusU(val value: MathMLElem)
	extends MathMLElem(MathML.h.prefix, "apply", MathML.h.attributes, MathML.h.scope, false, (Seq[MathMLElem](Minus) ++ value): _*) with OneMathMLChild {

	def eval(boundVariables: Map[String, Double]) = Try(-1d * value.eval(boundVariables).get)

	def constant: Option[Constant] = value.c match {
		case Some(v) => Some(v * Cn(-1))
		case _ => None
	}

	def simplifyStep() = -(value.s)

	def variables: Set[String] = value.variables

	def derivative(x: String) = -(value.d(x))

	def toMathJS: String = "-" + value.toMathJS

	def mathMLChild = value

	def copy(child: MathMLElem) = ApplyMinusU(child)
}