package com.artclod.mathml.scalar.apply

import org.junit.runner.RunWith
import org.scalatestplus.play._
import play.api.test.Helpers._
import org.junit.runner.RunWith
import play.api.test._
import play.api.test.Helpers._
import com.artclod.mathml._
import com.artclod.mathml.scalar._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import math.E

// LATER try out http://rlegendi.github.io/specs2-runner/ and remove RunWith
@RunWith(classOf[JUnitRunner])
class ApplyRootSpec extends PlaySpec {

	"eval" should {
		"do nth root" in {
			ApplyRoot(3, 8).eval().get mustBe(2)
		}
	}

	"variables" should {
		"be empty if elements are constant" in {
			ApplyRoot(3, 8).variables mustBe empty
		}

		"be x if an element constains an x" in {
			ApplyRoot(5, x).variables mustBe(Set("x"))
		}
	}

	"c" should {
		"return correct log" in {
			ApplyRoot(3, 8).c.get mustBe(`2`)
		}

		"fail if not a constant " in {
			ApplyRoot(5, x).c mustBe empty
		}
	}

	"s" should {
		"return constant if value is constant" in {
			ApplyRoot(4, 16).s mustBe(`2`)
		}

		"remain unchanged if nothing can be simplified" in {
			ApplyRoot(5, x).s mustBe(ApplyRoot(5, x))
		}
	}

	"d" should {
		"obey the derivative rule for sqrt" in {
			ApplyRoot(2, F).dx mustBe(Fdx / (2 * √(F)))
		}

		"obey the derivative rule for arbitrary degree" in {
			ApplyRoot(5, F).dx mustBe(Fdx / (5 * `n√`(5d / (5-1))(F)))
		}
	}

	"toText" should {
		"handle nthRoot" in {
			ApplyRoot(3, 5).toMathJS mustBe("nthRoot(5, 3)")
		}
	}
}