package com.artclod.mathml.scalar.apply

import org.junit.runner.RunWith
import org.scalatestplus.play._
import play.api.test.Helpers._
import org.junit.runner.RunWith
import play.api.test._
import play.api.test.Helpers._
import com.artclod.mathml._
import com.artclod.mathml.scalar._
import org.scalatest.junit.JUnitRunner

class ApplyTimesSpec extends PlaySpec {

	"eval" should {
		"multiply two numbers" in {
			ApplyTimes(`3`, `-2`).eval().get mustBe(-6)
		}

		"multiply many numbers" in {
			ApplyTimes(`3`, `2`, `4`).eval().get mustBe(24)
		}
		
		"return 0 if any numbers are 0" in {
			ApplyTimes(`.5`, `2`, `0`).eval().get mustBe(0)
		}
		
		"fail if non zero numbers produce a 0 output" in {
			ApplyTimes(Cn(1E-300), Cn(1E-300)).eval().isFailure mustBe(true)
		}
	}

	"variables" should {
		"be empty if element is constant" in {
			ApplyTimes(`1`, `2`).variables mustBe empty
		}

		"be x if element constains an x" in {
			ApplyTimes(x, `2`).variables mustBe(Set("x"))
		}

		"be y if element constains a y" in {
			ApplyTimes(y, `2`).variables mustBe(Set("y"))
		}

		"be x & y if element constains x & y" in {
			ApplyTimes(x, y).variables mustBe(Set("x", "y"))
		}
	}

	"c" should {
		"return 0 if any value is zero" in {
			ApplyTimes(`1`, `0`, x).c.get mustBe(`0`)
		}

		"return multiplication of values if possible" in {
			ApplyTimes(`4`, `2`, `1`).c.get mustBe(`8`)
		}

		"return none if values do not multiply to a constant" in {
			ApplyTimes(`4`, x).c.isEmpty mustBe(true)
		}
	}

	"s" should {
		"return 0 if isZero is true" in {
			ApplyTimes(`1`, `0`, `1`).s mustBe(`0`)
		}

		"return 0 if any value is zero" in {
			ApplyTimes(`1`, `0`, x).s mustBe(`0`)
		}

		"return 1 if isOne is true" in {
			ApplyTimes(`1`, `1`, `1`).s mustBe(`1`)
		}

		"multiple any constanst together" in {
			ApplyTimes(`4`, `1`, `3`).s mustBe(Cn(12))
		}

		"remove 1s in a sequence" in {
			ApplyTimes(`1`, `3`, x).s mustBe(`3` * x)
		}

		"remove 1s" in {
			ApplyTimes(`1`, x).s mustBe(x)
		}

		"multiply constants and leave variables, with nested elements  (constands go to end)" in {
			ApplyTimes(x, `4`, y, (`2` * `3`)).s mustBe(ApplyTimes(`24`, x, y))
		}

		"remain unchanged if nothing can be simplified" in {
			ApplyTimes(`3`, x).s mustBe(ApplyTimes(`3`, x))
		}
	}

	"d" should {
		"obey the product rule: (f g)' = f'g + fg'" in {
			ApplyTimes(F, G).dx mustBe(Fdx * G + F * Gdx)
		}

		// (f(x) g(x) h(x))' = g(x)h(x)f'(x) + f(x)h(x)g'(x) + f(x)g(x)h'(x)
		"obey the multiple product rule: (fgh)' = f'gh + fg'h + fgh'" in {
			ApplyTimes(F, G, H).dx mustBe(ApplyPlus(ApplyTimes(Fdx, G, H), ApplyTimes(F, Gdx, H), ApplyTimes(F, G, Hdx)))
		}
	}

	"toText" should {
		"handle 3 * 5" in {
			ApplyTimes(3, 5).toMathJS mustBe("(3 * 5)")
		}
	}

}