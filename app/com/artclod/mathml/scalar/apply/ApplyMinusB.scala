package com.artclod.mathml.scalar.apply

import com.artclod.mathml._
import com.artclod.mathml.scalar._
import com.artclod.mathml.scalar.concept.Constant

import scala.util._

/**
 * ApplyMinus for the Binary case
 */
case class ApplyMinusB(val value1: MathMLElem, val value2: MathMLElem)
	extends MathMLElem(MathML.h.prefix, "apply", MathML.h.attributes, MathML.h.scope, false, (Seq[MathMLElem](Minus) ++ value1 ++ value2): _*) {

	def eval(boundVariables: Map[String, Double]) = Try(value1.eval(boundVariables).get - value2.eval(boundVariables).get)

	def constant: Option[Constant] = (value1.c, value2.c) match {
		case (Some(v1), Some(v2)) => Some(v1 - v2)
		case _ => None
	}

	def simplifyStep() =
		if (value2.isZero) value1.s
		else if (value1.isZero) -(value2.s)
		else (value1.s) - (value2.s)
		
	def variables: Set[String] = value1.variables ++ value2.variables

	def derivative(x: String): MathMLElem = (value1.d(x)) - (value2.d(x))

	override def toMathJS: String = "(" + value1.toMathJS + " - " + value2.toMathJS + ")"
}