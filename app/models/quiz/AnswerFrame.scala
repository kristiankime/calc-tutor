package models.quiz

import com.artclod.mathml.{Inconclusive, MathML, No, Yes}
import com.artclod.slick.{JodaUTC, NumericBoolean}
import controllers.quiz.{AnswerJson, AnswerPartFunctionJson, AnswerSectionJson}
import models._
import models.support.HasOrder
import org.joda.time.DateTime

case class AnswerFrame(answer: Answer, sections: Vector[AnswerSectionFrame], correctUnknown: Boolean) {
  if(sections.isEmpty) {throw new IllegalArgumentException("Answer sections was empty")}

  def id(answerId: AnswerId) = AnswerFrame(
    answer = answer.copy(id = answerId),
    sections = sections.map(s => s.answerId(answerId)),
    correctUnknown = correctUnknown
  )

}

case class AnswerSectionFrame(answerSection: AnswerSection, parts: Vector[AnswerPart], correctUnknown: Boolean) extends HasOrder[AnswerSectionFrame] {
  override def order = answerSection.order

  (answerSection.choice.nonEmpty, parts.nonEmpty ) match {
    case (true, true) => {throw new IllegalArgumentException("Answer was had both choice and parts")}
    case (false, false) => {throw new IllegalArgumentException("Answer was had neither choice not parts")}
    case _ : (Boolean, Boolean) => { "All good" }
  }

  def id(sectionId: AnswerSectionId) = AnswerSectionFrame(
    answerSection = answerSection.copy(id = sectionId),
    parts = parts.map(p => p.copy(answerSectionId=sectionId)),
    correctUnknown = correctUnknown
  )

  def answerId(answerId: AnswerId) = AnswerSectionFrame(
    answerSection = answerSection.copy(answerId = answerId),
    parts = parts.map(p => p.copy(answerId=answerId)),
    correctUnknown = correctUnknown
  )

}

object AnswerFrame {

  def apply(questionFrame: QuestionFrame, answerJson: AnswerJson, userId: UserId, creationDate: DateTime = JodaUTC.now): AnswerFrame = {
    if(questionFrame.sections.size != answerJson.sections.size){throw new IllegalArgumentException("sections were not the same size")}

    val sections = questionFrame.sections.zip(answerJson.sections).zipWithIndex.map(s => sectionFrame(s._1._1, s._1._2, s._2.toShort))
    val allCorrect : Short = sections.map(s => s.answerSection.correctNum).reduce(math.min(_, _).toShort)
    val answer = Answer(id=null, ownerId=userId, questionId=questionFrame.question.id, correctNum=allCorrect, creationDate=JodaUTC.now)
    val correctUnknown = allCorrect <= NumericBoolean.Unknown
    AnswerFrame(answer, sections, correctUnknown)
  }

  private def sectionFrame(sectionFrame: QuestionSectionFrame, answerSectionJson: AnswerSectionJson, index: Short) =  {
    val questionSection = sectionFrame.section
    sectionFrame.parts match {
      case Left(choices) => {
        val choiceIndex = answerSectionJson.choiceIndex.toInt // TODO can throw
        val correctIndex = choices.indexWhere(c => c.correct)
        val answerSection = AnswerSection(id=null, answerId=null, sectionId=questionSection.id, questionId=questionSection.questionId, choice=Some(choiceIndex.toShort), correctNum=NumericBoolean(choiceIndex == correctIndex), order=index)
        AnswerSectionFrame(answerSection, Vector(), false)
      }
      case Right(functions) => {
        if(answerSectionJson.functions.size != functions.size) {
          throw new IllegalArgumentException("function parts were not the same size")
        }
        val parts = answerSectionJson.functions.zip(functions).zipWithIndex.map(fs => answerPart(fs._1._1, fs._1._2, fs._2.toShort))
        val correct = parts.map(p => p.correctNum).reduce(math.min(_, _).toShort)
        val answerSection = AnswerSection(id=null, answerId=null, sectionId=questionSection.id, questionId=questionSection.questionId, choice=None, correctNum=correct, order=index)
        val correctUnknown = correct <= NumericBoolean.Unknown
        AnswerSectionFrame(answerSection, parts, correctUnknown)
      }
    }
  }

  private def answerPart(answerPartFunctionJson: AnswerPartFunctionJson, questionPartFunction: QuestionPartFunction, order: Short) : AnswerPart = {
    val functionMath = MathML(answerPartFunctionJson.functionMath).get

    val correct = (questionPartFunction.functionMath ?= functionMath) match {
      case Yes => NumericBoolean.T
      case No => NumericBoolean.F
      case Inconclusive => NumericBoolean.Unknown
    }

    AnswerPart(id=null, answerSectionId=null, answerId=null,
      questionPartId=questionPartFunction.id, sectionId=questionPartFunction.sectionId, questionId = questionPartFunction.questionId,
      functionRaw=answerPartFunctionJson.functionRaw, functionMath = functionMath, correctNum = correct, order = order
    )
  }

}
