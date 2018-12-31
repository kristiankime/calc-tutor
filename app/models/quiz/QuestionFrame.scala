package models.quiz

import com.artclod.mathml.MathML
import com.artclod.mathml.scalar.MathMLElem
import com.artclod.slick.{JodaUTC, NumericBoolean}
import com.artclod.util.OneOfThree
import com.artclod.util.ofthree.{First, Second, Third}
import controllers.quiz._
import models.quiz.util.SequenceTokenOrMath
import models.support.HasOrder
import models.{QuestionId, QuestionPartId, QuestionSectionId, UserId}
import org.joda.time.DateTime
import play.twirl.api.Html
import models.user.User


// ======= QuestionFrame
case class QuestionFrame(question: Question, sections: Vector[QuestionSectionFrame], skills: Vector[Skill], userConstants: QuestionUserConstantsFrame) {
  // ==== throw errors for bad formulation
  if(sections.isEmpty) { throw new IllegalArgumentException("There were no sections"); }
  if(skills.isEmpty) { throw new IllegalArgumentException("There were no skills"); }

  def id(questionId: QuestionId) = QuestionFrame(question.copy(id = questionId), sections.map(_.questionId(questionId)), skills, userConstants.questionId(questionId))

  def fixConstants(user: User) =
    copy(
      question = question.fixConstants(user, userConstants),
      sections = sections.map(_.fixConstants(user, userConstants)),
      userConstants = QuestionUserConstantsFrame.empty)
}

object QuestionFrame {

  // ==== JSON to Frame ====
  def apply(questionJson: QuestionJson, ownerId: UserId, skillMap: Map[String, Skill], now : DateTime = JodaUTC.now) : QuestionFrame = {
    val question : Question = Question(id = null,
      ownerId = ownerId,
      title = questionJson.title,
      descriptionRaw = questionJson.descriptionRaw,
      descriptionHtml = Html(questionJson.descriptionHtml),
      creationDate = now)

//    val question : Question = questionJson.toModel(ownerId, now)

    val sections : Vector[QuestionSectionFrame] = questionJson.sections.zipWithIndex.map(s => sectionFrame(s._1, s._2))

    val userConstants : QuestionUserConstantsFrame = questionJson.userConstants.map(_.toModel).getOrElse(QuestionUserConstantsFrame.empty)

    QuestionFrame(question=question, sections=sections, skills = questionJson.skills.map(s => skillMap.getOrElse(s, throw new IllegalArgumentException("Skill not found for " + s)) ), userConstants = userConstants)
  }

  private def sectionFrame(section: QuestionSectionJson, index: Int) : QuestionSectionFrame = {
    val questionSection = QuestionSection(id = null, questionId = null,
      explanationRaw = section.explanationRaw,
      explanationHtml = Html(section.explanationHtml),
      order = index.toShort)

    val parts : OneOfThree[ Vector[QuestionPartChoice], Vector[QuestionPartFunction], Vector[QuestionPartSequence] ] = section.partType match {
      case "choice" => {
        if(section.correctChoiceIndex >= section.choices.size || section.correctChoiceIndex < 0) {
          throw new IllegalArgumentException("section.correctChoiceIndex did not match any section [" + section.correctChoiceIndex + "] " + section.choices.size)
        }
        First(section.choices.zipWithIndex.map(f => partChoice(f._1, f._2, NumericBoolean(section.correctChoiceIndex == f._2))))
      }
      case "function" => Second(section.functions.zipWithIndex.map(f => partFunction(f._1, f._2)))
      case "sequence" => Third(section.sequences.zipWithIndex.map(f => partSequence(f._1, f._2)))
      case _ => throw new IllegalArgumentException("section.partType was not recognized [" + section.partType + "]")
    }

    QuestionSectionFrame(questionSection, parts)
  }

  private def partChoice(part: QuestionPartChoiceJson, index: Int, correct: Short) : QuestionPartChoice = {
    QuestionPartChoice(id = null, sectionId = null, questionId = null,
      summaryRaw = part.summaryRaw,
      summaryHtml = Html(part.summaryHtml),
      correctChoice = correct,
      order = index.toShort)
  }

  private def partFunction(part: QuestionPartFunctionJson, index: Int) : QuestionPartFunction = {
    QuestionPartFunction(id = null, sectionId = null, questionId = null,
      summaryRaw = part.summaryRaw,
      summaryHtml = Html(part.summaryHtml),
      functionRaw = part.functionRaw,
      functionMath = MathML(part.functionMath).get,
      order = index.toShort)
  }

  private def partSequence(part: QuestionPartSequenceJson, index: Int) : QuestionPartSequence = {
    QuestionPartSequence(id = null, sectionId = null, questionId = null,
      summaryRaw = part.summaryRaw,
      summaryHtml = Html(part.summaryHtml),
      sequenceStr = part.sequenceStr,
      sequenceMath = SequenceTokenOrMath(part.sequenceMath),
      order = index.toShort)
  }

  // ---- utility functions
  def checkInOrder(items : Seq[HasOrder[_]], message: String) {
    val size = items.size
    if(items.map(_.order.toInt) != (0 until size)) {
      throw new IllegalArgumentException(message + " was not in order " + items)
    }
  }
}


// ======= QuestionUserConstantsFrame
case class QuestionUserConstantsFrame(integers: Vector[QuestionUserConstantInteger], decimals: Vector[QuestionUserConstantDecimal], sets: Vector[QuestionUserConstantSet]) {
  def questionId(questionId: QuestionId) : QuestionUserConstantsFrame =
    QuestionUserConstantsFrame(
      integers.map(_.copy(questionId = questionId)),
      decimals.map(_.copy(questionId = questionId)),
      sets.map(_.copy(questionId = questionId))
    )

  def all: Vector[UserConstant] = integers ++ decimals ++ sets

  def constant(name: String) = all.find(_.name == name)

}

object QuestionUserConstantsFrame {
  val empty = QuestionUserConstantsFrame(Vector(), Vector(), Vector())

//  def apply(integers: Vector[QuestionUserConstantInteger], decimals: Vector[QuestionUserConstantDecimal], sets: Vector[QuestionUserConstantSet]): QuestionUserConstantsFrame = new QuestionUserConstantsFrame(integers, decimals, sets)
  def apply(values : (Seq[QuestionUserConstantInteger], Seq[QuestionUserConstantDecimal], Seq[QuestionUserConstantSet]) ): QuestionUserConstantsFrame = new QuestionUserConstantsFrame(values._1.toVector, values._2.toVector, values._3.toVector)
}

// ======= QuestionSectionFrame
case class QuestionSectionFrame(section: QuestionSection, parts: OneOfThree[ Vector[QuestionPartChoice], Vector[QuestionPartFunction], Vector[QuestionPartSequence] ]) extends HasOrder[QuestionSectionFrame] {

  override def order = section.order

  def correctIndex = parts match {
    case First(choices) => Some(choices.indexWhere(_.correctChoice == NumericBoolean.T))
    case Second(functions) => None
    case Third(sequences) => None
  }

  def choiceSize = parts match {
    case First(choices) => Some(choices.size)
    case Second(functions) => None
    case Third(sequences) => None
  }

  def partKind = parts match {
    case First(choices) => "choice"
    case Second(functions) => "function"
    case Third(sequences) => "sequence"
  }

  def id(sectionId: QuestionSectionId) = QuestionSectionFrame(
    section = section.copy(id = sectionId),
    parts = parts match {
      case First(ps)  => First(ps.map(p => p.copy(sectionId=sectionId)))
      case Second(ps) => Second(ps.map(p => p.copy(sectionId=sectionId)))
      case Third(ps)  => Third(ps.map(p => p.copy(sectionId=sectionId)))
    }
  )

  def questionId(questionId: QuestionId) = QuestionSectionFrame(
    section = section.copy(questionId = questionId),
    parts = parts match {
      case First(ps)  => First(ps.map(p => p.copy(questionId=questionId)))
      case Second(ps) => Second(ps.map(p => p.copy(questionId=questionId)))
      case Third(ps)  => Third(ps.map(p => p.copy(questionId=questionId)))
    }
  )

  def fixConstants(user: User, userConstants: QuestionUserConstantsFrame) = this.copy(
    section = section.fixConstants(user, userConstants),
    parts = parts match {
      case First(ps)  => First( ps.map(p => p.fixConstants(user, userConstants)))
      case Second(ps) => Second(ps.map(p => p.fixConstants(user, userConstants)))
      case Third(ps)  => Third( ps.map(p => p.fixConstants(user, userConstants)))
    }
  )

  // ==== throw errors for bad formulation
  parts match {
    case First(partChoices) => {
      if(partChoices.isEmpty) {
        throw new IllegalArgumentException("There were no partChoices");
      } else if (!partChoices.map(_.correctChoice == NumericBoolean.T).fold(false)((a, b) => a || b)) {
        throw new IllegalArgumentException("There was no correct choice in " + partChoices)
      }
      QuestionFrame.checkInOrder(partChoices, "partChoices")
    }
    case Second(functionChoices) => {
      if(functionChoices.isEmpty) {
        throw new IllegalArgumentException("There were no functionChoices");
      }
      QuestionFrame.checkInOrder(functionChoices, "functionChoices")
    }

    case Third(sequenceChoices) => {
      if(sequenceChoices.isEmpty) {
        throw new IllegalArgumentException("There were no sequenceChoices");
      }
      QuestionFrame.checkInOrder(sequenceChoices, "sequenceChoices")
    }
  }

}

object QuestionSectionFrame {

  def apply(section: QuestionSection, choices: Seq[QuestionPartChoice], functions: Seq[QuestionPartFunction], sequences: Seq[QuestionPartSequence]): QuestionSectionFrame  =
    (choices.nonEmpty, functions.nonEmpty, sequences.nonEmpty) match {
      case (false, false, false) => throw new IllegalArgumentException("functions, choices and sequence were all null")
      case (true, true, true) => throw new IllegalArgumentException("functions, choices and sequence all had values: functions = " + functions + " choices = " + choices + " sequences = " + sequences)
      case (true, false, false) => QuestionSectionFrame(section, First( Vector(choices:_*).sorted ))
      case (false, true, false) => QuestionSectionFrame(section, Second( Vector(functions:_*).sorted ))
      case (false, false, true) => QuestionSectionFrame(section, Third( Vector(sequences:_*).sorted ))
      case _  => throw new IllegalArgumentException("two of functions, choices and sequence had values: functions = " + functions + " choices = " + choices + " sequences = " + sequences)
    }

}
