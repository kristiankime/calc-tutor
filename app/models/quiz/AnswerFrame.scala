package models.quiz

import com.artclod.mathml.{Inconclusive, MathML, No, Yes}
import com.artclod.slick.{JodaUTC, NumericBoolean}
import controllers.quiz.{AnswerJson, AnswerPartFunctionJson, AnswerSectionJson}
import models._
import models.support.HasOrder
import org.joda.time.DateTime

case class AnswerFrame(answer: Answer, sections: Vector[AnswerSectionFrame]) {
  if(sections.isEmpty) {throw new IllegalArgumentException("Answer sections was empty")}
}

case class AnswerSectionFrame(answerSection: AnswerSection, parts: Vector[AnswerPart]) extends HasOrder[AnswerSectionFrame] {
  override def order = answerSection.order
}

object AnswerFrame {
  def apply(questionFrame: QuestionFrame, answerJson: AnswerJson, userId: UserId): AnswerFrame = {
    if(questionFrame.sections != answerJson.sections)("sections were not the same size")

    val sections = questionFrame.sections.zip(answerJson.sections).zipWithIndex.map(s => sectionFrame(s._1._1, s._1._2, s._2.toShort))
    val allCorrect = sections.map(s => s.answerSection.correct).reduce(_ && _)
    val answer = Answer(id=null, ownerId=userId, questionId=questionFrame.question.id, allCorrectNum=NumericBoolean(allCorrect), creationDate=JodaUTC.now)
    AnswerFrame(answer, sections)
  }

  def sectionFrame(sectionFrame: QuestionSectionFrame, answerSectionJson: AnswerSectionJson, index: Short) =  {
    val questionSection = sectionFrame.section
    sectionFrame.parts match {
      case Left(choices) => {
        val choiceIndex = answerSectionJson.choiceIndex.toInt // TODO can throw
        val correctIndex = choices.indexWhere(c => c.correct)
        val answerSection = AnswerSection(id=null, answerId=null, sectionId=questionSection.id, questionId=questionSection.questionId, correctNum=NumericBoolean(choiceIndex == correctIndex), order=index)
        AnswerSectionFrame(answerSection, Vector())
      }
      case Right(functions) => {
        if(answerSectionJson.functions.size != functions) {
          throw new IllegalArgumentException("function parts were not the same size")
        }
        val parts = answerSectionJson.functions.zip(functions).zipWithIndex.map(fs => answerPart(fs._1._1, fs._1._2, fs._2.toShort))
        val correct = parts.map(p => p.correct).reduce(_ && _)
        val answerSection =  AnswerSection(id=null, answerId=null, sectionId=questionSection.id, questionId=questionSection.questionId, correctNum=NumericBoolean(correct), order=index)
        AnswerSectionFrame(answerSection, parts)
      }
    }
  }

  def answerPart(answerPartFunctionJson: AnswerPartFunctionJson, questionPartFunction: QuestionPartFunction, order: Short) : AnswerPart = {
    val functionMath = MathML(answerPartFunctionJson.functionMath).get

    val correct = (questionPartFunction.functionMath ?= functionMath) match {
      case Yes => true
      case _ => false
    }

    AnswerPart(id=null, answerSectionId=null, answerId=null,
      questionPartId=questionPartFunction.id, sectionId=questionPartFunction.sectionId, questionId = questionPartFunction.questionId,
      functionRaw=answerPartFunctionJson.functionRaw, functionMath = functionMath, correctNum = NumericBoolean(correct), order = order
    )
  }


}
