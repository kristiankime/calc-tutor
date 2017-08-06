package com.artclod.xml

import org.junit.runner.RunWith
import org.scalatestplus.play._
import play.api.test.Helpers._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

// LATER try out http://rlegendi.github.io/specs2-runner/ and remove RunWith
@RunWith(classOf[JUnitRunner])
class NodesSpec extends PlaySpec {

	"nodeCount" should {

		"be 1 for a single element" in {
			Nodes.nodeCount(<a></a>) mustBe(1)
		}

		"be 3 for an element with two children" in {
			Nodes.nodeCount(<top><a/><b/></top>) mustBe(3)
		}

		"be 4 for an element with two children, one of which has a child" in {
			Nodes.nodeCount(<top><a><asub/></a><b/></top>) mustBe(4)
		}
	}

}