package com.artclod.math

import org.scalatestplus.play._

import scala.util.Success

class SequenceParseSpec extends PlaySpec {

  "SequenceParse.apply(String)" should {
      "parse 1;2;3" in { SequenceParse("1;2;3") mustEqual(Success(Vector(1,2,3))) }

//    "parse inf" in { DoubleParse("inf") mustEqual( Success(Double.PositiveInfinity)) }
//    "parse Inf" in { DoubleParse("Inf") mustEqual( Success(Double.PositiveInfinity)) }
//    "parse infinity" in { DoubleParse("infinity") mustEqual( Success(Double.PositiveInfinity)) }
//    "parse positive infinity" in { DoubleParse("positive infinity") mustEqual( Success(Double.PositiveInfinity)) }
//    "parse pos inf" in { DoubleParse("pos inf") mustEqual( Success(Double.PositiveInfinity)) }
//    "parse +inf" in { DoubleParse("+inf") mustEqual( Success(Double.PositiveInfinity)) }
//
//    "parse negative infinity" in { DoubleParse("negative infinity") mustEqual( Success(Double.NegativeInfinity)) }
//    "parse neg inf" in { DoubleParse("neg inf") mustEqual( Success(Double.NegativeInfinity)) }
//    "parse -inf" in { DoubleParse("-inf") mustEqual( Success(Double.NegativeInfinity)) }
//
//    "parse 3" in { DoubleParse("3") mustEqual( Success(3d)) }
//    "parse 3.1" in { DoubleParse("3.1") mustEqual( Success(3.1d)) }
  }

}
