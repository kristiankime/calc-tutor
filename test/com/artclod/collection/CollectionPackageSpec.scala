package com.artclod.collection

import org.junit.runner.RunWith
import org.scalatestplus.play._
import play.api.test.Helpers._
import play.api.test._
import com.artclod.mathml._
import com.artclod.mathml.scalar._
import org.scalatest.junit.JUnitRunner

class CollectionPackageSpec extends PlaySpec {

	"elementAfter" should {
		"should return next element" in {
      List("A", "B", "C").elementAfter("B") mustEqual(Some("C"))
		}

		"should return None if element is the last element" in {
			List("A", "B", "C").elementAfter("C") mustEqual(None)
		}
		
		"should return None if element is not in the list" in {
			List("A", "B", "C").elementAfter("Not in the list") mustEqual(None)
		}
	}

  "elementBefore" should {
    "should return previous element" in {
      List("A", "B", "C").elementBefore("B") mustEqual(Some("A"))
    }

    "should return None if element is the first element" in {
      List("A", "B", "C").elementBefore("A") mustEqual(None)
    }

    "should return None if element is not in the list" in {
      List("A", "B", "C").elementBefore("Not in the list") mustEqual(None)
    }
  }


  "dropOption" should {
    "should return empty collection if option is None" in {
      val noList : Option[List[String]] = None

      val emptyList = noList.dropOption

      emptyList mustBe empty
    }

    "should return collection if option is Some" in {
      val noList : Option[List[String]] = Some(List("a","b"))

      val emptyList = noList.dropOption

      emptyList mustEqual(List("a","b"))
    }
  }

  "rescaleZero2One" should {
    "should return empty collection if collection is empty" in {
      val emptyList = List[Double]()

      com.artclod.collection.rescaleZero2One(emptyList) mustEqual (Seq())
    }

    "should return half if collection has one element" in {
      val list = List(10d)
      rescaleZero2One(list) mustEqual(Seq(.5d))
    }

    "should rescale with multiple elements" in {
      val list = List(0d, 4d, 2d, 4d)
      rescaleZero2One(list) mustEqual(Seq(0d, 1d, .5d, 1d))
    }
  }
}