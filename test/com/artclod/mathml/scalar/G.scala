package com.artclod.mathml.scalar

import org.junit.runner.RunWith
import play.api.test._
import play.api.test.Helpers._
import com.artclod.mathml._
import com.artclod.mathml.scalar._
import scala.util._
import com.artclod.mathml.scalar.concept.Constant

object G extends MathMLElem(MathML.h.prefix, "G", MathML.h.attributes, MathML.h.scope, true, Seq(): _*) {

	def eval(boundVariables: Map[String, Double]) = Failure(new UnsupportedOperationException())

	def constant: Option[Constant] = None

	def simplifyStep() = G.this

	def variables: Set[String] = Set("x")

	def derivative(wrt: String) = if (wrt == "x") Gdx else Cn(0)

	override def toMathJS: String = throw new UnsupportedOperationException

}
