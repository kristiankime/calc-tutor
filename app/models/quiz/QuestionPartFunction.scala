package models.quiz

import com.artclod.mathml.scalar.MathMLElem
import models.{QuestionId, SectionId}
import play.twirl.api.Html

case class QuestionPartFunction(id: Long, sectionId: SectionId, questionId: QuestionId, descriptionRaw: String, descriptionHtml: Html, functionRaw: String, functionMath: MathMLElem)