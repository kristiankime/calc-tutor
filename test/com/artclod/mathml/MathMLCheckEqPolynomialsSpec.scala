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
import com.artclod.mathml.scalar.apply.{ApplyLn => ln}
import com.artclod.mathml.scalar.apply.{ApplyLog => log}
import com.artclod.mathml.Match._
import org.scalatest.junit.JUnitRunner

class MathMLCheckEqPolynomialsSpec extends PlaySpec {

	"Checking equality between symbolic differentiation and manual derivative for these polynomials" should {

		"confirm (5x + 4)' = 5" in {
			val f = (`5` * x + `4`) dx
			val g = `5`
			(f ?= g) mustBe(Yes)
		}

		"confirm (5x + 4)' != 4.9" in {
			val f = (`5` * x + `4`) dx
			val g = Cn(4.9)
			(f ?= g) mustBe(No)
		}

		"confirm (x^3)' = 3x^2" in {
			val f = (x ^ `3`)dx
			val g = `3` * (x ^ `2`)
			(f ?= g) mustBe(Yes)
		}

		"confirm (x^3)' != 4x^2" in {
			val f = (x ^ `3`) dx
			val g = `4` * (x ^ `2`)
			(f ?= g) mustBe(No)
		}

		"confirm (2x^2 + -3x + -2)' = 4x -3" in {
			val f = (`2` * (x ^ `2`) + `-3` * x + `-2`) dx
			val g = (`4` * x - `3`)
			(f ?= g) mustBe(Yes)
		}

		"confirm (2x^2 + -3x + -2)' != 3x -3" in {
			val f = (`2` * (x ^ `2`) + `-3` * x + `-2`) dx
			val g = (`3` * x - `3`)
			(f ?= g) mustBe(No)
		}

		"confirm (x^3 + 3x + 4)' = 3x^2+3" in {
			val f = ((x ^ `3`) + `3` * x + `4`) dx
			val g = (`3` * (x ^ `2`) + `3`)
			(f ?= g) mustBe(Yes)
		}

		"confirm (x^3 + 3x) = 3x^2 + 3" in {
			val f = ((x ^ `3`) + `3` * x) dx
			val g = (`3` * (x ^ `2`) + `3`)
			(f ?= g) mustBe(Yes)
		}

	}

}