package models.quiz

import com.artclod.mathml.scalar.MathMLElem
import models.support.HasOrder
import models.user.User
import models.{QuestionId, QuestionPartId, QuestionSectionId}
import play.twirl.api.Html
import models.quiz.UserConstant.EnhancedHtml
import models.quiz.UserConstant.EnhancedMathMLElem

case class QuestionPartFunction(id: QuestionPartId, sectionId: QuestionSectionId, questionId: QuestionId, summaryRaw: String, summaryHtml: Html, functionRaw: String, functionMath: MathMLElem, order: Short) extends HasOrder[QuestionPartFunction] {
  def fixConstants(user: User, userConstants: QuestionUserConstantsFrame) = this.copy(
    summaryHtml = summaryHtml.fixConstants(user, userConstants),
    functionMath = functionMath.fixConstants(user, userConstants)
  )
}