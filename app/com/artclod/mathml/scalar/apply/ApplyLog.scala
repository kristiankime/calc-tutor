package com.artclod.mathml.scalar.apply

import com.artclod.mathml.scalar._
import com.artclod.mathml.scalar.concept._

case class ApplyLog(base: BigDecimal, value: MathMLElem) extends Logarithm(base, value, Seq(Log, Logbase(base)): _*) with OneMathMLChild {

	def simplifyStep() = ApplyLog(b, v.s)

	def derivative(x: String) = {
		val f = v.s
		val fP = f.d(x)
		val log_b = Cn(math.log(b.doubleValue))

		fP / (log_b * f)
	}

	override def toMathJS: String = "log(" + value.toMathJS + ", " + base.toString() + ")"

	def mathMLChild = value

	def copy(child: MathMLElem) = ApplyLog(child)
}

object ApplyLog {
	def apply(value: MathMLElem): ApplyLog10 = ApplyLog10(value)
}