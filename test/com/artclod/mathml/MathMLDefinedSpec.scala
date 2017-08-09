package com.artclod.mathml

import org.junit.runner.RunWith
import org.scalatestplus.play._
import play.api.test.Helpers._
import com.artclod.mathml.scalar._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

class MathMLDefinedSpec extends PlaySpec {

  "isDefinedAt" should {
    "return true if well defined" in {
      MathMLDefined.isDefinedAt(`1` / x, "x" -> 1) mustBe(true)
    }

    "return false if pos infinity" in {
      MathMLDefined.isDefinedAt(`1` / x, "x" -> 0) mustBe(false)
    }

    "return false if neg infinity" in {
      MathMLDefined.isDefinedAt(`-1` / x, "x" -> 0) mustBe(false)
    }

    "return false if not defined" in {
      MathMLDefined.isDefinedAt(`0` / x, "x" -> 0) mustBe(false)
    }

    "return false on Failure" in {
      MathMLDefined.isDefinedAt(e ^ x, "x" -> Double.MinValue) mustBe(false)
    }

    "x^(-2/3) @ 0 is not defined" in {
      MathMLDefined.isDefinedAt(x ^ (`-2` / `3`), "x" -> 0) mustBe(false)
    }

    "x^(1/3)' @ 0 is not defined" in {
      val f = x ^ (`1` / `3`)
      MathMLDefined.isDefinedAt(f dx, "x" -> 0) mustBe(false)
    }

    "tan(x/2)' @ pi is not defined" in {
      MathMLDefined.isDefinedAt(  (sec(x / 2) ^ 2) * (`1` / `2`), "x" -> math.Pi) mustBe(false)
    }
  }

  "isDefinedFor" should {
    "return true for x (for 100%)" in {
      MathMLDefined.isDefinedFor( x , 1.00d) mustBe(true)
    }

    "return true for 1/x (for 99%)" in {
      MathMLDefined.isDefinedFor( `1` / x , 0.99d) mustBe(true)
    }

    "return true for ln(x) (for 40%)" in {
      MathMLDefined.isDefinedFor( ln(x) , 0.40d) mustBe(true)
    }

    "return false for ln(x) (for 60%)" in {
      MathMLDefined.isDefinedFor( ln(x) , 0.60d) mustBe(false)
    }
  }

}
