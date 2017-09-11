package models.quiz

import com.artclod.mathml.MathML
import com.artclod.mathml.scalar.MathMLElem
import com.artclod.slick.{JodaUTC, NumericBoolean}
import controllers.quiz.{QuestionJson, QuestionPartChoiceJson, QuestionPartFunctionJson, QuestionSectionJson}
import models.support.HasOrder
import models.{QuestionPartId, QuestionId, QuestionSectionId, UserId}
import org.joda.time.DateTime
import play.twirl.api.Html

case class QuestionFrame(question: Question, sections: Vector[QuestionSectionFrame]) {
  def id(questionId: QuestionId) = QuestionFrame(question.copy(id = questionId), sections.map(s => s.questionId(questionId)))

  // ==== throw errors for bad formulation
  if(sections.isEmpty) {
    throw new IllegalArgumentException("There were no sections");
  }

}

case class QuestionSectionFrame(section: QuestionSection, parts: Either[Vector[QuestionPartChoice], Vector[QuestionPartFunction]]) extends HasOrder[QuestionSectionFrame] {

  override def order = section.order

  def correctIndex = parts match {
    case Left(choices) => Some(choices.indexWhere(_.correctChoice == 1))
    case Right(functions) => None
  }

  def choiceSize = parts match {
    case Left(choices) => Some(choices.size)
    case Right(functions) => None
  }

  def choiceOrFunction = parts match {
    case Left(choices) => "choice"
    case Right(functions) => "function"
  }

  def id(sectionId: QuestionSectionId) = QuestionSectionFrame(
    section = section.copy(id = sectionId),
    parts = parts match {
      case Left(ps) => Left(ps.map(p => p.copy(sectionId=sectionId)))
      case Right(ps) => Right(ps.map(p => p.copy(sectionId=sectionId)))
    }
  )

  def questionId(questionId: QuestionId) = QuestionSectionFrame(
    section = section.copy(questionId = questionId),
    parts = parts match {
      case Left(ps) => Left(ps.map(p => p.copy(questionId=questionId)))
      case Right(ps) => Right(ps.map(p => p.copy(questionId=questionId)))
    }
  )

  // ==== throw errors for bad formulation
  parts match {
    case Left(partChoices) => {
      if(partChoices.isEmpty) {
        throw new IllegalArgumentException("There were no partChoices");
      } else if (!partChoices.map(_.correctChoice == 1).fold(false)((a, b) => a || b)) {
        throw new IllegalArgumentException("There was no correct choice in " + partChoices)
      }
      QuestionFrame.checkInOrder(partChoices, "partChoices")
    }
    case Right(functionChoices) => {
      if(functionChoices.isEmpty) {
        throw new IllegalArgumentException("There were no functionChoices");
      }
      QuestionFrame.checkInOrder(functionChoices, "functionChoices")
    }
  }

}

object QuestionFrame {

  def checkInOrder(items : Seq[HasOrder[_]], message: String) {
    val size = items.size
    if(items.map(_.order.toInt) != (0 until size)) {
      throw new IllegalArgumentException(message + " was not in order " + items)
    }
  }

  // ==== JSON to Frame ====
  def  apply(questionJson: QuestionJson, ownerId: UserId, now : DateTime = JodaUTC.now) : QuestionFrame = {
    val question : Question = Question(id = null,
      ownerId = ownerId,
      title = questionJson.title,
      descriptionRaw = questionJson.descriptionRaw,
      descriptionHtml = Html(questionJson.descriptionHtml),
      creationDate = now)

    val sections : Vector[QuestionSectionFrame] = questionJson.sections.zipWithIndex.map(s => sectionFrame(s._1, s._2))

   QuestionFrame(question=question, sections=sections)
  }

  private def sectionFrame(section: QuestionSectionJson, index: Int) : QuestionSectionFrame = {
    val questionSection = QuestionSection(id = null, questionId = null,
      explanationRaw = section.explanationRaw,
      explanationHtml = Html(section.explanationHtml),
      order = index.toShort)

    val parts = section.choiceOrFunction match {
      case "choice" => {
        if(section.correctChoiceIndex >= section.choices.size || section.correctChoiceIndex < 0) {
          throw new IllegalArgumentException("section.correctChoiceIndex did not match any section [" + section.correctChoiceIndex + "] " + section.choices.size)
        }
        Left(section.choices.zipWithIndex.map(f => partChoice(f._1, f._2, NumericBoolean(section.correctChoiceIndex == f._2))))
      }
      case "function" => Right(section.functions.zipWithIndex.map(f => partFunction(f._1, f._2)))
      case _ => throw new IllegalArgumentException("section.choiceOrFunction was not recognized [" + section.choiceOrFunction + "]")
    }

    QuestionSectionFrame(questionSection, parts)
  }

  private def partChoice(part: QuestionPartChoiceJson, index: Int, correct: Short) : QuestionPartChoice = {
    QuestionPartChoice(id = null, sectionId = null, questionId = null,
      descriptionRaw = part.summaryRaw,
      descriptionHtml = Html(part.summaryHtml),
      correctChoice = correct,
      order = index.toShort)
  }

  private def partFunction(part: QuestionPartFunctionJson, index: Int) : QuestionPartFunction = {
    QuestionPartFunction(id = null, sectionId = null, questionId = null,
      descriptionRaw = part.summaryRaw,
      descriptionHtml = Html(part.summaryHtml),
      functionRaw = part.functionRaw,
      functionMath = MathML(part.functionMath).get,
      order = index.toShort)
  }

}

object QuestionSectionFrame {

  def apply(section: QuestionSection, choices: Seq[QuestionPartChoice], functions: Seq[QuestionPartFunction]): QuestionSectionFrame  =
    (choices.nonEmpty, functions.nonEmpty) match {
      case (false, false) => throw new IllegalArgumentException("functions and choices were both null")
      case (true, true) => throw new IllegalArgumentException("both functions and choices had values functions = " + functions + " choices = " + choices)
      case (true, false) => QuestionSectionFrame(section, Left( Vector(choices:_*).sorted ))
      case (false, true) => QuestionSectionFrame(section, Right( Vector(functions:_*).sorted ))
    }

}