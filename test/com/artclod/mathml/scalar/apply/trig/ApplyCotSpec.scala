package com.artclod.mathml.scalar.apply.trig

import org.junit.runner.RunWith
import org.scalatestplus.play._
import play.api.test.Helpers._
import org.junit.runner.RunWith
import play.api.test._
import play.api.test.Helpers._
import com.artclod.mathml._
import com.artclod.mathml.scalar._
import com.artclod.mathml.scalar.concept.Trigonometry
import org.scalatest.junit.JUnitRunner

class ApplyCotSpec extends PlaySpec {

	"eval" should {
		"do cot" in {
			ApplyCot(π/2).eval().get mustBe(Trigonometry.cot(math.Pi/2))
		}
	}

	"c" should {
		"return correct cot" in {
			ApplyCot(π/2).c.get mustBe(Cn(Trigonometry.cot(math.Pi/2)))
		}

		"fail if not a constant " in {
			ApplyCot(x).c mustBe empty
		}
	}

	"s" should {
		"simplify what can be simpified" in {
			ApplyCot(NeedsSimp).s mustBe(ApplyCot(Simplified))
		}

		"remain unchanged if nothing can be simplified" in {
			ApplyCot(x).s mustBe(ApplyCot(x))
		}
	}

	"d" should {
		"obey the derivative rule: cot(f)' = -csc(f)^2 * f'" in {
			ApplyCot(F).dx mustBe((-(ApplyCsc(F) ^ `2`)) * Fdx)
		}
	}

	"toText" should {
		"handle cot(3)" in {
			ApplyCot(3).toMathJS mustBe("cot(3)")
		}
	}

}
