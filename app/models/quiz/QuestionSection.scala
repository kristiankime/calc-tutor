package models.quiz

import models.support.HasOrder
import models.user.User
import models.{QuestionId, QuestionSectionId}
import play.twirl.api.Html
import models.quiz.UserConstant.EnhancedHtml

case class QuestionSection(id: QuestionSectionId, questionId: QuestionId, explanationRaw: String, explanationHtml: Html, order: Short) extends HasOrder[QuestionSection] {
  def fixConstants(user: User, userConstants: QuestionUserConstantsFrame) = this.copy(explanationHtml = explanationHtml.fixConstants(user, questionId, userConstants))
}