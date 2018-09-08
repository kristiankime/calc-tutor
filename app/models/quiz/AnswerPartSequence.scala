package models.quiz

import com.artclod.mathml.scalar.MathMLElem
import com.artclod.slick.NumericBoolean
import models._
import models.support.HasOrder

case class AnswerPartSequence(// =================== Ids ====================
                      id: AnswerPartId, answerSectionId: AnswerSectionId, answerId: AnswerId, questionPartId: QuestionPartId, sectionId: QuestionSectionId, questionId: QuestionId,
                      // =============== Answer stuff ===============
                      sequenceStr: String, correctNum: Short,  order: Short) extends HasOrder[AnswerPartSequence] {
  def correct = NumericBoolean(correctNum)
}