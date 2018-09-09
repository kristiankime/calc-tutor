package models.quiz

import com.artclod.mathml.scalar.MathMLElem
import models.support.HasOrder
import models.{QuestionId, QuestionPartId, QuestionSectionId}
import play.twirl.api.Html

case class QuestionPartSequence(id: QuestionPartId, sectionId: QuestionSectionId, questionId: QuestionId, summaryRaw: String, summaryHtml: Html, sequenceStr: String, order: Short) extends HasOrder[QuestionPartSequence]