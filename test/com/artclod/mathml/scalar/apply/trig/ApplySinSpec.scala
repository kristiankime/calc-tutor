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

// LATER try out http://rlegendi.github.io/specs2-runner/ and remove RunWith
@RunWith(classOf[JUnitRunner])
class ApplySinSpec extends PlaySpec {

	"eval" should {
		"do sin" in {
			ApplySin(π / `2`).eval().get mustBe(1)
		}
	}

	"c" should {
		"return correct sin" in {
			ApplySin(π / `2`).c.get mustBe(`1`)
		}

		"fail if not a constant " in {
			ApplySin(x).c mustBe empty
		}
	}

	"s" should {
		"simplify what can be simpified" in {
			ApplySin(NeedsSimp).s mustBe(ApplySin(Simplified))
		}

		"remain unchanged if nothing can be simplified" in {
			ApplySin(x).s mustBe(ApplySin(x))
		}
	}

	"d" should {
		"obey the derivative rule: sin(f)' = cos(f)f'" in {
			ApplySin(F).dx mustBe(ApplyCos(F) * Fdx)
		}
	}

	"toText" should {
		"handle sin(3)" in {
			ApplySin(3).toMathJS mustBe("sin(3)")
		}
	}

}





