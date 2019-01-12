package models.quiz

import com.artclod.mathml.scalar.MathMLElem
import models.quiz.util.SequenceTokenOrMath
import models.support.HasOrder
import models.user.User
import models.{QuestionId, QuestionPartId, QuestionSectionId}
import play.twirl.api.Html
import models.quiz.UserConstant.EnhancedHtml

case class QuestionPartSequence(id: QuestionPartId, sectionId: QuestionSectionId, questionId: QuestionId, summaryRaw: String, summaryHtml: Html, sequenceStr: String, sequenceMath: SequenceTokenOrMath, order: Short) extends HasOrder[QuestionPartSequence] {
  def fixConstants(user: User, userConstants: QuestionUserConstantsFrame) = this.copy(
    summaryHtml = summaryHtml.fixConstants(user, questionId, userConstants),
    sequenceMath = sequenceMath.fixConstants(user, questionId, userConstants)
  )
}