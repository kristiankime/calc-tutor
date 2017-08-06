package com.artclod.mathml.scalar

import com.artclod.mathml._
import com.artclod.mathml.scalar.apply._
import com.artclod.mathml.scalar.concept._

import scala.annotation.tailrec
import scala.util._
import scala.xml._

abstract class MathMLElem(
	prefix: String,
	label: String,
	attributes1: MetaData,
	scope: NamespaceBinding,
	minimizeEmpty: Boolean,
	child: Node*)
	extends Elem(prefix, label, attributes1, scope, minimizeEmpty, child: _*) {

  // LATER it would be nice if this was just called eval but Map[A,B] is Iterable[(A,B)] so the signatures conflict
  def evalT(boundVariables: (String, Double)*) : Try[Double] = eval(Map(boundVariables:_*))

	def eval(boundVariables: Map[String, Double] = Map()): Try[Double]

	def isZero: Boolean = c.map(_.isZero).getOrElse(false)

	def isOne: Boolean = c.map(_.isOne).getOrElse(false)

	private var s_ : MathMLElem = null
	def s = {
		if (s_ == null) {
			s_ = simplifyRecurse(this)
		}
		s_
	}
	def simplify = s

	@tailrec private def simplifyRecurse(elem: MathMLElem): MathMLElem = {
		val simp = elem.simplifyStepWithCNCheck
		if (simp == elem) { elem }
		else { simplifyRecurse(simp) }
	}

	private def simplifyStepWithCNCheck: MathMLElem = c.getOrElse(simplifyStep)

	/**
	 * Does one round of simplification on this element.
	 * Implementations of this method should not use the "s".
	 */
	protected def simplifyStep: MathMLElem

	protected def constant: Option[Constant]

	private var c_ : Option[Constant] = null
	def c = {
		if (c_ == null) {
			c_ = constant
		}
		c_
	}

	def variables: Set[String]

	protected def derivative(wrt: String): MathMLElem

	def d(wrt: String) =
		if (!variables.contains(wrt)) `0`
		else derivative(wrt).s

	def dx = d("x")

	def +(m: MathMLElem) = ApplyPlus(this, m)
  def +(m: Short) = ApplyPlus(this, Cn(m))
  def +(m: Int) = ApplyPlus(this, Cn(m))
  def +(m: Long) = ApplyPlus(this, Cn(m))
  def +(m: Float) = ApplyPlus(this, Cn(m))
  def +(m: Double) = ApplyPlus(this, Cn(m))

	def *(m: MathMLElem) = ApplyTimes(this, m)
  def *(m: Short) = ApplyTimes(this, Cn(m))
  def *(m: Int) = ApplyTimes(this, Cn(m))
  def *(m: Long) = ApplyTimes(this, Cn(m))
  def *(m: Float) = ApplyTimes(this, Cn(m))
  def *(m: Double) = ApplyTimes(this, Cn(m))

  def -(m: MathMLElem) = ApplyMinusB(this, m)
  def -(m: Short) = ApplyMinusB(this, Cn(m))
  def -(m: Int) = ApplyMinusB(this, Cn(m))
  def -(m: Long) = ApplyMinusB(this, Cn(m))
  def -(m: Float) = ApplyMinusB(this, Cn(m))
  def -(m: Double) = ApplyMinusB(this, Cn(m))

  def unary_-() = ApplyMinusU(this)

	def /(m: MathMLElem) = ApplyDivide(this, m)
  def /(m: Short) = ApplyDivide(this, Cn(m))
  def /(m: Int) = ApplyDivide(this, Cn(m))
  def /(m: Long) = ApplyDivide(this, Cn(m))
  def /(m: Float) = ApplyDivide(this, Cn(m))
  def /(m: Double) = ApplyDivide(this, Cn(m))

	def ^(m: MathMLElem) = ApplyPower(this, m)
  def ^(m: Short) = ApplyPower(this, Cn(m))
  def ^(m: Int) = ApplyPower(this, Cn(m))
  def ^(m: Long) = ApplyPower(this, Cn(m))
  def ^(m: Float) = ApplyPower(this, Cn(m))
  def ^(m: Double) = ApplyPower(this, Cn(m))

	def ?=(e: MathMLElem) = MathMLEq.checkEq("x", this, e)

  def isDefinedAt(boundVariables: (String, Double)*) = MathMLDefined.isDefinedAt(this, boundVariables:_*)

	def toMathJS : String
}
