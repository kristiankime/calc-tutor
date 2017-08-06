package com.artclod.mathml

import org.junit.runner.RunWith
import org.scalatestplus.play._
import play.api.test.Helpers._
import org.junit.runner.RunWith
import scala.xml._
import play.api.test._
import play.api.test.Helpers._
import com.artclod.mathml.scalar._
import com.artclod.mathml.scalar.apply._
import com.artclod.mathml.scalar.apply.{ApplyLn => ln}
import com.artclod.mathml.scalar.apply.{ApplyLog => log}
import com.artclod.mathml.Match._
import org.scalatest.junit.JUnitRunner

// LATER try out http://rlegendi.github.io/specs2-runner/ and remove RunWith
@RunWith(classOf[JUnitRunner])
class MathMLCheckEqExponentialSpec extends PlaySpec {

	"Checking equality between symbolic differentiation and manual derivative " should {

		"confirm e ^ x' = e ^ x" in {
			val f = (e ^ x) dx
			val g = e ^ x
			(f ?= g) mustBe(Yes)
		}

		"confirm e ^ 10*x' = 10 * e ^ 10*x" in {
			val f = (e ^ (`10` * x)) dx
			val g = `10` * (e ^ (`10` * x))
			(f ?= g) mustBe(Yes)
		}

		"confirm e ^ 10*x' != 11* e ^ 10*x" in {
			val f = (e ^ (`10` * x)) dx
			val g = `11` * (e ^ (`10` * x))
			(f ?= g) mustBe(No)
		}

		"confirm e ^ 100*x' = 100 * e ^ 100*x" in {
			val f = (e ^ (`100` * x)) dx
			val g = `100` * (e ^ (`100` * x))
			(f ?= g) mustBe(Yes)
		}

		"confirm e ^ 100*x' != 101* e ^ 100*x" in {
			val f = (e ^ (`100` * x)) dx
			val g = `101` * (e ^ (`100` * x))
			(f ?= g) mustBe(No)
		}

		"confirm e ^ 1000*x' = 1000 * e ^ 1000*x" in {
			val f = (e ^ (`1000` * x)) dx
			val g = `1000` * (e ^ (`1000` * x))
			(f ?= g) mustBe(Yes)
		}

		"confirm e ^ 1000*x' != 1001* e ^ 1000*x" in {
			val f = (e ^ (`1000` * x)) dx
			val g = `1001` * (e ^ (`1000` * x))
			(f ?= g) mustBe(No)
		}

		"confirm e ^ -100*x' = -100 * e ^ -100*x" in {
			val f = (e ^ (`-100` * x)) dx
			val g = `-100` * (e ^ (`-100` * x))
			(f ?= g) mustBe(Yes)
		}

		"confirm e ^ -100*x' != -101 * e ^ -100*x" in {
			val f = (e ^ (`-100` * x)) dx
			val g = `-101` * (e ^ (`-100` * x))
			(f ?= g) mustBe(No)
		}
	}

}