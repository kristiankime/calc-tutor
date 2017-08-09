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

class ApplyMinusBSpec extends PlaySpec {

	"eval" should {
		"subtract numbers" in {
			ApplyMinusB(`8`, `6`).eval().get mustBe(2)
		}
	}
	
	"variables" should {
		"be empty if element is constant" in {
			ApplyMinusB(`1`, `2`).variables mustBe empty
		}

		"be x if element constains an x" in {
			ApplyMinusB(x, `2`).variables mustBe(Set("x"))
		}

		"be y if element constains a y" in {
			ApplyMinusB(y, `2`).variables mustBe(Set("y"))
		}

		"be x & y if element constains x & y" in {
			ApplyMinusB(x, y).variables mustBe(Set("x", "y"))
		}
	}

	"c" should {
		"return subtraction if values are numbers " in {
			ApplyMinusB(`6`, `4`).c.get mustBe(`2`)
		}

		"return 0 if values are equal " in {
			ApplyMinusB(`5`, `5`).c.get mustBe(`0`)
		}

		"fail if not a constant " in {
			ApplyMinusB(x, `5`).c mustBe empty
		}
	}

	"s" should {
		"return 0 if numbers are the same" in {
			ApplyMinusB(`1`, `1`).s mustBe(`0`)
		}

		"return 1 if numbers subtract to 1" in {
			ApplyMinusB(`3`, `2`).s mustBe(`1`)
		}

		"return first value if second value is 0" in {
			ApplyMinusB(Cn(4), `0`).s mustBe(`4`)
		}

		"return minus second value if first value is 0" in {
			ApplyMinusB(`0`, `3`).s mustBe(`-3`)
		}

		"remain unchanged if nothing can be simplified" in {
			ApplyMinusB(`3`, x).s mustBe(`3` - x)
		}
	}

	"d" should {
		"obey the subtraction rule: (f - g)' = f' - g'" in {
			ApplyMinusB(F, G).dx mustBe(Fdx - Gdx)
		}
	}

	"toText" should {
		"handle 3 - 5" in {
			ApplyMinusB(3, 5).toMathJS mustBe("(3 - 5)")
		}
	}

}