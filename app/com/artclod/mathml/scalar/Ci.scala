package com.artclod.mathml.scalar

import com.artclod.mathml._
import com.artclod.mathml.scalar.concept.Constant

import scala.util._
import scala.xml._

case class Ci(val identifier: IdentifierText) extends MathMLElem(MathML.h.prefix, "ci", MathML.h.attributes, MathML.h.scope, false, Seq(identifier): _*) with NoMathMLChildren {

	override val c = None;

	override val s = this;

	def eval(boundVariables: Map[String, Double]) = Try(boundVariables.get(text).get)

	def constant: Option[Constant] = None

	def simplifyStep() = this

	def variables: Set[String] = Set(identifier.name.trim)

	def derivative(wrt: String): MathMLElem = if (text.trim == wrt) Cn(1) else Cn(0)

	def toMathJS = identifier.name.trim
}

object Ci {
	def apply(value: String) = new Ci(IdentifierText(value))
}

case class IdentifierText(nameStr: String) extends Text(nameStr.trim){
	val name = nameStr.trim
}