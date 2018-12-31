package com.artclod.mathml.scalar.apply

import com.artclod.mathml._
import com.artclod.mathml.scalar._
import com.artclod.mathml.scalar.concept.Constant

import scala.util._

case class ApplyPower(val base: MathMLElem, val exp: MathMLElem)
	extends MathMLElem(MathML.h.prefix, "apply", MathML.h.attributes, MathML.h.scope, false, (Seq[MathMLElem](Power) ++ base ++ exp): _*) with TwoMathMLChildren {

	def eval(boundVariables: Map[String, Double]): Try[Double] = {
		(base.eval(boundVariables), exp.eval(boundVariables)) match {
			case (bv: Failure[Double], _) => bv
			case (_, ev: Failure[Double]) => ev
			case (Success(bv), Success(ev)) => mathPowFailOnZero(bv, ev)
		}
	}

	/**
	 * In actual mathematics a^b is never 0 except when a is 0.
	 * So count 0 as Failure here unless the base is 0.
	 */
	private def mathPowFailOnZero(bv: Double, ev: Double): Try[Double] = {
		if (bv == 0d && ev > 0d) { return Success(0d) }
		else {
			val ret = math.pow(bv, ev)
			if (ret == 0d) { Failure(new IllegalStateException("power returned 0 for " + this)) }
			else { Success(ret) }
		}
	}

	def constant: Option[Constant] = (base.c, exp.c) match {
		case (Some(b), _) if (b.isOne) => Some(`1`)
		case (_, Some(e)) if (e.isZero) => Some(`1`)
		case (Some(b), Some(e)) => Some(b ^ e)
		case _ => None
	}

	def simplifyStep() =
		if (base.isOne) `1`
		else if (exp.isZero) `1`
		else if (exp.isOne) base.s
		else base.s ^ exp.s

	def variables: Set[String] = base.variables ++ exp.variables

	def derivative(x: String): MathMLElem = {
		// http://en.wikipedia.org/wiki/Differentiation_rules see functional power rule
		// (f ^ g)' = f^(g-1) * (g * f' + f * log(f) * g')
		val f = base.s
		val fP = f.d(x)
		val g = exp.s
		val gP = g.d(x)

		val first = (f ^ (g - `1`)).s
		val second = (g * fP + f * ApplyLn(f) * gP).s

		(first * second).s
	}

	override def toMathJS: String = "(" + base.toMathJS + " ^ " + exp.toMathJS + ")"

	def mathMLChildren = (base, exp)

	def copy(first: MathMLElem, second: MathMLElem) = ApplyPower(first, second)
}