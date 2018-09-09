package com.artclod.math

import org.scalatestplus.play._

import scala.util.Success

class SequenceParseSpec extends PlaySpec {

  "SequenceParse.apply(String)" should {
      "parse ; separated integers" in { SequenceParse("1;2;3") mustEqual(Success(Vector(1.0, 2.0, 3.0))) }

      "handle decimal values" in { SequenceParse("1.1;2.2;3.3") mustEqual(Success(Vector(1.1, 2.2, 3.3))) }

      "handle white space" in { SequenceParse("1.1; 2.2; 3.3") mustEqual(Success(Vector(1.1, 2.2, 3.3))) }

      "handle commas" in { SequenceParse("1,000.1; 2,000.2; 3,000.3") mustEqual(Success(Vector(1000.1, 2000.2, 3000.3))) }
  }

}
