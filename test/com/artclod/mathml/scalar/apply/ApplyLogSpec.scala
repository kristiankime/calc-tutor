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

class ApplyLogSpec extends PlaySpec {

	"eval" should {
		"do natural log" in {
			ApplyLog(6, `36`).eval().get mustBe(2)
		}
	}

	"variables" should {
		"be empty if elements are constant" in {
			ApplyLog(5, `2`).variables mustBe empty
		}

		"be x if an element constains an x" in {
			ApplyLog(5, x).variables mustBe(Set("x"))
		}
	}

	"c" should {
		"return correct log" in {
			ApplyLog(2.5, Cn(15.625)).c.get mustBe(`3`)
		}

		"fail if not a constant " in {
			ApplyLog(5, x).c mustBe empty
		}
	}

	"s" should {
		"return constant if value is constant" in {
			ApplyLog(5, `25`).s mustBe(`2`)
		}

		"remain unchanged if nothing can be simplified" in {
			ApplyLog(5, x).s mustBe(ApplyLog(5, x))
		}
	}

	"d" should {
		"obey the ln derivative rule: log_b(f)' = f' / ln(b) f" in {
			ApplyLog(E * E, F).dx mustBe(Fdx / (`2` * F))
		}
	}

	"toText" should {
		"handle log(4,2)" in {
			ApplyLog(2, 4).toMathJS mustBe("log(4, 2)")
		}
	}

}