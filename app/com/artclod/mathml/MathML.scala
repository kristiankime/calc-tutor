package com.artclod.mathml

import com.artclod.mathml.scalar._
import com.artclod.mathml.scalar.apply._
import com.artclod.mathml.scalar.apply.trig._
import com.artclod.mathml.scalar.concept.Constant

import scala.util._
import scala.xml._

object MathML {
	val h = <hack/> // This is a hack so we can get default XML meta data for default MathML objects
		
	def apply(text: String): Try[MathMLElem] = Try(xml.XML.loadString(text)).flatMap(apply(_))

	def apply(xml: Elem): Try[MathMLElem] = {
		xml.label.toLowerCase match {
			case "math" => Try(Math(xml.prefix, xml.attributes, xml.scope, xml.minimizeEmpty, MathML(xml.childElem(0)).get))
			case "apply" => applyElement(xml)
			case "cn" => constantElement(xml)
			case "ci" => Success(Ci(xml.child(0).text)) // LATER child(0).text could be nonsense
			case "exponentiale" => Success(ExponentialE)
			case "pi" => Success(com.artclod.mathml.scalar.Pi)
			case "logbase" => Cn(xml.childElem(0)).map(Logbase(_)) // LATER need to handle special Constants, xml.childElem(0) could fail
			case "degree" => Cn(xml.childElem(0)).map(Degree(_)) // LATER need to handle special Constants, xml.childElem(0) could fail
			case "mfenced" => MathML(xml.childElem(0)).map(Mfenced(_))
			case _ => Failure(new IllegalArgumentException(xml + " was not recognized as a MathML element"))
		}
	}

	private def constantElement(xml: Elem): Try[Constant] = Cn(xml.child(0))

	private def applyElement(xml: Elem): Try[MathMLElem] = {
		if (xml.childElem.size < 2) {
			Failure(new IllegalArgumentException("Apply MathML Elements must have at least two children " + xml))
		} else {
			val apply = xml
			val operator = xml.childElem(0)
			val argumentsTry = xml.childElem.drop(1).map(MathML(_))
			val failure = argumentsTry.find(_.isFailure)

			if (failure.nonEmpty) failure.get
			else applyElementCreate(apply, operator, argumentsTry.map(_.get))
		}
	}

	private def applyElementCreate(a: Elem, o: Elem, args: Seq[MathMLElem]): Try[MathMLElem] = {
		(o.label.toLowerCase(), args) match {
			case ("plus", _) => Success(ApplyPlus(args: _*))
			case ("minus", Seq(v)) => Success(ApplyMinusU(v))
			case ("minus", Seq(v1, v2)) => Success(ApplyMinusB(v1, v2))
			case ("times", _) => Success(ApplyTimes(args: _*))
			case ("divide", Seq(num, den)) => Success(ApplyDivide(num, den))
			case ("power", Seq(base, exp)) => Success(ApplyPower(base, exp))
			case ("ln", Seq(value)) => Success(ApplyLn(value))
			case ("log", Seq(value)) => Success(ApplyLog10(value))
			case ("log", Seq(b: Logbase, value)) => Success(ApplyLog(b.v, value))
			case ("sin", Seq(v)) => Success(ApplySin(v))
			case ("cos", Seq(v)) => Success(ApplyCos(v))
			case ("tan", Seq(v)) => Success(ApplyTan(v))
			case ("sec", Seq(v)) => Success(ApplySec(v))
			case ("csc", Seq(v)) => Success(ApplyCsc(v))
			case ("cot", Seq(v)) => Success(ApplyCot(v))
			case ("root", Seq(value)) => Success(ApplySqrt(value))
			case ("root", Seq(d: Degree, value)) => Success(ApplyRoot(d.v, value))
			case (a, c) => Failure(new IllegalArgumentException(o + " was not recognized as an applyable MathML element (label [" + o.label + "] might not be recognized or wrong number of child elements [" + c.length + "])"))
		}
	}

	private implicit class PimpedElem(e: Elem) {
		def childElem : Seq[Elem] = e.child.collect(_ match { case x: Elem => x })
	}
}
