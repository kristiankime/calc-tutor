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
class ApplyTanSpec extends PlaySpec {

	"eval" should {
		"do sin" in {
			ApplyTan(π).eval().get mustBe(Trigonometry.tan(math.Pi))
		}
	}

	"c" should {
		"return correct tan" in {
			ApplyTan(π).c.get mustBe(Cn(Trigonometry.tan(math.Pi)))
		}

		"fail if not a constant " in {
			ApplyTan(x).c mustBe empty
		}
	}

	"s" should {
		"simplify what can be simpified" in {
			ApplyTan(NeedsSimp).s mustBe(ApplyTan(Simplified))
		}

		"remain unchanged if nothing can be simplified" in {
			ApplyTan(x).s mustBe(ApplyTan(x))
		}
	}

	"d" should {
		"obey the derivative rule: sin(f)' = cos(f)f'" in {
			ApplyTan(F).dx mustBe((ApplySec(F) ^ `2`) * Fdx)
		}
	}

	"toText" should {
		"handle tan(3)" in {
			ApplyTan(3).toMathJS mustBe("tan(3)")
		}
	}

}





