package com.artclod.mathml.scalar.apply

import com.artclod.mathml.scalar.concept.{Constant, Logarithm}
import com.artclod.mathml.scalar.{Log, MathMLElem, OneMathMLChild, ln_10}

import scala.math.BigDecimal.int2bigDecimal
import scala.util.Try

case class ApplyLog10(value: MathMLElem) extends Logarithm(10, value, Seq(Log): _*) with OneMathMLChild {

	override def eval(boundVariables: Map[String, Double]) = Try(math.log10(v.eval(boundVariables).get))

	override def constant: Option[Constant] = v.c match {
		case Some(v) => Some(Logarithm.log10(v))
		case _ => None
	}

	def simplifyStep() = ApplyLog10(v.s)

	def derivative(x: String) = {
		val f = v.s
		val fP = f.d(x)

		fP / (ln_10 * f)
	}

	override def toMathJS: String = "log(" + value.toMathJS + ", 10)"

	def mathMLChild = value

	def copy(child: MathMLElem) = ApplyLog10(child)
}