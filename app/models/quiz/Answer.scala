package models.quiz

import com.artclod.slick.NumericBoolean
import models.{AnswerId, QuestionId, UserId}
import org.joda.time.DateTime
import play.twirl.api.Html

case class Answer(id: AnswerId, ownerId: UserId, questionId: QuestionId, allCorrectNum: Short, creationDate: DateTime) {
  def allCorrect = NumericBoolean(allCorrectNum)
}
