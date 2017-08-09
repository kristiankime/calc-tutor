package com.artclod.mathml.scalar.concept

import org.junit.runner.RunWith
import org.scalatestplus.play._
import play.api.test.Helpers._
import org.junit.runner.RunWith
import play.api.test._
import play.api.test.Helpers._
import com.artclod.mathml._
import com.artclod.mathml.scalar._
import com.artclod.mathml.scalar.apply._
import org.scalatest.junit.JUnitRunner

class UnaryFunctionSpec extends PlaySpec {

	"variables" should {
		"return empty for constant internals" in {
			DummyUnaryFunction(`1`).variables mustBe(Set())
		}

		"return the variable if it has one" in {
			DummyUnaryFunction(x).variables mustBe(Set("x"))
		}
	}

}