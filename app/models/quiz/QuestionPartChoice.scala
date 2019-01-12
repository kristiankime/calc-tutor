package models.quiz

import com.artclod.slick.NumericBoolean
import models.support.HasOrder
import models.user.User
import models.{QuestionId, QuestionPartId, QuestionSectionId}
import play.twirl.api.Html
import models.quiz.UserConstant.EnhancedHtml

case class QuestionPartChoice(id: QuestionPartId, sectionId: QuestionSectionId, questionId: QuestionId, summaryRaw: String, summaryHtml: Html, correctChoice: Short, order: Short) extends HasOrder[QuestionPartChoice] {
  def correct = NumericBoolean(correctChoice)

  def fixConstants(user: User, userConstants: QuestionUserConstantsFrame) = this.copy(summaryHtml = summaryHtml.fixConstants(user, questionId, userConstants))
}