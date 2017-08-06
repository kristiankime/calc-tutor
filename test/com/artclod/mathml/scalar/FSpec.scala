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
class FSpec extends PlaySpec {

	"d" should {
		"return zero for wrt ! x" in {
			F d("y") mustBe(`0`)
		}

		"return Fx for x" in {
			F d("x") mustBe(Fdx)
		}
	}

}