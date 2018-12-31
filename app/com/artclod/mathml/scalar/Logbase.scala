package com.artclod.mathml.scalar

import com.artclod.mathml._
import com.artclod.mathml.scalar.concept.Constant

import scala.util.{Success, _}
import scala.xml._

case class Logbase(val value: Constant)
	extends MathMLElem(MathML.h.prefix, "logbase", MathML.h.attributes, MathML.h.scope, false, Seq(value): _*) with NoMathMLChildren {

	def eval(boundVariables: Map[String, Double]) = Failure(new UnsupportedOperationException("Logbase should not get evaled, use eval on the surrounding element."))

	def constant: Option[Constant] = None

	def variables: Set[String] = Set()

	def simplifyStep() = this

	def derivative(wrt: String): MathMLElem = throw new UnsupportedOperationException("Logbase should not get derived, use derive on the surrounding element.")

	val v : BigDecimal = value match {
		case c: CnInteger => BigDecimal(c.v)
		case c: CnReal => c.v
	}

	override def toMathJS: String = value.toMathJS
}

object Logbase {

	def apply(v: BigDecimal): Logbase = Logbase(Cn(v))

	def apply(e: Elem): Try[Logbase] = MathML(e) match {
		case Failure(a) => Failure(a)
		case Success(a) => a match {
			case c: CnInteger => Logbase(Cn(c.v))
			case c: CnReal => Logbase(Cn(c.v))
			case _ => Failure(new IllegalArgumentException("Could not create Logbase from " + e))
		}
	}
}