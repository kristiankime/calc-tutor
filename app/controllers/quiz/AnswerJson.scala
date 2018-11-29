package controllers.quiz

import com.artclod.mathml.MathML
import com.artclod.slick.NumericBoolean
import com.artclod.util.OneOfThree
import com.artclod.util.ofthree.{First, Second, Third}
import models.quiz._

import scala.util.Random

// === AnswerJson
case class AnswerJson(sections: Vector[AnswerSectionJson], correct: Int) {
  if(sections.size == 0) {throw new IllegalArgumentException("Answers must have at least one section")}
}

object AnswerJson {
  // These can be stored in the db
  val correctYes = NumericBoolean.T
  val correctNo = NumericBoolean.F
  // These are temporary values only used in the Json/Ui
  val correctUnknown = NumericBoolean.Unknown
  val correctBlank = NumericBoolean.Blank

  val noChoiceSelected : Short = -1

  // === Build a "Blank" Answer from the question
  def blank(questionFrame: QuestionFrame): AnswerJson = {
    AnswerJson(questionFrame.sections.map(sectionFrame => AnswerSectionJson.blank(sectionFrame)), correct = correctBlank)
  }

  // === Easier Builder
  def apply(correct: Int, sections: AnswerSectionJson*) : AnswerJson = AnswerJson(Vector(sections:_*), correct)

  // === Filled in from previous answer
  def apply(answerFrame: AnswerFrame) : AnswerJson =
    AnswerJson(answerFrame.sections.map(s => AnswerSectionJson(s)), answerFrame.answer.correctNum)
}

// === AnswerSectionJson
case class AnswerSectionJson(choiceIndex: Int, functions: Vector[AnswerPartFunctionJson], sequences: Vector[AnswerPartSequenceJson], correct: Int)

object AnswerSectionJson {
  val rand = new Random(System.currentTimeMillis())

  def blank(sectionFrame: QuestionSectionFrame): AnswerSectionJson =
    AnswerSectionJson(choiceIndex = sectionFrame.choiceSize.map(v => rand.nextInt(v)).getOrElse(AnswerJson.noChoiceSelected), functions=AnswerPartFunctionJson.blank(sectionFrame.parts), sequences = AnswerPartSequenceJson.blank(sectionFrame.parts), correct = AnswerJson.correctBlank)

  def apply(correct: Int, choiceIndex: Int): AnswerSectionJson =
    AnswerSectionJson(choiceIndex, Vector(), Vector(), correct)

  def apply(correct: Int, choiceIndex: Int, functionParts: Vector[AnswerPartFunctionJson], sequenceParts: Vector[AnswerPartSequenceJson]) : AnswerSectionJson =
    AnswerSectionJson(choiceIndex, Vector(functionParts:_*), Vector(sequenceParts:_*), correct)

  def apply(correct: Int, choiceIndex: Int, functionParts: AnswerPartFunctionJson*) : AnswerSectionJson =
    AnswerSectionJson(choiceIndex, Vector(functionParts:_*), Vector(), correct)

  def apply(answerSectionFrame: AnswerSectionFrame) : AnswerSectionJson =
    AnswerSectionJson(
      choiceIndex = answerSectionFrame.answerSection.choice.getOrElse(AnswerJson.noChoiceSelected).toShort,
      functions = answerSectionFrame.functionParts.map(p => AnswerPartFunctionJson(p)),
      sequences = answerSectionFrame.sequenceParts.map(p => AnswerPartSequenceJson(p)),
      correct = answerSectionFrame.answerSection.correctNum )

}

// === AnswerPartFunctionJson
case class AnswerPartFunctionJson(functionRaw: String, functionMath: String, correct: Int)

object AnswerPartFunctionJson {

  def blank(functionParts: OneOfThree[_, Vector[QuestionPartFunction], _]): Vector[AnswerPartFunctionJson] = functionParts match {
    case First(_) => Vector()
    case Second(fps) => fps.map(p => AnswerPartFunctionJson("", "", AnswerJson.correctBlank))
    case Third(_) => Vector()
  }

  def apply(function: String, correct: Int) : AnswerPartFunctionJson =
    AnswerPartFunctionJson(function, MathML(function).get.toString, correct)

  def apply(answerPartFunction: AnswerPartFunction) : AnswerPartFunctionJson =
    AnswerPartFunctionJson(answerPartFunction.functionRaw, answerPartFunction.functionMath.toString, answerPartFunction.correctNum)

}

// === AnswerPartSequenceJson
case class AnswerPartSequenceJson(sequenceStr: String, sequenceMath: String, correct: Int)

object AnswerPartSequenceJson {

  def blank(sequenceParts: OneOfThree[_, _, Vector[QuestionPartSequence]]): Vector[AnswerPartSequenceJson] = sequenceParts match {
    case First(_) => Vector()
    case Second(_) => Vector()
    case Third(seq) => seq.map(p => AnswerPartSequenceJson("", "", AnswerJson.correctBlank))
  }

  def apply(answerPartSequence: AnswerPartSequence) : AnswerPartSequenceJson =
    AnswerPartSequenceJson(answerPartSequence.sequenceStr, answerPartSequence.sequenceMath.stringVersion, answerPartSequence.correctNum)

}
