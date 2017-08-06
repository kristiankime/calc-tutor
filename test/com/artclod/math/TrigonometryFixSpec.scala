package com.artclod.math

import scala.math.{Pi => π, _}
import org.junit.runner._
import org.junit.runner.RunWith
import org.scalatestplus.play._
import play.api.test.Helpers._
import play.api.test._
import com.artclod.math.TrigonometryFix._
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TrigonometryFixSpec extends PlaySpec {

  "cos0" should {
    "return  1 for     0" in { cos0(0d)    mustBe(1d) }
    "return  0 for   π/2" in { cos0(π/2)   mustBe(0d) }
    "return -1 for     π" in { cos0(π)     mustBe(-1d) }
    "return  0 for π*3/2" in { cos0(π*3/2) mustBe(0d) }
  }

  "sin0" should {
    "return  0 for     0" in { sin0(0d)    mustBe(0d) }
    "return  1 for   π/2" in { sin0(π/2)   mustBe(1d) }
    "return  0 for     π" in { sin0(π)     mustBe(0d) }
    "return -1 for π*3/2" in { sin0(π*3/2) mustBe(-1d) }
  }

  "tan0" should {
    "return  0 for     0" in { tan0(0d)    mustBe(0d) }
    "return  1 for   π/2" in { tan0(π/2)   mustBe(Double.PositiveInfinity) }
    "return  0 for     π" in { tan0(π)     mustBe(0d) }
    "return -1 for π*3/2" in { tan0(π*3/2) mustBe(Double.PositiveInfinity) }
  }

  "sec0" should {
    "return  0 for     0" in { sec0(0d)    mustBe(1d) }
    "return  1 for   π/2" in { sec0(π/2)   mustBe(Double.PositiveInfinity) }
    "return  0 for     π" in { sec0(π)     mustBe(-1d) }
    "return -1 for π*3/2" in { sec0(π*3/2) mustBe(Double.PositiveInfinity) }
  }
}
