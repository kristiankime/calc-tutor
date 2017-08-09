package com.artclod.math

import org.junit.runner._
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatestplus.play._
import play.api.test.Helpers._

import scala.math.{Pi => π}
import scala.util.Success

class DoubleParseSpec extends PlaySpec {

  "DoubleParse.apply(String)" should {
    "parse inf" in { DoubleParse("inf") mustEqual( Success(Double.PositiveInfinity)) }
    "parse Inf" in { DoubleParse("Inf") mustEqual( Success(Double.PositiveInfinity)) }
    "parse infinity" in { DoubleParse("infinity") mustEqual( Success(Double.PositiveInfinity)) }
    "parse positive infinity" in { DoubleParse("positive infinity") mustEqual( Success(Double.PositiveInfinity)) }
    "parse pos inf" in { DoubleParse("pos inf") mustEqual( Success(Double.PositiveInfinity)) }
    "parse +inf" in { DoubleParse("+inf") mustEqual( Success(Double.PositiveInfinity)) }

    "parse negative infinity" in { DoubleParse("negative infinity") mustEqual( Success(Double.NegativeInfinity)) }
    "parse neg inf" in { DoubleParse("neg inf") mustEqual( Success(Double.NegativeInfinity)) }
    "parse -inf" in { DoubleParse("-inf") mustEqual( Success(Double.NegativeInfinity)) }

    "parse 3" in { DoubleParse("3") mustEqual( Success(3d)) }
    "parse 3.1" in { DoubleParse("3.1") mustEqual( Success(3.1d)) }
  }

}
