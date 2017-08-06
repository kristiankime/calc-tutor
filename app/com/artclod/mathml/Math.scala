package com.artclod.mathml

import com.artclod.mathml.scalar.MathMLElem
import com.artclod.mathml.scalar.concept.Constant

import scala.xml._

case class Math(
	override val prefix: String,
	attributes1: MetaData,
	override val scope: NamespaceBinding,
	override val minimizeEmpty: Boolean,
	val value: MathMLElem)
	extends MathMLElem(prefix, "math", attributes1, scope, minimizeEmpty, Seq(value): _*) {

	def this(value: MathMLElem) = this(MathML.h.prefix, MathML.h.attributes, MathML.h.scope, false, value)

	def eval(boundVariables: Map[String, Double]) = value.eval(boundVariables)

	def constant: Option[Constant] = value.c
	
	def simplifyStep() = Math(prefix, attributes, scope, minimizeEmpty, value.s)

	def variables: Set[String] = value.variables

	def derivative(wrt: String) = Math(prefix, attributes, scope, minimizeEmpty, value.d(wrt).s)

	override def toMathJS: String = value.toMathJS
}

object Math {
	def apply(value: MathMLElem) = new Math(value)
}
