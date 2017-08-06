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
class ApplyLog10Spec extends PlaySpec {

	"eval" should {
		"do natural log" in {
			ApplyLog10(Cn(100)).eval().get mustBe(2)
		}
	}

	"variables" should {
		"be empty if elements are constant" in {
			ApplyLog10(`2`).variables mustBe empty
		}

		"be x if an element constains an x" in {
			ApplyLog10(x).variables mustBe(Set("x"))
		}
	}

	"c" should {
		"return correct log" in {
			ApplyLog10(Cn(math.pow(10, 2))).c.get mustBe(Cn(2))
		}

		"fail if not a constant " in {
			ApplyLog10(x).c mustBe empty
		}
	}

	"s" should {
		"return constant if value is constant" in {
			ApplyLog10(Cn(math.pow(10, 3.5))).s mustBe(`3.5`)
		}

		"remain unchanged if nothing can be simplified" in {
			ApplyLog10(x).s mustBe(ApplyLog10(x))
		}
	}

	"d" should {
		"obey the ln derivative rule: log_10(f)' = f' / ln(10) f" in {
			ApplyLog10(F).dx mustBe(Fdx / (ln_10 * F))
		}
	}

	"toText" should {
		"handle log(3, 10)" in {
			ApplyLog10(3).toMathJS mustBe("log(3, 10)")
		}
	}

}