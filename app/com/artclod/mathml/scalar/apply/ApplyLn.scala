package com.artclod.mathml.scalar.apply

import com.artclod.mathml.scalar._
import com.artclod.mathml.scalar.concept._

import scala.util._

case class ApplyLn(value: MathMLElem) extends Logarithm(ExponentialE.v, value, Seq(Ln): _*) with OneMathMLChild {

	override def eval(boundVariables: Map[String, Double]) = Try(math.log(v.eval(boundVariables).get))

	override def constant: Option[Constant] = v.c match {
		case Some(v) => Some(Logarithm.ln(v))
		case _ => None
	}

	def simplifyStep() = ApplyLn(v.s)

	def derivative(x: String) = {
		val f = v.s
		val fP = f.d(x)

		fP / f
	}

	override def toMathJS: String = "log(" + value.toMathJS + ")"

	def mathMLChild = value

	def copy(child: MathMLElem) = ApplyLn(child)
}