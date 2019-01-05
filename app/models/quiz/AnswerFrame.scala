package models.quiz

import com.artclod.math.SequenceParse
import com.artclod.mathml.{Inconclusive, MathML, No, Yes}
import com.artclod.slick.{JodaUTC, NumericBoolean}
import com.artclod.util.ofthree.{First, Second, Third}
import controllers.quiz.{AnswerJson, AnswerPartFunctionJson, AnswerPartSequenceJson, AnswerSectionJson}
import models._
import models.quiz.util.SequenceTokenOrMath
import models.support.HasOrder
import org.joda.time.DateTime
import models.user.User

case class AnswerFrame(answer: Answer, sections: Vector[AnswerSectionFrame], correctUnknown: Boolean) {
  if(sections.isEmpty) {throw new IllegalArgumentException("Answer sections was empty")}

  def id(answerId: AnswerId) = AnswerFrame(
    answer = answer.copy(id = answerId),
    sections = sections.map(s => s.answerId(answerId)),
    correctUnknown = correctUnknown
  )

}

case class AnswerSectionFrame(answerSection: AnswerSection, functionParts: Vector[AnswerPartFunction], sequenceParts: Vector[AnswerPartSequence], correctUnknown: Boolean) extends HasOrder[AnswerSectionFrame] {
  override def order = answerSection.order

  val isChoice = NumericBoolean(answerSection.choice.nonEmpty)
  val isFunc = NumericBoolean(functionParts.nonEmpty)
  val isSeq = NumericBoolean(sequenceParts.nonEmpty)

  val typeTotal = isChoice + isFunc + isSeq
  if(typeTotal != 1.toShort) { throw new IllegalArgumentException("There should have been only one part type for answer section " + answerSection)}

  def id(sectionId: AnswerSectionId) = AnswerSectionFrame(
    answerSection = answerSection.copy(id = sectionId),
    functionParts = functionParts.map(p => p.copy(answerSectionId=sectionId)),
    sequenceParts = sequenceParts.map(p => p.copy(answerSectionId=sectionId)),
    correctUnknown = correctUnknown
  )

  def answerId(answerId: AnswerId) = AnswerSectionFrame(
    answerSection = answerSection.copy(answerId = answerId),
    functionParts = functionParts.map(p => p.copy(answerId=answerId)),
    sequenceParts = sequenceParts.map(p => p.copy(answerId=answerId)),
    correctUnknown = correctUnknown
  )

  def correct = functionParts.map(_.correct).fold(true)((a, b) => a && b)
}

object AnswerFrame {

  def apply(questionFrameIn: QuestionFrame, answerJson: AnswerJson, user: User, creationDate: DateTime = JodaUTC.now): AnswerFrame = {
    if(questionFrameIn.sections.size != answerJson.sections.size){throw new IllegalArgumentException("sections were not the same size")}

    val questionFrame = questionFrameIn.fixConstants(user) // We need to fix the user constants before we can determine if the user answered correctly

    val sections = questionFrame.sections.zip(answerJson.sections).zipWithIndex.map(s => sectionFrame(s._1._1, s._1._2, s._2.toShort))
    val correct : Short = sections.map(s => s.answerSection.correctNum).reduce(math.min(_, _).toShort)
    val answer = Answer(id=null, ownerId=user.id, questionId=questionFrame.question.id, correctNum=correct, creationDate=JodaUTC.now)
    val correctUnknown = correct <= NumericBoolean.Unknown
    AnswerFrame(answer, sections, correctUnknown)
  }

  private def sectionFrame(sectionFrame: QuestionSectionFrame, answerSectionJson: AnswerSectionJson, index: Short) =  {
    val questionSection = sectionFrame.section
    sectionFrame.parts match {
      case First(choices) => {
        val choiceIndex = answerSectionJson.choiceIndex.toInt // TODO can throw
        val correctIndex = choices.indexWhere(c => c.correct)
        val answerSection = AnswerSection(id=null, answerId=null, sectionId=questionSection.id, questionId=questionSection.questionId, choice=Some(choiceIndex.toShort), correctNum=NumericBoolean(choiceIndex == correctIndex), order=index)
        AnswerSectionFrame(answerSection, Vector(), Vector(), false)
      }
      case Second(functions) => {
        if(answerSectionJson.functions.size != functions.size) {
          throw new IllegalArgumentException("function parts were not the same size")
        }
        val parts = answerSectionJson.functions.zip(functions).zipWithIndex.map(fs => answerPartFunction(fs._1._1, fs._1._2, fs._2.toShort))
        val correct = parts.map(p => p.correctNum).reduce(math.min(_, _).toShort)
        val answerSection = AnswerSection(id=null, answerId=null, sectionId=questionSection.id, questionId=questionSection.questionId, choice=None, correctNum=correct, order=index)
        val correctUnknown = correct <= NumericBoolean.Unknown
        AnswerSectionFrame(answerSection, parts, Vector(), correctUnknown)
      }

      case Third(sequences) => {
        if(answerSectionJson.sequences.size != sequences.size) {
          throw new IllegalArgumentException("sequences parts were not the same size")
        }
        val parts = answerSectionJson.sequences.zip(sequences).zipWithIndex.map(fs => answerPartSequence(fs._1._1, fs._1._2, fs._2.toShort))
        val correct = parts.map(p => p.correctNum).reduce(math.min(_, _).toShort)
        val answerSection = AnswerSection(id=null, answerId=null, sectionId=questionSection.id, questionId=questionSection.questionId, choice=None, correctNum=correct, order=index)
        val correctUnknown = correct <= NumericBoolean.Unknown
        AnswerSectionFrame(answerSection, Vector(), parts, correctUnknown)
      }
    }
  }

  private def answerPartFunction(answerPartFunctionJson: AnswerPartFunctionJson, questionPartFunction: QuestionPartFunction, order: Short) : AnswerPartFunction = {
    val functionMath = MathML(answerPartFunctionJson.functionMath).get

    val correct = (questionPartFunction.functionMath ?= functionMath) match {
      case Yes => NumericBoolean.T
      case No => NumericBoolean.F
      case Inconclusive => NumericBoolean.Unknown
    }

    AnswerPartFunction(id=null, answerSectionId=null, answerId=null,
      questionPartId=questionPartFunction.id, sectionId=questionPartFunction.sectionId, questionId = questionPartFunction.questionId,
      functionRaw=answerPartFunctionJson.functionRaw, functionMath = functionMath, correctNum = correct, order = order
    )
  }

  private def answerPartSequence(answerPartSequenceJson: AnswerPartSequenceJson, questionPartSequence: QuestionPartSequence, order: Short) : AnswerPartSequence = {
    val correct = NumericBoolean(questionPartSequence.sequenceMath.mathEquals(  SequenceTokenOrMath(answerPartSequenceJson.sequenceMath)  ) )
    AnswerPartSequence(id=null, answerSectionId=null, answerId=null,
      questionPartId=questionPartSequence.id, sectionId=questionPartSequence.sectionId, questionId = questionPartSequence.questionId,
      sequenceStr = answerPartSequenceJson.sequenceStr, sequenceMath = SequenceTokenOrMath(answerPartSequenceJson.sequenceMath), correctNum = correct, order = order
    )
  }

}
