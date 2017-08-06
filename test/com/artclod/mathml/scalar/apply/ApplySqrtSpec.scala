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

// LATER try out http://rlegendi.github.io/specs2-runner/ and remove RunWith
@RunWith(classOf[JUnitRunner])
class ApplySqrtSpec extends PlaySpec {

	"eval" should {
		"do square root" in {
			ApplySqrt(4).eval().get mustBe(2)
		}
	}

	"variables" should {
		"be empty if elements are constant" in {
			ApplySqrt(2).variables mustBe empty
		}

		"be x if an element constains an x" in {
			ApplySqrt(x).variables mustBe(Set("x"))
		}
	}

	"c" should {
		"return correct square root" in {
			ApplySqrt(4).c.get mustBe(`2`)
		}

		"fail if not a constant " in {
			ApplySqrt(x).c mustBe empty
		}
	}

	"s" should {
		"return constant if value is constant" in {
			ApplySqrt(9).s mustBe(`3`)
		}

		"remain unchanged if nothing can be simplified" in {
			ApplySqrt(x).s mustBe(√(x))
		}
	}

	"d" should {
		"obey the ln derivative rule: sqrt(f)' = f'/(2 * sqrt(f))" in {
			ApplySqrt(F).dx mustBe(Fdx / (2 * √(F)))
		}
	}

	"toText" should {
		"handle sqrt(7)" in {
			ApplySqrt(7).toMathJS mustBe("sqrt(7)")
		}
	}
}