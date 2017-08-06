package com.artclod.mathml.scalar.concept

import com.artclod.mathml._
import com.artclod.mathml.scalar._

import scala.util._

abstract class Logarithm(val b: BigDecimal, val v: MathMLElem, pre: MathMLElem*)
	extends MathMLElem(MathML.h.prefix, "apply", MathML.h.attributes, MathML.h.scope, false, (pre ++ v): _*) {

	def eval(boundVariables: Map[String, Double]) = Try(math.log(v.eval(boundVariables).get) / math.log(b.doubleValue))

	def constant: Option[Constant] = v.c match {
		case Some(v) => Some(Logarithm.log(b, v))
		case _ => None
	}

	def variables: Set[String] = v.variables
	
}

object Logarithm {

	def ln(value: Constant) = value match {
		case c: ConstantInteger => Cn(math.log(c.v.doubleValue))
		case c: ConstantDecimal => Cn(math.log(c.v.doubleValue))
	}

	def log10(value: Constant) = value match {
		case c: ConstantInteger => Cn(math.log10(c.v.doubleValue))
		case c: ConstantDecimal => Cn(math.log10(c.v.doubleValue))
	}

	def log(b: BigDecimal, value: Constant) = value match {
		case c: ConstantInteger => Cn(math.log(c.v.doubleValue) / math.log(b.doubleValue))
		case c: ConstantDecimal => Cn(math.log(c.v.doubleValue) / math.log(b.doubleValue))
	}

}