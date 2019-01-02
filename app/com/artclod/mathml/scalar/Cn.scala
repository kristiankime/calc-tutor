package com.artclod.mathml.scalar

import com.artclod.mathml.scalar.concept._

import scala.math.ScalaNumber
import scala.util._
import scala.xml._

case class CnInteger(override val v: BigInt) extends ConstantInteger("cn", Cn.integerType, false, v, Seq(IntegerText(v)): _*) with NoMathMLChildren

case class CnReal(override val v: BigDecimal) extends ConstantDecimal("cn", Cn.realType, false, v, Seq(RealText(v)): _*) with NoMathMLChildren

object Cn {
	def apply(str: String): Try[Constant] = {
		val trimmed = str.trim
		(Try(BigInt(trimmed)), Try(BigDecimal(trimmed))) match {
			case (Success(v), _) => Success(Cn(v))
			case (_, Success(v)) => Success(Cn(v))
			case (Failure(a), Failure(b)) => Failure(b)
		}
	}

	def apply(value: Node): Try[Constant] = apply(value.text)

	def apply(value: Short) = CnInteger(value.toInt)

	def apply(value: Int) = CnInteger(value)

	def apply(value: Long) = CnInteger(value)

	def apply(value: BigInt) = CnInteger(value)

	def apply(value: Float): Constant = Cn.apply(BigDecimal(value.toDouble))

	def apply(value: Double): Constant = Cn(BigDecimal(value))

	def apply(value: BigDecimal): Constant = if (value.isWhole) Cn(value.toBigInt) else Cn.bigDecimal(value)

	def bigDecimal(value: BigDecimal) = CnReal(value)

	val realType = <cn type="real"></cn>.attributes

	val integerType = <cn type="integer"></cn>.attributes
}

class NumberText[T <: ScalaNumber](val num: T) extends Text(num.toString)

case class RealText(override val num: BigDecimal) extends NumberText[BigDecimal](num) // LATER Integers print as 2.0

case class IntegerText(override val num: BigInt) extends NumberText[BigInt](num)
