package com.artclod.markup

import laika.api.Transform
import laika.parse.markdown.{Markdown => MD}
import laika.render.HTML
import play.twirl.api.Html

object Markdowner {

  def string(text: String) = Transform from MD.strict to HTML fromString text toString

  def html(text: String) = Html(Transform from MD.strict to HTML fromString text toString)

}
