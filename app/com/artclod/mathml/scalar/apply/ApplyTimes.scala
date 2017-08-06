package com.artclod.mathml.scalar.apply

import com.artclod.mathml._
import com.artclod.mathml.scalar._
import com.artclod.mathml.scalar.concept.Constant

import scala.util._

case class ApplyTimes(val values: MathMLElem*)
	extends MathMLElem(MathML.h.prefix, "apply", MathML.h.attributes, MathML.h.scope, false, (Seq[MathMLElem](Times) ++ values): _*) {

	def eval(boundVariables: Map[String, Double]): Try[Double] = {
		val tryVals = values.map(_.eval(boundVariables))
		tryVals.find(_.isFailure) match {
			case Some(failure) => failure
			case None => product(tryVals.map(_.get))
		}
	}

	private def product(vals: Seq[Double]): Try[Double] = {
		val product = vals.reduceLeft(_ * _)
		val hasNoZeroes = vals.find(_ == 0d).isEmpty

		if (product == 0d && hasNoZeroes) {
			Failure(new IllegalStateException("times returned 0 when there were no 0 elements " + this))
		} else {
			Success(product)
		}
	}

	def constant: Option[Constant] = if (values.forall(_.c.nonEmpty)) {
		Some(values.map(_.c.get).reduce(_ * _))
	} else if (values.map(_.c).contains(Some(`0`))) {
		Some(`0`)
	} else {
		None
	}

	def simplifyStep() =
		(cns, flattenedMathMLElems) match {
			case (Seq(cns @ _*), Seq()) => cns.reduce(_ * _)
			case (Seq(), Seq(elem)) => elem
			case (Seq(), Seq(elems @ _*)) => ApplyTimes(elems: _*)
			case (Seq(cns @ _*), Seq(elems @ _*)) => ApplyTimes(Seq(cns.reduce(_ * _)).filterNot(_.isOne) ++ elems: _*)
		}

	private def cns = values.map(_.c).filter(_.nonEmpty).map(_.get)

	private def flattenedMathMLElems: Seq[MathMLElem] = values.filter(_.c.isEmpty).map(_.s)
		.flatMap(_ match {
			case v: ApplyTimes => v.values
			case v: MathMLElem => Seq(v)
		})

	def variables: Set[String] = values.foldLeft(Set[String]())(_ ++ _.variables)

	def derivative(x: String): MathMLElem =
		if (values.size == 1) {
			values(0)
		} else {
			ApplyPlus((0 until values.size).map(i => derivativeForPos(x, i)): _*)
		}

	private def derivativeForPos(x: String, pos: Int) = {
		val items = for (i <- 0 until values.size) yield { if (i == pos) { values(i).d(x) } else { values(i).s } }
		ApplyTimes(items: _*)
	}

	def toMathJS = values.map(_.toMathJS).mkString("(", " * " ,")")
}
