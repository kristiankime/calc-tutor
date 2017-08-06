package com.artclod.mathml.scalar.apply

import com.artclod.mathml._
import com.artclod.mathml.scalar._
import com.artclod.mathml.scalar.concept.Constant

import scala.util._

case class ApplyPlus(val values: MathMLElem*)
	extends MathMLElem(MathML.h.prefix, "apply", MathML.h.attributes, MathML.h.scope, false, (Seq[MathMLElem](Plus) ++ values): _*) {

	def eval(boundVariables: Map[String, Double]) = Try(values.map(_.eval(boundVariables).get).reduceLeft(_ + _))

	def constant: Option[Constant] =
		if (values.forall(_.c.nonEmpty)) {
			Some(values.map(_.c.get).reduce(_ + _))
		} else {
			None
		}

	def simplifyStep() = 
		(cns, flattenedMathMLElems) match {
			case (Seq(cns @ _*), Seq()) => cns.reduce(_ + _)
			case (Seq(), Seq(elem)) => elem
			case (Seq(), Seq(elems @ _*)) => ApplyPlus(elems: _*)
			case (Seq(cns @ _*), Seq(elems @ _*)) => ApplyPlus(elems ++ Seq(cns.reduce(_ + _)).filterNot(_.isZero): _*)
		}

	private def cns = values.map(_.c).filter(_.nonEmpty).map(_.get)

	private def flattenedMathMLElems: Seq[MathMLElem] = values.filter(_.c.isEmpty).map(_.s)
		.flatMap(_ match {
			case v: ApplyPlus => v.values
			case v: MathMLElem => Seq(v)
		})

	def variables: Set[String] = values.foldLeft(Set[String]())(_ ++ _.variables)

	def derivative(x: String) = ApplyPlus(values.map(_.d(x)): _*).s

	def toMathJS = values.map(_.toMathJS).mkString("(", " + " ,")")
}