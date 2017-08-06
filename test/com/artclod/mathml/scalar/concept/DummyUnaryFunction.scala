package com.artclod.mathml.scalar.concept

import com.artclod.mathml._
import com.artclod.mathml.scalar._
import com.artclod.mathml.scalar.concept._
import play.api.test.Helpers._

object Dummy extends Operator("dummy")

case class DummyUnaryFunction(override val v: MathMLElem) extends UnaryFunction(v, Dummy){

	override def eval(b: Map[String, Double]) = throw new UnsupportedOperationException

	override def constant: Option[Constant] = throw new UnsupportedOperationException

	def simplifyStep() = throw new UnsupportedOperationException

	def derivative(x: String) = throw new UnsupportedOperationException

	override def toMathJS: String = throw new UnsupportedOperationException

}