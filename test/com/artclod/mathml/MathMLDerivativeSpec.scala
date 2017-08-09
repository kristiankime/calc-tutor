package com.artclod.mathml

import org.junit.runner.RunWith
import org.scalatestplus.play._
import play.api.test.Helpers._
import com.artclod.mathml.Match._
import org.junit.runner.RunWith
import com.artclod.mathml.scalar._
import org.junit.runner.RunWith
import scala.xml._
import play.api.test._
import play.api.test.Helpers._
import com.artclod.mathml.scalar._
import com.artclod.mathml.scalar.apply._
import com.artclod.mathml.scalar.apply.{ApplyLn => ln, ApplyLog => log}
import com.artclod.mathml.scalar.apply.trig.{ApplyCos => cos, ApplyCot => cot, ApplyCsc => csc, ApplySec => sec, ApplySin => sin, ApplyTan => tan}
import com.artclod.mathml.Match._
import org.scalatest.junit.JUnitRunner

class MathMLDerivativeSpec extends PlaySpec {

	"Checking symbolic differentiation and manual derivative " should {

		"confirm ln(x)' = 1 / x" in {
			(ln(x) dx) mustBe(`1` / x)
		}

		// LATER try to simplify these
		//
		//		"1 / ln(x)' = -1 / (x * log(x)^2)" in {
		//			( (`1` / ln(x))ʹ ) mustBe( `-1` / (x * (ln(x) ^ `2`)) )
		//		}
		//		
		//		"x / ln(x)' = (ln(x)-1) / (ln(x)^2)" in {
		//			(x / ln(x)ʹ) mustBe((ln(x) - `1`) / (ln(x) ^ `2`))
		//		}

		//		<apply><times/><apply><times/><cn type="integer">4</cn><apply><power/><ci>x</ci><cn type="integer">3</cn></apply></apply><apply><power/><exponentiale/><ci>x</ci></apply></apply>
		
//		"(4 * x ^ 3 * e ^ x)' = " in {
//			val mathF = MathML(<apply><times/><apply><times/><cn type="integer">4</cn><apply><power/><ci>x</ci><cn type="integer">3</cn></apply></apply><apply><power/><exponentiale/><ci>x</ci></apply></apply>).get
//		    val f =  ((`4` * (x ^ `3`)) * (e ^ x))
//		    
//		    System.err.println(f dx);
//		    
//		    f mustBe(mathF)
//		    
//		    (f dx) mustBe(   Cn(4 * math.E) )
//		}
		
	}

}