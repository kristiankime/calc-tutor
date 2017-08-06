package com.artclod.mathml

import org.junit.runner.RunWith
import org.scalatestplus.play._
import play.api.test.Helpers._
import com.artclod.mathml.Match._
import com.artclod.mathml.scalar._
import com.artclod.mathml.scalar.apply.{ApplyLn => ln, ApplyLog => log, ApplyRoot => rt, _}
import org.junit.runner.RunWith
import com.artclod.mathml.scalar._
import org.scalatest.junit.JUnitRunner

// LATER try out http://rlegendi.github.io/specs2-runner/ and remove RunWith
@RunWith(classOf[JUnitRunner])
class MathMLRangeSpec extends PlaySpec {

	"percentInRange" should {

    "return 100% if all values are in of range" in {
			MathMLRange.percentInRange("x", x, -5d, 5d, List(-4, -3, -2, -1, 0, 1, 2, 3, 4, 5)) mustBe(1d)
    }

		"return 0% if no values are out of range" in {
			MathMLRange.percentInRange("x", x, -50d, -25d, List(-4, -3, -2, -1, 0, 1, 2, 3, 4, 5)) mustBe(0d)
		}

		"return 50% if half of the values are out of range" in {
			MathMLRange.percentInRange("x", x, -2d, 2d, List(-4, -3, -2, -1, 0, 1, 2, 3, 4, 5)) mustBe(.5d)
		}

		"treat odd values (inf, NaN) as out of range" in {
			MathMLRange.percentInRange("x", x, -10d, 10d, List(Double.NegativeInfinity, Double.NaN, 0, 1, Double.PositiveInfinity)) mustBe(.4d)
		}

	}

}