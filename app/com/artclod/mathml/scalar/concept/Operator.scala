package com.artclod.mathml.scalar.concept

import com.artclod.mathml._
import com.artclod.mathml.scalar._

import scala.util._

abstract class Operator(override val label: String)
	extends MathMLElem(MathML.h.prefix, label, MathML.h.attributes, MathML.h.scope, true, Seq(): _*) {

	def eval(boundVariables: Map[String, Double]) = Failure(new UnsupportedOperationException("Operators should not get evaled, use eval on the surrounding apply element."))

	def constant: Option[Constant] = None

	def variables: Set[String] = Set()
	
	def simplifyStep() : this.type = this

	def derivative(wrt: String): MathMLElem = throw new UnsupportedOperationException("Operators should not get derived, use derive on the surrounding apply element.")

	def toMathJS = throw new UnsupportedOperationException("Operators should not toTexted, use toText on the surrounding apply element.")
}