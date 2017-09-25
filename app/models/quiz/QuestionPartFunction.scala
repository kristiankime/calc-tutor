package models.quiz

import com.artclod.mathml.scalar.MathMLElem
import models.support.HasOrder
import models.{QuestionPartId, QuestionId, QuestionSectionId}
import play.twirl.api.Html

case class QuestionPartFunction(id: QuestionPartId, sectionId: QuestionSectionId, questionId: QuestionId, summaryRaw: String, summaryHtml: Html, functionRaw: String, functionMath: MathMLElem, order: Short) extends HasOrder[QuestionPartFunction]