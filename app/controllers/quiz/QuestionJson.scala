package controllers.quiz

import com.artclod.markup.Markdowner
import com.artclod.mathml.MathML
import com.artclod.util.ofthree.{First, Second, Third}
import models.{QuestionId, UserId, quiz}
import models.quiz._
import models.quiz.util.SetOfNumbers
import org.joda.time.DateTime
import play.api.libs.json.Json
import play.twirl.api.Html

// === QuestionJson
case class MinimalQuestionJson(id: Long, title: String, correct: Option[Long], attempts: Seq[MinimalAttemptsJson])
case class MinimalAttemptsJson(id: Long, correct: Boolean)

object MinimalQuestionJson {
  val id = "id"
  val title = "title"
  val correct = "correct"
  val attempts = "attempts"

  def apply(question: Question, answer: Option[Answer], attempts: Seq[Answer]): MinimalQuestionJson = {
    MinimalQuestionJson(question.id.v, question.titleArchived, answer.map(_.id.v), attempts.map(a => MinimalAttemptsJson(a.id.v, a.correct)))
  }

  def s(questions: Seq[Question], answerOp: Option[models.quiz.Answer], answers: Map[QuestionId, Seq[models.quiz.Answer]]): Seq[MinimalQuestionJson] = {
    questions.map(q => {
      val correct = answerOp.flatMap(a => if(a.questionId == q.id){Some(a)}else{None})
      val attempts = answers.getOrElse(q.id, Seq())
      apply(q, correct, attempts)
    })
  }

  implicit val minimalAttemptsFormat = Json.format[MinimalAttemptsJson]
  implicit val minimalQuestionFormat = Json.format[MinimalQuestionJson]
}


case class QuestionJson(title: String, descriptionRaw: String, descriptionHtml: String, sections: Vector[QuestionSectionJson], skills: Vector[String], userConstants: Option[QuestionUserConstantsJson]) {
  if(sections.size == 0) { throw new IllegalArgumentException("Questions must have at least one section")}
  if(skills.size == 0) { throw new IllegalArgumentException("Questions must have at least one skill")}

  def toModel(ownerId: UserId, now : DateTime) = Question(id = null,
    ownerId = ownerId,
    title = this.title,
    descriptionRaw = this.descriptionRaw,
    descriptionHtml = Html(this.descriptionHtml),
    creationDate = now)
}

object QuestionJson {

  def apply(title: String, description: String, sections: Seq[QuestionSectionJson], skills: Seq[String], userConstants: Option[QuestionUserConstantsJson]) : QuestionJson = QuestionJson(title, description, Markdowner.string(description), Vector(sections:_*), Vector(skills:_*), userConstants)

  def apply(questionFrame: QuestionFrame) : QuestionJson = {
    val sections = questionFrame.sections.map(s => QuestionSectionJson(s))
    val skills = questionFrame.skills.map((s => s.name))
    val userConstants = Some(QuestionUserConstantsJson(questionFrame.userConstants))
    QuestionJson(questionFrame.question.title, questionFrame.question.descriptionRaw, questionFrame.question.descriptionHtml.toString, sections, skills, userConstants)
  }

}

// === QuestionUserConstantJson
case class QuestionUserConstantsJson(integers: Seq[QuestionUserConstantIntegerJson], decimals: Seq[QuestionUserConstantDecimalJson], sets: Seq[QuestionUserConstantSetJson]) {
  def toModel() = quiz.QuestionUserConstantsFrame(integers.map(_.toModel()).toVector, decimals.map(_.toModel()).toVector, sets.map(_.toModel()).toVector)
}

object QuestionUserConstantsJson {
  def apply(questionUserConstants: QuestionUserConstantsFrame) : QuestionUserConstantsJson =
    QuestionUserConstantsJson(
      questionUserConstants.integers.map(QuestionUserConstantIntegerJson(_)),
      questionUserConstants.decimals.map(QuestionUserConstantDecimalJson(_)),
      questionUserConstants.sets.map(QuestionUserConstantSetJson(_))
    )
}

// ---
case class QuestionUserConstantIntegerJson(name: String, lower: Int, upper: Int) {
  def toModel() = QuestionUserConstantInteger(id = null, questionId = null, name = name, lower = lower, upper = upper)
}

object QuestionUserConstantIntegerJson {
  def apply(v : QuestionUserConstantInteger) : QuestionUserConstantIntegerJson = QuestionUserConstantIntegerJson(name = v.name, lower = v.lower, upper = v.upper)
}

// ---
case class QuestionUserConstantDecimalJson(name: String, lower: Double, upper: Double, precision: Int) {
  def toModel() = QuestionUserConstantDecimal(id = null, questionId = null, name = name, lower = lower, upper = upper, precision = precision)
}

object QuestionUserConstantDecimalJson {
  def apply(v : QuestionUserConstantDecimal) : QuestionUserConstantDecimalJson = QuestionUserConstantDecimalJson(name = v.name, lower = v.lower, upper = v.upper, precision = v.precision)
}

// ---
case class QuestionUserConstantSetJson(name: String, valuesRaw: String, valuesMath: String)  {
  def toModel() = QuestionUserConstantSet(id = null, questionId = null, name = name, valuesRaw = valuesRaw, valuesMath = SetOfNumbers(valuesMath))
}

object QuestionUserConstantSetJson {
  def apply(v : QuestionUserConstantSet) : QuestionUserConstantSetJson = QuestionUserConstantSetJson(name = v.name, valuesRaw = v.valuesRaw, valuesMath = v.valuesMath.toString)
}



// === QuestionSectionJson
case class QuestionSectionJson(explanationRaw: String, explanationHtml: String, partType: String, correctChoiceIndex: Int, choices: Vector[QuestionPartChoiceJson], functions: Vector[QuestionPartFunctionJson], sequences: Vector[QuestionPartSequenceJson]) {
  partType match {
    case QuestionCreate.choice => {
      if(correctChoiceIndex < 0 || correctChoiceIndex >= choices.size){ throw new IllegalArgumentException("Choice index was no in correct range")}
      if(choices.size == 0) {throw new IllegalArgumentException() }
    }
    case QuestionCreate.function => null
    case QuestionCreate.sequence => null
    case _ => throw new IllegalArgumentException("partType was not recognized type [" + partType + "]")
  }
}

object QuestionSectionJson {

  def ch(explanation: String, correctChoiceIndex : Int, choices: QuestionPartChoiceJson*)  : QuestionSectionJson =
    if (correctChoiceIndex >= 0 && correctChoiceIndex < choices.size)
      QuestionSectionJson(explanation, Markdowner.string(explanation), QuestionCreate.choice, correctChoiceIndex, Vector(choices: _*), Vector(), Vector())
    else {
      throw new IllegalArgumentException("Not a valid QuestionSectionJson combo")
    }

  def fn(explanation: String, functions: QuestionPartFunctionJson*)  : QuestionSectionJson =
    QuestionSectionJson(explanation, Markdowner.string(explanation), QuestionCreate.function, -1, Vector(), Vector(functions:_*), Vector())

  def se(explanation: String, sequences: QuestionPartSequenceJson*)  : QuestionSectionJson =
    QuestionSectionJson(explanation, Markdowner.string(explanation), QuestionCreate.sequence, -1, Vector(), Vector(), Vector(sequences:_*))

  def apply(questionSectionFrame: QuestionSectionFrame) : QuestionSectionJson = {
    val choices   : Vector[QuestionPartChoiceJson]   = questionSectionFrame.parts.first.getOrElse(Vector()).map(c => QuestionPartChoiceJson(c))
    val functions : Vector[QuestionPartFunctionJson] = questionSectionFrame.parts.second.getOrElse(Vector()).map(f => QuestionPartFunctionJson(f))
    val sequences : Vector[QuestionPartSequenceJson] = questionSectionFrame.parts.third.getOrElse(Vector()).map(s => QuestionPartSequenceJson(s))

    QuestionSectionJson(
      questionSectionFrame.section.explanationRaw,
      questionSectionFrame.section.explanationHtml.toString,
      questionSectionFrame.parts match {
        case First(a)  => QuestionCreate.choice
        case Second(a) => QuestionCreate.function
        case Third(a)  => QuestionCreate.sequence
      },
      questionSectionFrame.correctIndex.getOrElse(-1),
      choices,
      functions,
      sequences)
  }

}

// === QuestionPartChoiceJson
case class QuestionPartChoiceJson(summaryRaw: String, summaryHtml: String)

object QuestionPartChoiceJson {

  def apply(summary: String) : QuestionPartChoiceJson = QuestionPartChoiceJson(summary, Markdowner.string(summary))

  def apply(questionPartChoice: QuestionPartChoice) : QuestionPartChoiceJson =
    QuestionPartChoiceJson(
      questionPartChoice.summaryRaw,
      questionPartChoice.summaryHtml.toString)
}

// === QuestionPartFunctionJson
case class QuestionPartFunctionJson(summaryRaw: String, summaryHtml: String, functionRaw: String, functionMath: String)

object QuestionPartFunctionJson {

  def apply(summary: String, function: String) : QuestionPartFunctionJson = QuestionPartFunctionJson(summary, Markdowner.string(summary), function, MathML(function).get.toString)

  def apply(questionPartFunction: QuestionPartFunction) : QuestionPartFunctionJson =
    QuestionPartFunctionJson(
      questionPartFunction.summaryRaw,
      questionPartFunction.summaryHtml.toString,
      questionPartFunction.functionRaw,
      questionPartFunction.functionMath.toString)

}

// === QuestionPartSequenceJson
case class QuestionPartSequenceJson(summaryRaw: String, summaryHtml: String, sequenceStr: String, sequenceMath: String)

object QuestionPartSequenceJson {

  def apply(summary: String, sequence: String, sequenceMath: String) : QuestionPartSequenceJson = QuestionPartSequenceJson(summary, Markdowner.string(summary), sequence, sequenceMath)

  def apply(questionPartSequence: QuestionPartSequence) : QuestionPartSequenceJson =
    QuestionPartSequenceJson(
      questionPartSequence.summaryRaw,
      questionPartSequence.summaryHtml.toString,
      questionPartSequence.sequenceStr,
      questionPartSequence.sequenceMath.stringVersion)

}