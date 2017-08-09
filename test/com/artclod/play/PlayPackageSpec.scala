package com.artclod.play

import org.junit.runner.RunWith
import org.scalatestplus.play._
import play.api.test.Helpers._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

class PlayPackageSpec extends PlaySpec {

//  import com.artclod.play.collapse

	"collapse" should {
		"leave seq as is if there no duplicates" in {
      collapse(Seq('a -> "a", 'b -> "b", 'c -> "c")) mustBe(Seq('a -> "a", 'b -> "b", 'c -> "c"))
		}

    "collapse duplicates in order of first appearance" in {
      collapse(Seq('a -> "a", 'b -> "b", 'c -> "c", 'a -> "+")) mustBe(Seq('a -> "a +", 'b -> "b", 'c -> "c"))
    }

    "collapse multiple duplicates in order of first appearance" in {
      collapse(Seq('a -> "a", 'b -> "b", 'b -> "@", 'c -> "c", 'a -> "+", 'c -> "!")) mustBe(Seq('a -> "a +", 'b -> "b @", 'c -> "c !"))
    }
	}

}