package com.artclod.mathml.scalar.concept

import com.artclod.mathml.MathML
import com.artclod.mathml.scalar._

abstract class UnaryFunction(val v: MathMLElem, pre: MathMLElem)
	extends MathMLElem(MathML.h.prefix, "apply", MathML.h.attributes, MathML.h.scope, false, (Seq(pre) ++ v): _*) {

	def variables: Set[String] = v.variables
	
}