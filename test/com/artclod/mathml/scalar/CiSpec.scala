package com.artclod.mathml.scalar

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
class CiSpec extends PlaySpec {

	"Indentifer.name" should {
		"be the same regardless of whitespace" in {
			Ci(" x   ").identifier.name == "x" mustBe(true)
		}
	}
	
	"Ci" should {
		"be the same regardless of whitespace with a string input" in {
			Ci(" x   ") == Ci("x") mustBe(true)
		}
	}

	"isZero" should {
		"return false" in {
			Ci("x").isZero mustBe(false)
		}
	}

	"isOne" should {
		"return false" in {
			Ci("x").isOne mustBe(false)
		}
	}

	"simplify" should {
		"return value unchanged" in {
			Ci("x").simplifyStep mustBe(Ci("x"))
		}
	}

	"derivative" should {
		"return one if wrt same variable" in {
			Ci("x").derivative("x") mustBe(Cn(1))
		}

		"return zero if wrt different variable" in {
			Ci("x").derivative("Y") mustBe(Cn(0))
		}
		
		"return zero if variable is different case" in {
			Ci("x").derivative("X") mustBe(Cn(0))
		}
	}
}