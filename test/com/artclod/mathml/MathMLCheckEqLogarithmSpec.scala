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

class MathMLCheckEqLogarithmSpec extends PlaySpec {

	"Checking equality between symbolic differentiation and manual derivative " should {
				
		"confirm ln(x)' = 1 / x" in {
			val f = ln(x) dx
			val g = `1` / x
			(f ?= g) mustBe(Yes)
		}

		"confirm log(4, x)' = 1 / (x ln(4))" in {
			val f = log(4, x) dx
			val g = `1` / (x * ln(`4`))
			(f ?= g) mustBe(Yes)
		}
		
		"confirm 1 / ln(x)' = -1 / (x * log(x)^2)" in {
			val f = (`1` / ln(x)) dx
			val g = `-1` / (x * (ln(x) ^ `2`))
			(f ?= g) mustBe(Yes)
		}

		"confirm x / ln(x)' = (ln(x)-1) / (ln(x)^2)" in {
			val f = (x / ln(x)) dx
			val g = (ln(x) - `1`) / (ln(x) ^ `2`)
			(f ?= g) mustBe(Yes)
		}

		"confirm (x/(x+ln(x)))' = (ln(x)-1) / (x+ln(x))^2" in {
			val f = x / (x + ln(x)) dx
			val g = (ln(x) - `1`) / ((x + ln(x)) ^ `2`)
			(f ?= g) mustBe(Yes)
		}

		"confirm (x/(x+log(x)))' = (ln(10)*(ln(x) - 1))/(x*ln(10)+ln(x))^2" in {
			val f = (x / (x + log(x))) dx
			val g = (ln(`10`) * (ln(x) - `1`)) / ((x * ln(`10`) + ln(x)) ^ `2`)
			(f ?= g) mustBe(Yes)
		}

	}

}