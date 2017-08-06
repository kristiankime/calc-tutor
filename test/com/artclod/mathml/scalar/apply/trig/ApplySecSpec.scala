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
class ApplySecSpec extends PlaySpec {

	"eval" should {
		"do csc" in {
			ApplySec(π).eval().get mustBe(Trigonometry.sec(math.Pi))
		}
	}

	"c" should {
		"return correct sec" in {
			ApplySec(π).c.get mustBe(Cn(Trigonometry.sec(math.Pi)))
		}

		"fail if not a constant " in {
			ApplySec(x).c mustBe empty
		}
	}

	"s" should {
		"simplify what can be simpified" in {
			ApplySec(NeedsSimp).s mustBe(ApplySec(Simplified))
		}

		"remain unchanged if nothing can be simplified" in {
			ApplySec(x).s mustBe(ApplySec(x))
		}
	}

	"d" should {
		"obey the derivative rule: sec(f)' = tan(f) sec(f) f'" in {
			ApplySec(F).dx mustBe((ApplyTan(F) * ApplySec(F) * Fdx)s)
		}
	}

	"toText" should {
		"handle sec(3)" in {
			ApplySec(3).toMathJS mustBe("sec(3)")
		}
	}

}
