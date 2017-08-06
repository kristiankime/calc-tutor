package com.artclod.mathml.scalar.concept

import com.artclod.mathml.MathML
import com.artclod.mathml.scalar._

import scala.util.{Success, Try}
import scala.xml._

// LATER might be able to make Constant scala.math.Numeric 
abstract class Constant(name: String, attributes1: MetaData, minimizeEmpty: Boolean, val v: Any, override val child: Node*)
	extends MathMLElem(MathML.h.prefix, name, attributes1, MathML.h.scope, minimizeEmpty, child: _*) {

	def constant: Option[this.type] = Some(this)

	override val c : Option[this.type] = Some(this);

	def simplifyStep(): this.type = this

	override val s : this.type = this;

	def variables: Set[String] = Set()

	def derivative(wrt: String) = `0`

	def isZero(): Boolean

	def isOne(): Boolean

	def +(c: Constant): Constant

	def *(c: Constant): Constant

	def -(c: Constant): Constant

	def /(c: Constant): Constant

	def ^(c: Constant): Constant
}

abstract class ConstantInteger(name: String, attributes1: MetaData, minimizeEmpty: Boolean, override val v: BigInt, override val child: Node*)
	extends Constant(name, attributes1, minimizeEmpty, v, child: _*) {

	def eval(boundVariables: Map[String, Double]) = Success(v.doubleValue)

	override def isZero() = { v.compare(BigInt(0)) == 0 }

	override def isOne() = { v.compare(BigInt(1)) == 0 }

	def +(c: Constant) = c match {
		case m: ConstantInteger => Cn(v + m.v)
		case m: ConstantDecimal => Cn(BigDecimal(v) + m.v)
	}

	def *(c: Constant) = c match {
		case m: ConstantInteger => Cn(v * m.v)
		case m: ConstantDecimal => Cn(BigDecimal(v) * m.v)
	}

	def -(c: Constant) = c match {
		case m: ConstantInteger => Cn(v - m.v)
		case m: ConstantDecimal => Cn(BigDecimal(v) - m.v)
	}

	def /(c: Constant) = c match {
		case m: ConstantInteger => Cn(BigDecimal(v) / BigDecimal(m.v))
		case m: ConstantDecimal => Cn(BigDecimal(v) / m.v)
	}

	def ^(c: Constant) = c match {
		case m: ConstantInteger => Cn(v.pow(m.v.intValue))
		case m: ConstantDecimal => Cn(math.pow(v.doubleValue, m.v.doubleValue))
	}

	def toMathJS = v.toString()
}

abstract class ConstantDecimal(name: String, attributes1: MetaData, minimizeEmpty: Boolean, override val v: BigDecimal, override val child: Node*)
	extends Constant(name, attributes1, minimizeEmpty, v, child: _*) {

	def eval(boundVariables: Map[String, Double]) = Try(v.doubleValue)

	override def isZero() = { v.compare(BigDecimal(0)) == 0 }

	override def isOne() = { v.compare(BigDecimal(1)) == 0 }

	def +(c: Constant) = c match {
		case m: ConstantInteger => Cn(v + BigDecimal(m.v))
		case m: ConstantDecimal => Cn(v + m.v)
	}

	def *(c: Constant) = c match {
		case m: ConstantInteger => Cn(v * BigDecimal(m.v))
		case m: ConstantDecimal => Cn(v * m.v)
	}

	def -(c: Constant) = c match {
		case m: ConstantInteger => Cn(v - BigDecimal(m.v))
		case m: ConstantDecimal => Cn(v - m.v)
	}

	def /(c: Constant) = c match {
		case m: ConstantInteger => Cn(v / BigDecimal(m.v))
		case m: ConstantDecimal => Cn(v / m.v)
	}

	def ^(c: Constant) = c match {
		case m: ConstantInteger => Cn(math.pow(v.doubleValue, m.v.doubleValue))
		case m: ConstantDecimal => Cn(math.pow(v.doubleValue, m.v.doubleValue))
	}

	def toMathJS = v.toString()
}