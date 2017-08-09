package com.artclod.random

import org.junit.runner.RunWith
import org.scalatestplus.play._
import play.api.test.Helpers._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import scala.util.Random

class RandomSpec extends PlaySpec {

  "pickNFrom" should {

    "return the right number of elements (aka N)" in {
      val picked = pickNFrom(2, new Random(0L))((1 to 10).toVector)
      picked.length mustBe(2)
    }

    "return elements should be from available elements" in {
      val picked = pickNFrom(3, new Random(0L))((1 to 10).toVector)
      picked(0) must (be >= 1 and be <= 10)
      picked(1) must (be >= 1 and be <= 10)
      picked(2) must (be >= 1 and be <= 10)
    }

    "return N elements if N is > the number of available elements" in {
      val picked = pickNFrom(5, new Random(0L))((1 to 2).toVector)
      picked.length mustBe(2)
    }

    "return all the elements if N is = the number of available elements" in {
      val picked = pickNFrom(3, new Random(0L))((1 to 3).toVector)
      picked must contain(1)
      picked must contain(2)
      picked must contain(3)
    }

  }

  "pick2From" should {

    "return 2 elements from the available" in {
      val picked = (1 to 10).toVector.pick2From(new Random(0L))
      picked._1 must (be >= 1 and be <= 10)
      picked._2 must (be >= 1 and be <= 10)
    }

    "error with only 1 element" in {
      a [IllegalArgumentException] must be thrownBy { Vector(1).pick2From(new Random(0L)) }
    }
  }

}
