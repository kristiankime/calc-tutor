package com.artclod.mathml.scalar.apply.trig

import com.artclod.mathml.scalar._
import com.artclod.mathml.scalar.concept._

import scala.util._

case class ApplyCos(value: MathMLElem) extends UnaryFunction(value, Cos) {

	override def eval(b: Map[String, Double]) = Try(Trigonometry.cos(v.eval(b).get))

	override def constant: Option[Constant] = v.c match {
		case Some(v) => Some(Trigonometry.cos(v))
		case _ => None
	}

	def simplifyStep() = ApplyCos(v.s)

	def derivative(x: String) = -ApplySin(v.s) * v.d(x)

	override def toMathJS: String = "cos(" + value.toMathJS + ")"

}