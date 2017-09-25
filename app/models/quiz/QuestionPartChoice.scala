package models.quiz

import com.artclod.slick.NumericBoolean
import models.support.HasOrder
import models.{QuestionPartId, QuestionId, QuestionSectionId}
import play.twirl.api.Html

case class QuestionPartChoice(id: QuestionPartId, sectionId: QuestionSectionId, questionId: QuestionId, summaryRaw: String, summaryHtml: Html, correctChoice: Short, order: Short) extends HasOrder[QuestionPartChoice] {
  def correct = NumericBoolean(correctChoice)
}