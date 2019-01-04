package com.artclod.mathml.scalar

import com.artclod.mathml._
import com.artclod.mathml.scalar.concept.Constant

import scala.util._
import scala.xml._


case class Mfenced(val value: MathMLElem)
	extends MathMLElem(MathML.h.prefix, "mfenced", MathML.h.attributes, MathML.h.scope, false, Seq(value): _*) with OneMathMLChild {

	def eval(boundVariables: Map[String, Double]) = value.eval(boundVariables)

	def constant: Option[Constant] = value.c match {
		case Some(v) => Some(v)
		case _ => None
	}

	def variables: Set[String] = value.variables

	def simplifyStep = Mfenced(value.s)

	def derivative(wrt: String) = Mfenced(value.d(wrt))

	def toMathJS = "(" + value.toMathJS + ")"

	override def mathMLChild: MathMLElem = value

	override def copy(child: MathMLElem): MathMLElem = Mfenced(child)
}