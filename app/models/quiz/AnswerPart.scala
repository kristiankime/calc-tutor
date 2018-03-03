package models.quiz

import com.artclod.mathml.scalar.MathMLElem
import com.artclod.slick.NumericBoolean
import models.support.HasOrder
import models._
import play.twirl.api.Html

case class AnswerPart(// =================== Ids ====================
                      id: AnswerPartId, answerSectionId: AnswerSectionId, answerId:   AnswerId,
                      questionPartId:       QuestionPartId, sectionId:       QuestionSectionId, questionId: QuestionId,
                      // =============== Answer stuff ===============
                      functionRaw: String, functionMath: MathMLElem, correctNum: Short, order: Short) extends HasOrder[AnswerPart] {
  def correct = NumericBoolean(correctNum)
}