package com.artclod.collection

import org.junit.runner.RunWith
import org.scalatestplus.play._
import play.api.test.Helpers._
import play.api.test._
import com.artclod.mathml._
import com.artclod.mathml.scalar._
import org.scalatest.junit.JUnitRunner

// LATER try out http://rlegendi.github.io/specs2-runner/ and remove RunWith
@RunWith(classOf[JUnitRunner])
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
}