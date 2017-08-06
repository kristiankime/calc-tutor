package com.artclod.markup


import org.junit.runner.RunWith
import org.scalatestplus.play._
import play.api.test.Helpers._
import com.artclod.math.DoubleParse
import org.junit.runner._
import org.scalatest.junit.JUnitRunner
import scala.util.Success

@RunWith(classOf[JUnitRunner])
class LaikaParserSpec extends PlaySpec {

  "apply(String)" should {

    "parse plain text into paragraph" in {
      LaikaParser("hello").get.toString() mustEqual("<p>hello</p>")
    }

    "parse markdown" in {
      LaikaParser("# H1").get.toString() mustEqual("\n<h1>H1</h1>")
    }

    "allow ASCII math" in {
      LaikaParser("""function: \`x+2\`""").get.toString() mustEqual("""<p>function: `x+2`</p>""")
    }

    "allow tex math" in {
      LaikaParser("""function: $$x+2$$""").get.toString() mustEqual("""<p>function: $$x+2$$</p>""")
    }
  }

  "replaceSpecials" should {

    "replace first match with 0" in {
      val (replaced, map) = LaikaParser.replaceSpecials("hello $$zero$$ bye", LaikaParser.mt)

      replaced mustEqual("hello $$0$$ bye")
      map mustEqual(Map("$$0$$" -> "$$zero$$"))
    }

    "replace all matches with sequential numbers" in {
      val (replaced, map) = LaikaParser.replaceSpecials("a $$zero$$ b $$one$$ c $$two$$ d", LaikaParser.mt)

      replaced mustEqual("a $$0$$ b $$1$$ c $$2$$ d")
      map mustEqual(Map("$$0$$" -> "$$zero$$", "$$1$$" -> "$$one$$", "$$2$$" -> "$$two$$"))
    }

    "handle the case where the string starts with a special" in {
      val (replaced, map) = LaikaParser.replaceSpecials("$$zero$$ b $$one$$ c $$two$$", LaikaParser.mt)

      replaced mustEqual("$$0$$ b $$1$$ c $$2$$")
      map mustEqual(Map("$$0$$" -> "$$zero$$", "$$1$$" -> "$$one$$", "$$2$$" -> "$$two$$"))
    }

    "handle the case where the string ends with a special" in {
      val (replaced, map) = LaikaParser.replaceSpecials("a $$zero$$ b $$one$$ c $$two$$", LaikaParser.mt)

      replaced mustEqual("a $$0$$ b $$1$$ c $$2$$")
      map mustEqual(Map("$$0$$" -> "$$zero$$", "$$1$$" -> "$$one$$", "$$2$$" -> "$$two$$"))
    }
  }

}
