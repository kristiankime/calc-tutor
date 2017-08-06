package com.artclod.mathml

import org.junit.runner.RunWith
import org.scalatestplus.play._
import play.api.test.Helpers._
import org.junit.runner.RunWith
import scala.xml._
import play.api.test._
import play.api.test.Helpers._
import com.artclod.mathml.scalar._
import com.artclod.mathml.scalar.apply._
import com.artclod.mathml.scalar.apply.trig._
import com.artclod.mathml.Match._
import com.artclod.mathml.scalar.apply.{ApplyLn => ln, ApplyLog => log, ApplyRoot => rt}
import org.scalatest.junit.JUnitRunner


// LATER try out http://rlegendi.github.io/specs2-runner/ and remove RunWith
@RunWith(classOf[JUnitRunner])
class MathMLEqSpec extends PlaySpec {

	"checkEq" should {

    "confirm rt(2, x^2) != x (abs of negatives)" in {
      MathMLEq.checkEq("x", rt(2, x ^2), x) mustBe(No)
    }

		"be true for two equal cns" in {
			MathMLEq.checkEq("x", `3`, `3`) mustBe(Yes)
		}

		"be true for two x+2 & 2+x" in {
			val v1 = x + `2`
			val v2 = `2` + x

			MathMLEq.checkEq("x", v1, v2) mustBe(Yes)
		}

		"be true for x^2 & x^2" in {
			val v1 = x ^ `2`
			val v2 = x ^ `2`

			MathMLEq.checkEq("x", v1, v2) mustBe(Yes)
		}

		"be true for [x + x + 2] & [2 * (x + 1)]" in {
			val v1 = x + x + `2`
			val v2 = `2` * (x + `1`)

			MathMLEq.checkEq("x", v1, v2) mustBe(Yes)
		}

		"be false for [x^2] & [x-2]" in {
			val v1 = x ^ `2`
			val v2 = x - `2`

			MathMLEq.checkEq("x", v1, v2) mustBe(No)
		}

		"be false for [2*x] & [x]" in {
			val v1 = `2` * x
			val v2 = x

			MathMLEq.checkEq("x", v1, v2) mustBe(No)
		}

		"be false for [x^2] & [1]" in {
			val v1 = x ^ `2`
			val v2 = `1`

			MathMLEq.checkEq("x", v1, v2) mustBe(No)
		}

		"be true for two log(x)s" in {
			MathMLEq.checkEq("x", ApplyLn(x), ApplyLn(x)) mustBe(Yes)
		}

		"be false for two logs with different bases" in {
			MathMLEq.checkEq("x", ApplyLog10(x), ApplyLn(x)) mustBe(No)
		}

	}

	"doubleNumbersCloseEnough" should {

		"be true for two identical numbers (near 1)" in {
			MathMLEq.doubleNumbersCloseEnough(1.23e+0, 1.23e+0) mustBe(true)
		}

		"be true for two close numbers (near 1)" in {
			MathMLEq.doubleNumbersCloseEnough(1.2300005e+0, 1.23e+0) mustBe(true)
		}

		"be false for two different numbers (near 1)" in {
			MathMLEq.doubleNumbersCloseEnough(1.24e+1, 1.23e+1) mustBe(false)
		}

		"be true for two identical very large numbers " in {
			MathMLEq.doubleNumbersCloseEnough(1.23e+100, 1.23e+100) mustBe(true)
		}

		"be true for two close very large numbers" in {
			MathMLEq.doubleNumbersCloseEnough(1.2300005e+100, 1.23e+100) mustBe(true)
		}

		"be false for two different very large numbers" in {
			MathMLEq.doubleNumbersCloseEnough(1.24e+100, 1.23e+100) mustBe(false)
		}

		"be true for two identical very small numbers" in {
			MathMLEq.doubleNumbersCloseEnough(1.23e-100, 1.23e-100) mustBe(true)
		}

		"be true for two close very small numbers" in {
			MathMLEq.doubleNumbersCloseEnough(1.2300005e-100, 1.23e-100) mustBe(true)
		}

		"be false for two different very small numbers" in {
			MathMLEq.doubleNumbersCloseEnough(1.24e-100, 1.23e-100) mustBe(false)
		}

		"be false for one large and one small number" in {
			MathMLEq.doubleNumbersCloseEnough(1.24e+100, 1.23e-100) mustBe(false)
		}
	}

}