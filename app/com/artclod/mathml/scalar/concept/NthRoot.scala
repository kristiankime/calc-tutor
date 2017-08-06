package com.artclod.mathml.scalar.concept

import com.artclod.mathml._
import com.artclod.mathml.scalar._

import scala.util._

abstract class NthRoot(val n: BigDecimal, val v: MathMLElem, pre: MathMLElem*)
	extends MathMLElem(MathML.h.prefix, "apply", MathML.h.attributes, MathML.h.scope, false, (pre ++ v): _*) {

	def eval(boundVariables: Map[String, Double]) = Try(math.pow(v.eval(boundVariables).get, 1d  / n.doubleValue))

	def constant: Option[Constant] = v.c match {
		case Some(v) => Some(NthRoot.root(n, v))
		case _ => None
	}

	def variables: Set[String] = v.variables
	
}

object NthRoot {

	def sqrt(value: Constant) = value match {
		case c: ConstantInteger => Cn(math.sqrt(c.v.doubleValue))
		case c: ConstantDecimal => Cn(math.sqrt(c.v.doubleValue))
	}

	def root(n: BigDecimal, value: Constant) = value match {
		case c: ConstantInteger => Cn(math.pow(c.v.doubleValue, 1d / n.doubleValue))
		case c: ConstantDecimal => Cn(math.pow(c.v.doubleValue, 1d / n.doubleValue))
	}

}