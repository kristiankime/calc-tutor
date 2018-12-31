package com.artclod.mathml.scalar.apply.trig

import com.artclod.mathml.scalar._
import com.artclod.mathml.scalar.concept._

import scala.util._

case class ApplySec(value: MathMLElem) extends UnaryFunction(value, Sec) with OneMathMLChild {

	override def eval(b: Map[String, Double]) = Try(Trigonometry.sec(v.eval(b).get))

	override def constant: Option[Constant] = v.c match {
		case Some(v) => Some(Trigonometry.sec(v))
		case _ => None
	}

	def simplifyStep() = ApplySec(v.s)

	def derivative(x: String) = ApplyTan(v.s) * ApplySec(v.s)* v.d(x)

	override def toMathJS: String = "sec(" + value.toMathJS + ")"

	def mathMLChild = value

	def copy(child: MathMLElem) = ApplySec(child)
}