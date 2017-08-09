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

class ApplyLnSpec extends PlaySpec {

	"eval" should {
		"do natural log" in {
			ApplyLn(e).eval().get mustBe(1)
		}
	}

	"variables" should {
		"be empty if elements are constant" in {
			ApplyLn(`2`).variables mustBe empty
		}

		"be x if an element constains an x" in {
			ApplyLn(x).variables mustBe(Set("x"))
		}
	}

	"c" should {
		"return correct log" in {
			ApplyLn(e).c.get mustBe(`1`)
		}

		"fail if not a constant " in {
			ApplyLn(x).c mustBe empty
		}
	}

	"s" should {
		"return constant if value is constant" in {
			ApplyLn(e).s mustBe(`1`)
		}

		"remain unchanged if nothing can be simplified" in {
			ApplyLn(x).s mustBe(ApplyLn(x))
		}
	}

	"d" should {
		"obey the ln derivative rule: ln(f)' = f'/f" in {
			ApplyLn(F).dx mustBe(Fdx / F)
		}
	}

	"toText" should {
		"handle log(7)" in {
			ApplyLn(7).toMathJS mustBe("log(7)")
		}
	}

}