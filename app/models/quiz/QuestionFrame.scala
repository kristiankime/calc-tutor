package models.quiz

import com.artclod.mathml.MathML
import com.artclod.mathml.scalar.MathMLElem
import com.artclod.slick.{JodaUTC, NumericBoolean}
import controllers.quiz.{QuestionJson, QuestionPartChoiceJson, QuestionPartFunctionJson, QuestionSectionJson}
import models.support.HasOrder
import models.{PartId, QuestionId, SectionId, UserId}
import org.joda.time.DateTime
import play.twirl.api.Html

case class QuestionFrame(question: Question, sections: Vector[SectionFrame]) {
  def id(questionId: QuestionId) = QuestionFrame(question.copy(id = questionId), sections.map(s => s.questionId(questionId)))
}

case class SectionFrame(section: QuestionSection, parts: Either[Vector[QuestionPartChoice], Vector[QuestionPartFunction]]) extends HasOrder[SectionFrame] {

  override def order = section.order

  def id(sectionId: SectionId) = SectionFrame(
      section = section.copy(id = sectionId),
      parts = parts match {
        case Left(ps) => Left(ps.map(p => p.copy(sectionId=sectionId)))
        case Right(ps) => Right(ps.map(p => p.copy(sectionId=sectionId)))
      }
    )

  def questionId(questionId: QuestionId) = SectionFrame(
    section = section.copy(questionId = questionId),
    parts = parts match {
      case Left(ps) => Left(ps.map(p => p.copy(questionId=questionId)))
      case Right(ps) => Right(ps.map(p => p.copy(questionId=questionId)))
    }
  )

}

object QuestionFrame {

  // ==== JSON to Frame ====
  def  apply(questionJson: QuestionJson, ownerId: UserId, now : DateTime = JodaUTC.now) : QuestionFrame = {
    val question : Question = Question(id = null,
      ownerId = ownerId,
      title = questionJson.title,
      descriptionRaw = questionJson.descriptionRaw,
      descriptionHtml = Html(questionJson.descriptionHtml),
      creationDate = now)

    val sections : Vector[SectionFrame] = questionJson.sections.zipWithIndex.map(s => sectionFrame(s._1, s._2))

   QuestionFrame(question=question, sections=sections)
  }

  private def sectionFrame(section: QuestionSectionJson, index: Int) : SectionFrame = {
    val questionSection = QuestionSection(id = null, questionId = null,
      explanationRaw = section.explanationRaw,
      explanationHtml = Html(section.explanationHtml),
      order = index.toShort)

    val parts = section.choiceOrFunction match {
      case "choice" => Left(section.choices.zipWithIndex.map(f => partChoice(f._1, f._2, NumericBoolean(section.correctChoiceIndex == f._1.id))))
      case "function" => Right(section.functions.zipWithIndex.map(f => partFunction(f._1, f._2)))
      case _ => throw new IllegalArgumentException("section.choiceOrFunction was not recognized [" + section.choiceOrFunction + "]")
    }

    SectionFrame(questionSection, parts)
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