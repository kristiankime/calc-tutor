package com.artclod.mathml.scalar.apply.trig

import org.junit.runner.RunWith
import org.scalatestplus.play._
import play.api.test.Helpers._
import org.junit.runner.RunWith
import play.api.test._
import play.api.test.Helpers._
import com.artclod.mathml._
import com.artclod.mathml.scalar._
import org.scalatest.junit.JUnitRunner

class ApplyCosSpec extends PlaySpec {

	"eval" should {
		"do sin" in {
			ApplyCos(π).eval().get mustBe(-1)
		}
	}

	"c" should {
		"return correct sin" in {
			ApplyCos(π).c.get mustBe(`-1`)
		}

		"fail if not a constant " in {
			ApplyCos(x).c mustBe empty
		}
	}

	"s" should {
		"return constant if value is constant" in {
			ApplyCos(π).s mustBe(`-1`)
		}

		"simplify what can be simpified" in {
			ApplyCos(NeedsSimp).s mustBe(ApplyCos(Simplified))
		}

		"remain unchanged if nothing can be simplified" in {
			ApplyCos(x).s mustBe(ApplyCos(x))
		}
	}

	"d" should {
		"obey the sin derivative rule: cos(f)' = -sin(f)f'" in {
			ApplyCos(F).dx mustBe(-ApplySin(F) * Fdx)
		}
	}

	"toText" should {
		"handle cos(3)" in {
			ApplyCos(3).toMathJS mustBe("cos(3)")
		}
	}

}
