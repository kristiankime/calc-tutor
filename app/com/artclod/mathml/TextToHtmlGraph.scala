package com.artclod.mathml

import com.artclod.play.JsResultsError
import com.google.common.annotations.VisibleForTesting
import play.api.libs.json.{Format, JsValue, Json}
import views.html
import com.artclod.util.TryUtil.{EitherPimp, TryPimp}
import com.artclod.play.JsResultPimp
import play.twirl.api.Html

import scala.collection.mutable.ArrayBuffer
import scala.util.Try

object TextToHtmlGraph {
  implicit val formatDerivativeDifficultyRequest : Format[TextToHtmlGraph] = Json.format[TextToHtmlGraph]

  @VisibleForTesting
  def from(text: String): Try[TextToHtmlGraph] = {
    val jsTry = Try(Json.parse(text))
    jsTry.flatMap( js => { JsResultPimp(formatDerivativeDifficultyRequest.reads(js)).toTry } )
  }

  def parse(text: String) : Try[Html] = {
    val objOp: Try[TextToHtmlGraph] = from(text)
    objOp.map(v => toHtml(v))
  }

  def toHtml(v: TextToHtmlGraph): Html = {
    throw new UnsupportedOperationException("Not coded yet")
//    views.html.mathml.graph(v.name, v.function, Some(v.title), v.glider.getOrElse(true), v.xSize.getOrElse(300), v.ySize.getOrElse(300), Some(v.name))
  }

  def replaceGraph(text: String) : String = {
    val gs = """\$g\$(.*?)\$g\$""".r
    val ret = gs.replaceSomeIn(text, m => {
      val main = m.group(1)
      val mainCleanup = main.replaceAll("&quot;", "\"")
      val toReplace = "{ " + mainCleanup + " }"
      val op = parse(toReplace).toOption.map(_.toString)
      op
      })
    ret
  }

  def replaceGraph(html: Html) : Html = Html(replaceGraph(html.toString()))


  // ========
  val graphIndicators = """$g$"""
  val graphInficatorsLiteral = java.util.regex.Pattern.quote(graphIndicators)

  def sideGraphs(text: String) : (String, List[Html]) = {
    val gs = (graphInficatorsLiteral + """(.*?)""" + graphInficatorsLiteral).r

    val sideGraphs = ArrayBuffer[Html]()

    val ret = gs.replaceSomeIn(text, m => {
      val main = m.group(1)
      val mainCleanup = main.replaceAll("&quot;", "\"")
      val toReplace = "{ " + mainCleanup + " }"
      val op = from(toReplace).toOption

      for(o <- op) {
        sideGraphs += toHtml(o)
      }
      op.map(v => "<a href=\"#" + v.name + "\">" +  v.name + "</a>")
    })

    (ret, sideGraphs.toList)
  }

  def sideGraphs(html: Html) : Html = {
    val mainAndGraphs = sideGraphs(html.toString())
    throw new UnsupportedOperationException("Not coded yet")
//    views.html.quiz.multiplechoice.graphsOnSide(Html(mainAndGraphs._1), mainAndGraphs._2)
  }

}

case class TextToHtmlGraph(title: String, function: String = "0", glider: Option[Boolean] = None, xSize : Option[Int] = None, ySize : Option[Int] = None) {
  def name = title.replaceAll("""\s+""", "_")
}
