package com.artclod.slick

import org.junit.runner.RunWith
import org.scalatestplus.play._
import play.api.test.Helpers._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

class PackageSpec extends PlaySpec {

  "listGroupBy" should {

    "return empty if input is empty" in {
      val grouped = listGroupBy(List[(Int, String)]())(_._1, _._2)

      grouped.length mustBe(0)
    }

    "return groups by key function" in {
      val grouped = listGroupBy(List((1, "a"), (1, "b"), (2, "foo")))(_._1, _._2)

      grouped mustBe(List( ListGroup(1, List("a", "b")), ListGroup(2, List("foo")))
      )
    }

  }

}
