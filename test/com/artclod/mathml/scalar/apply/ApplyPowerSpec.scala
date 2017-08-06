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

// LATER try out http://rlegendi.github.io/specs2/runner/ and remove RunWith
@RunWith(classOf[JUnitRunner])
class ApplyPowerSpec extends PlaySpec {

	"eval" should {
		"raise value to power" in {
			ApplyPower(`2`, `3`).eval().get mustBe(8)
		}
		
		"return 0 if base is 0" in {
			ApplyPower(`0`, e).eval().get mustBe(0)
		}
		
		"fail if base is nonzero and power results is 0" in {
			ApplyPower(e, `-1000`).eval().isFailure mustBe(true)
		}
	}

	"variables" should {
		"be empty if element is constant" in {
			ApplyPower(`1`, `2`).variables mustBe empty
		}

		"be x if element constains an x" in {
			ApplyPower(x, `2`).variables mustBe(Set("x"))
		}

		"be y if element constains a y" in {
			ApplyPower(y, `2`).variables mustBe(Set("y"))
		}

		"be x & y if element constains x & y" in {
			ApplyPower(x, y).variables mustBe(Set("x", "y"))
		}
	}
	
	"c" should {
		"return 1 if base is 1" in {
			ApplyPower(`1`, x).c.get mustBe(`1`)
		}

		"return base if power is 1" in {
			ApplyPower(`5`, `1`).c.get mustBe(`5`)
		}

		"return 1 if power is 0" in {
			ApplyPower(x, `0`).c.get mustBe(`1`)
		}

		"return None if function is not constant" in {
			ApplyPower(`2`, x).c mustBe empty
		}
	}

	"s" should {
		"return 1 if base is 1" in {
			ApplyPower(`1`, x).s mustBe(`1`)
		}

		"return 1 if exponent is 0" in {
			ApplyPower(x, `0`).s mustBe(`1`)
		}

		"return base if exponent is 1" in {
			ApplyPower(x, `1`).s mustBe(x)
		}
	}

	"d" should {
		"obey the elementary power rule: (x^n)' = n*x^(n-1)" in {
			ApplyPower(x, `3`).dx mustBe(`3` * (x ^ `2`))
		}

		"obey the chain power rule: (f^n)' = n*f^(n-1)*f'" in {
			ApplyPower(F, `3`).dx mustBe(ApplyTimes(`3`, F ^ `2`, Fdx))
		}

		// (f^g)' = f^(g-1) * (g f'+f log(f) g')
		"obey the generalized power rule: (f^g)' =  f^(g-1)    * (g * f'  + f * log(f)     * g')" in {
      ApplyPower(F, G).dx mustBe(   ((F^(G-`1`)) * (G * Fdx + F * ApplyLn(F) * Gdx))s )
		}
	}

	"toText" should {
		"handle 3 ^ 5" in {
			ApplyPower(3, 5).toMathJS mustBe("(3 ^ 5)")
		}
	}

}