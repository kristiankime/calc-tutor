package com.artclod.markup

import com.artclod.mathml.TextToHtmlGraph
import laika.api.Transform
import laika.parse.markdown.Markdown
import laika.render.HTML
import play.twirl.api.Html

import scala.collection.mutable
import scala.util.Try

object LaikaParser {
  val gt = TextToHtmlGraph.graphIndicators
  val mt = """$$"""


  def parity(parity : Int, index: Int ) = (index % 2) == parity

  /**
    * Takes a string where section of the string are bounded by special characters and returns
    * (1) The text with those section reaplace with an id string (eg $12$ for the 12th one replaced)
    * (2) A map that maps those Ids back to their original text
    *
    * @param text the text to process.
    */
  def replaceSpecials(text: String, specialString: String) = {
    val startsWithSpecial = text.startsWith(specialString)
    val endsWithSpecial = text.startsWith(specialString)

    val split = text.split(java.util.regex.Pattern.quote(specialString))

    val specialMap = mutable.HashMap[String, String]()

    val processed = for(i <- 0 until split.size) yield {
      if(i % 2 == 0) {
        split(i)
      } else  {
        val id = specialString + (i/2) + specialString
        specialMap.put(id, specialString + split(i) + specialString)
        id
      }
    }

    (processed.mkString(""),  specialMap.toMap)
  }

  def inlineSpecial(text: String, inline: Map[String, String]) =  {
    var temp = text
    for(entry <- inline) {
      temp = temp.replace(entry._1, entry._2)
    }
    temp
  }

  def apply(textIn: String) = {
    val (textGt, mapGt) = replaceSpecials(textIn, gt)
    val (text, mapMt) = replaceSpecials(textGt, mt)

    Try(Html( {
      val start = (Transform from Markdown.strict to HTML fromString text toString)
      inlineSpecial(inlineSpecial(start, mapMt), mapGt)
    }))
//    Try(Html(Transform from Markdown.strict to HTML fromString text toString()))
  }

}
