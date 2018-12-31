package com.artclod.mathml.scalar.apply.trig

import com.artclod.mathml.scalar._
import com.artclod.mathml.scalar.concept._

import scala.util._

case class ApplyCot(value: MathMLElem) extends UnaryFunction(value, Cot) with OneMathMLChild {

	override def eval(b: Map[String, Double]) = Try(Trigonometry.cot(v.eval(b).get))

	override def constant: Option[Constant] = v.c match {
		case Some(v) => Some(Trigonometry.cot(v))
		case _ => None
	}

	def simplifyStep() = ApplyCot(v.s)

	def derivative(x: String) = -(ApplyCsc(v) ^ 2) * v.d(x)

	override def toMathJS: String = "cot(" + value.toMathJS + ")"

	def mathMLChild = value

	def copy(child: MathMLElem) = ApplyCot(child)
}