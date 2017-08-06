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

// LATER try out http://rlegendi.github.io/specs2-runner/ and remove RunWith
@RunWith(classOf[JUnitRunner])
class ApplyCscSpec extends PlaySpec {

	"eval" should {
		"do csc" in {
			ApplyCsc(π/2).eval().get mustBe(Trigonometry.csc(math.Pi/2))
		}
	}

	"c" should {
		"return correct cot" in {
			ApplyCsc(π/2).c.get mustBe(Cn(Trigonometry.csc(math.Pi/2)))
		}

		"fail if not a constant " in {
			ApplyCsc(x).c mustBe empty
		}
	}

	"s" should {
		"simplify what can be simpified" in {
			ApplyCsc(NeedsSimp).s mustBe(ApplyCsc(Simplified))
		}

		"remain unchanged if nothing can be simplified" in {
			ApplyCsc(x).s mustBe(ApplyCsc(x))
		}
	}

	"d" should {
		"obey the derivative rule: csc(f)' = -cot(f) csc(f) f'" in {
			ApplyCsc(F).dx mustBe((-ApplyCot(F) * ApplyCsc(F) * Fdx)s)
		}
	}

	"toText" should {
		"handle csc(3)" in {
			ApplyCsc(3).toMathJS mustBe("csc(3)")
		}
	}

}
