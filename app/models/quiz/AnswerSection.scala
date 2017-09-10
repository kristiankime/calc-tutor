package models.quiz

import com.artclod.slick.NumericBoolean
import models.support.HasOrder
import models.{AnswerId, AnswerSectionId, QuestionId, QuestionSectionId}
import play.twirl.api.Html

case class AnswerSection(id: AnswerSectionId, answerId: AnswerId, sectionId : QuestionSectionId, questionId: QuestionId, choice: Option[Short], correctNum: Short, order: Short) extends HasOrder[AnswerSection] {
  def correct : Boolean = NumericBoolean(correctNum)
}