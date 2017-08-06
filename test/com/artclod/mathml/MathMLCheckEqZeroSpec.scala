package com.artclod.mathml

import org.junit.runner.RunWith
import org.scalatestplus.play._
import play.api.test.Helpers._
import com.artclod.mathml.Match._
import com.artclod.mathml.scalar._
import com.artclod.mathml.scalar.apply.trig.{ApplyCos => cos, ApplyCot => cot, ApplyCsc => csc, ApplySec => sec, ApplySin => sin, ApplyTan => tan}
import com.artclod.mathml.scalar.apply.{ApplyLn => ln, ApplyLog => log}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

// LATER try out http://rlegendi.github.io/specs2-runner/ and remove RunWith
@RunWith(classOf[JUnitRunner])
class MathMLCheckEqZeroSpec extends PlaySpec {

	"Checking equality when functions evaluate to zero " should {

    "confirm log(2^x, 2) = x" in {
      (log(2, `2`^x) ?= x) mustBe(Yes)
    }

    "confirm log(2^x, 2) - x = 0" in {
      ((log(2, `2`^x) - x) ?= `0`) mustBe(Yes)
    }

    "confirm log(10^x, 10) - x = 0" in {
      ((log(10, `10`^x) - x) ?= `0`) mustBe(Yes)
    }

    "confirm log(2.5^x, 2.5) - x = 0" in {
      ((log(2.5, `2.5`^x) - x) ?= `0`) mustBe(Yes)
    }

    "confirm log(2^x, 2) - x != 0.0000000001 (ensure non zero doesn't equal zero)" in {
      ((log(2, `2`^x) - x) ?= Cn(0.0000000001)) mustBe(No)
    }
		
	}

}