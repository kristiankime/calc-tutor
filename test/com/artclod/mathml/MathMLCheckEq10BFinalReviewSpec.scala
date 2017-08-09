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
import com.artclod.mathml.scalar.apply.{ApplyLn => ln, ApplyLog => log}
import com.artclod.mathml.Match._
import org.scalatest.junit.JUnitRunner

class MathMLCheckEq10BFinalReviewSpec extends PlaySpec {

	"Checking answers for math10b final review " should {

		"confirm (1/(2x+3)^5)' = (-10)/(2*x+3)^6 " in {
			val f = (`1` / (`2` * x + `3`) ^ `5`) dx
			val g = (`-10`) / ((`2` * x + `3`) ^ `6`)
			(f ?= g) mustBe(Yes)
		}

		"confirm (1/(2x+3)^5)' = (-10)/((2*x+3)^6) " in {
			val f = (`1` / (`2` * x + `3`) ^ `5`) dx
			val g = (`-10`) / ((`2` * x + `3`) ^ `6`)
			(f ?= g) mustBe(Yes)
		}
	}

}