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
class ApplyMinusUSpec extends PlaySpec {

	"eval" should {
		"return negative of value" in {
			ApplyMinusU(`6`).eval().get mustBe(-6)
		}
	}
	
	"variables" should {
		"be empty if element is constant" in {
			ApplyMinusU(`2`).variables mustBe empty
		}

		"be x if element constains an x" in {
			ApplyMinusU(x).variables mustBe(Set("x"))
		}

		"be y if element constains a y" in {
			ApplyMinusU(y).variables mustBe(Set("y"))
		}
	}
	
	"c" should {
		"return 0 if value is 0" in {
			ApplyMinusU(`0`).c.get mustBe(`0`)
		}

		"return 1 if value is -1" in {
			ApplyMinusU(`-1`).c.get mustBe(`1`)
		}

		"return negative of a value" in {
			ApplyMinusU(`3`).c.get mustBe(`-3`)
		}

		"fail if not a constant " in {
			ApplyMinusU(x).c mustBe empty
		}
	}

	"s" should {
		"return constant if value is constant" in {
			ApplyMinusU(`-4`).s mustBe(`4`)
		}

		"remain unchanged if nothing can be simplified" in {
			ApplyMinusU(x).s mustBe(ApplyMinusU(x))
		}
	}

	"d" should {
		"return negative of values derivative" in {
			ApplyMinusU(F).dx mustBe(ApplyMinusU(Fdx))
		}
	}

	"toText" should {
		"handle -4" in {
			ApplyMinusU(4).toMathJS mustBe("-4")
		}
	}

}