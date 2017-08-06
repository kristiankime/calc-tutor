package com.artclod.mathml

import org.junit.runner.RunWith
import org.scalatestplus.play._
import play.api.test.Helpers._
import scala.xml._
import play.api.test._
import play.api.test.Helpers._
import com.artclod.mathml.scalar._
import com.artclod.mathml.scalar.apply._
import com.artclod.mathml.scalar.apply.{ApplyLn => ln, ApplyLog => log}
import com.artclod.mathml.scalar.apply.trig.{ApplyCos => cos, ApplyCot => cot, ApplyCsc => csc, ApplySec => sec, ApplySin => sin, ApplyTan => tan}
import com.artclod.mathml.Match._
import org.scalatest.junit.JUnitRunner

// LATER try out http://rlegendi.github.io/specs2-runner/ and remove RunWith
@RunWith(classOf[JUnitRunner])
class MathMLCheckEqSpec extends PlaySpec {

	"Checking equality between symbolic differentiation and manual derivative " should {

		"confirm x' = x / x " in {
			val f = x
			val g = x / x
			((f dx) ?= g) mustBe(Yes)
		}

		"confirm (x^2 - x^2)' = 0 " in {
			val f = ((x ^ `2`) - (x ^ `2`))
			val g = `0`
			((f dx) ?= g) mustBe(Yes)
		}

		"confirm (4*x^3*e^x)/(x^2 - 5)' =  (((12 * (x ^ 2) * (e ^ x) + 4 * (x ^ 3) * (e ^ x)) * ((x ^ 2) - 5) - (4 * (x ^ 3) * (e ^ x)) * 2 * x)) / (((x ^ 2) - 5) ^ 2) " in {
			val f = ((`4` * (x ^ `3`)) * (e ^ x)) / ((x ^ `2`) - `5`)

			val g = (((`12` * (x ^ `2`) * (e ^ x) + `4` * (x ^ `3`) * (e ^ x)) * ((x ^ `2`) - `5`) - (`4` * (x ^ `3`) * (e ^ x)) * `2` * x)) / (((x ^ `2`) - `5`) ^ `2`)

			((f dx) ?= g) mustBe(Yes)
		}

		"confirm ln( 1/x + x^2 - 9)' = (-x^(-2) + 2*x)/(1/x + x^2 - 9)" in {
			val f = ln(`1` / x + (x ^ `2`) - `9`)

			val g = ((-(x ^ (`-2`))) + `2` * x) / (`1` / x + (x ^ `2`) - `9`)

			((f dx) ?= g) mustBe(Yes)
		}

		"confirm ((tan(x) / x^2) / 4^x)' = ((sec(x))^2*4^x*x^2 - tan(x)*(2*x*4^x+ln(4)*4^x*x^2))/(4^x*x^2)^2" in {
			val f = (tan(x) / (x ^ `2`)) / (`4` ^ x)
			val g = (((sec(x)) ^ `2`) * (`4` ^ x) * (x ^ `2`) - tan(x) * (`2` * x * (`4` ^ x) + ln(`4`) * (`4` ^ x) * (x ^ `2`))) / (((`4` ^ x) * (x ^ `2`)) ^ `2`)

			((f dx) ?= g) mustBe(Yes)
		}

		"confirm 1/5^x' = -5^(-x)*ln(5)" in {
			val f = `1` / (`5` ^ x)
			val g = -(`5`^(-x))*ln(`5`)
			((f dx) ?= g) mustBe(Yes)
		}

    "confirm ln(e^x)' = 1" in {
      val f = ln(e^x)
      val g = `1`

      ((f dx) ?= g) mustBe(Yes)
    }

    "confirm (e^x / e^x)' = 0" in {
      val f =  (e^x) / (e^x)
      val g = `0`

      ((f dx) ?= g) mustBe(Yes)
    }

    "confirm ((cot(x) ^ 2) * sin(x))' = -cos(x) - (cot(x) * csc(x))" in {
      val f =  ((cot(x) ^ `2`) * sin(x))
      val g = -cos(x) - (cot(x) * csc(x))

      ((f dx) ?= g) mustBe(Yes)
    }

    "confirm ((cot(x) ^ 2) * sin(x))' = 2 * cot(x) * -(csc(x)^2) * sin(x) + (cot(x)^2) * cos(x)" in {
      val f =  ((cot(x) ^ `2`) * sin(x))
      val g = `2` * cot(x) * -(csc(x)^`2`) * sin(x) + (cot(x)^`2`) * cos(x)

      ((f dx) ?= g) mustBe(Yes)
    }

    "confirm (sin(x)^2 + cos(x)^2 - 1)/(1 + ln(x*e^x/(1+tan(x)^7)))' = 0" in {
      val f = ( (sin(x)^2) + (cos(x)^2) - 1)/(1 + ln(x * (e^x) /(1+ (tan(x)^7) )))
      val g = `0`

      ((f dx) ?= g) mustBe(Yes)
    }

    "confirm (ln(exp(x))-x)' = 0" in {
      val f = (ln(e ^ x) - x)
      val g = `0`
      ((f dx) ?= g) mustBe(Yes)
    }
  }

	"Check that multiplying top and bottom of a quotient by the same thing " should {

		"have no effect for x^4 / x^2" in {
			val f = (x ^ `4`) / (x ^ `2`)
			val g = (x ^ `6`) / (x ^ `4`)
			(f ?= g) mustBe(Yes)
		}

	}



}