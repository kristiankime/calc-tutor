package com.artclod.mathml.scalar

import org.junit.runner.RunWith
import play.api.test._
import play.api.test.Helpers._
import com.artclod.mathml._
import com.artclod.mathml.scalar._
import scala.util._
import com.artclod.mathml.scalar.concept.Constant

object Gdx extends MathMLElem(MathML.h.prefix, "Gdx", MathML.h.attributes, MathML.h.scope, true, Seq(): _*) {

	def eval(boundVariables: Map[String, Double]) = Failure(new UnsupportedOperationException())

	def constant: Option[Constant] = None

	def simplifyStep() = Gdx.this

	def variables: Set[String] = Set("x")

	def derivative(wrt: String) = if (wrt == "x") throw new UnsupportedOperationException() else Cn(0)

	override def toMathJS: String = throw new UnsupportedOperationException

}
