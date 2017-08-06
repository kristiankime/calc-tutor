package com.artclod.mathml.scalar.apply.trig

import com.artclod.mathml.scalar._
import com.artclod.mathml.scalar.concept._

import scala.util._

case class ApplyCsc(value: MathMLElem) extends UnaryFunction(value, Csc) {

	override def eval(b: Map[String, Double]) = Try(Trigonometry.csc(v.eval(b).get))

	override def constant: Option[Constant] = v.c match {
		case Some(v) => Some(Trigonometry.csc(v))
		case _ => None
	}

	def simplifyStep() = ApplyCsc(v.s)

	def derivative(x: String) = -ApplyCot(v.s) * ApplyCsc(v.s) * v.d(x)

	override def toMathJS: String = "csc(" + value.toMathJS + ")"
}