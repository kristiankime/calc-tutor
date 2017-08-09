package com.artclod.mathml

import org.junit.runner.RunWith
import org.scalatestplus.play._
import play.api.test.Helpers._
import org.junit.runner.RunWith
//import views.html.tag.name
import com.artclod.mathml.{TextToHtmlGraph => m}
import org.scalatest.junit.JUnitRunner

class TextToHtmlGraphSpec extends PlaySpec {

  "objTry" should {

    "parse text into object" in {
      val objTry = TextToHtmlGraph.from( """{ "title": "name", "function": "x+2", "titleOp" : "title", "glider": false, "xSize": 301, "ySize": 302 }""")
      objTry.get mustBe( TextToHtmlGraph("name", "x+2", Some(false), Some(301), Some(302)) )
    }

    "parse text into object (does not require optional fields)" in {
      val objTry = TextToHtmlGraph.from( """{ "title": "name", "function": "x+1" }""")
      objTry.get mustBe( TextToHtmlGraph("name", "x+1") )
    }

  }

//  "replace" should {
//
//    "replace $g$ xxx $g$ with html graph" in {
//      val replaced = TextToHtmlGraph.replaceGraph("""Some text $g$ "title": "name", "function": "x^2" $g$ more text""")
//      replaced mustBe("Some text " + views.html.mathml.graph(name = "name", initialFunction = "x^2", titleOp = Some("name"), anchorOp = Some("name")) +  " more text")
//    }
//
//  }

}
