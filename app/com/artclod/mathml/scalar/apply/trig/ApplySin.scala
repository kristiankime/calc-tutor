package com.artclod.mathml.scalar.apply.trig

import com.artclod.mathml.scalar._
import com.artclod.mathml.scalar.concept._

import scala.util._

case class ApplySin(value: MathMLElem) extends UnaryFunction(value, Sin) with OneMathMLChild {

	override def eval(b: Map[String, Double]) = Try(Trigonometry.sin(v.eval(b).get))

	override def constant: Option[Constant] = v.c match {
		case Some(v) => Some(Trigonometry.sin(v))
		case _ => None
	}

	def simplifyStep() = ApplySin(v.s)

	def derivative(x: String) = ApplyCos(v.s) * v.d(x)

	override def toMathJS: String = "sin(" + value.toMathJS + ")"

	def mathMLChild = value

	def copy(child: MathMLElem) = ApplySin(child)
}