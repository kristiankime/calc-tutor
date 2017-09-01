package models.quiz

import com.artclod.mathml.scalar.MathMLElem
import models.support.HasOrder
import models.{PartId, QuestionId, SectionId}
import play.twirl.api.Html

case class QuestionPartFunction(id: PartId, sectionId: SectionId, questionId: QuestionId, descriptionRaw: String, descriptionHtml: Html, functionRaw: String, functionMath: MathMLElem, order: Short) extends HasOrder[QuestionPartFunction]