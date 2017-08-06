package com.artclod.mathml.scalar

import org.junit.runner.RunWith
import play.api.test._
import play.api.test.Helpers._
import com.artclod.mathml._
import com.artclod.mathml.scalar._
import scala.util._
import com.artclod.mathml.scalar.concept.Constant

object NeedsSimp extends MathMLElem(MathML.h.prefix, "NeedsSimp", MathML.h.attributes, MathML.h.scope, true, Seq(): _*) {

	def eval(boundVariables: Map[String, Double]) = Failure(new UnsupportedOperationException())

	def constant: Option[Constant] = None

	def simplifyStep() = Simplified

	def variables: Set[String] = throw new UnsupportedOperationException()

	def derivative(wrt: String) = throw new UnsupportedOperationException()

	override def toMathJS: String = throw new UnsupportedOperationException

}


object Simplified extends MathMLElem(MathML.h.prefix, "Simplified", MathML.h.attributes, MathML.h.scope, true, Seq(): _*) {

	def eval(boundVariables: Map[String, Double]) = Failure(new UnsupportedOperationException())

	def constant: Option[Constant] = None

	def simplifyStep() = Simplified

	def variables: Set[String] = throw new UnsupportedOperationException()

	def derivative(wrt: String) = throw new UnsupportedOperationException()

	override def toMathJS: String = throw new UnsupportedOperationException

}
