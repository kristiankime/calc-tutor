package models.quiz

import com.artclod.slick.NumericBoolean
import models.support.HasOrder
import models.{AnswerId, AnswerSectionId, QuestionId, SectionId}
import play.twirl.api.Html

case class AnswerSection(id: AnswerSectionId, answerId: AnswerId, sectionId : SectionId, questionId: QuestionId, correctNum: Short, order: Short) extends HasOrder[AnswerSection] {
  def correct : Boolean = NumericBoolean(correctNum)
}