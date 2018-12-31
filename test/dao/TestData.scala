package dao

import java.util.concurrent.TimeUnit

import com.artclod.markup.Markdowner
import com.artclod.mathml.MathML
import com.artclod.mathml.scalar.Cn
import com.artclod.slick.{JodaUTC, NumericBoolean}
import com.artclod.util.OneOfThree
import com.artclod.util.ofthree.{First, Second, Third}
import models._
import models.organization.{Course, Organization}
import models.quiz.util.{SequenceTokenOrMath, SetOfNumbers}
import models.quiz.{QuestionFrame, QuestionPartChoice, QuestionPartFunction, _}
import models.user.User
import org.joda.time.DateTime

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Awaitable}

object TestData {
  val duration = Duration(5000, TimeUnit.SECONDS)

  def await[A](awaitable: Awaitable[A]) = Await.result(awaitable, duration)
  def await[A, B](a1: Awaitable[A], a2: Awaitable[B]) = (Await.result(a1, duration), Await.result(a2, duration))
  def await[A, B, C](a1: Awaitable[A], a2: Awaitable[B], a3: Awaitable[C]) = (Await.result(a1, duration), Await.result(a2, duration), Await.result(a3, duration))
  def await[A, B, C, D](a1: Awaitable[A], a2: Awaitable[B], a3: Awaitable[C], a4: Awaitable[D]) = (Await.result(a1, duration), Await.result(a2, duration), Await.result(a3, duration), Await.result(a4, duration))
  def await[A, B, C, D, E](a1: Awaitable[A], a2: Awaitable[B], a3: Awaitable[C], a4: Awaitable[D], a5: Awaitable[E]) = (Await.result(a1, duration), Await.result(a2, duration), Await.result(a3, duration), Await.result(a4, duration), Await.result(a5, duration))

  def user(id: Int) = User(id = null, loginId = "loginId" + id , name = "name" + id, email = None, lastAccess = JodaUTC(0))

  def userWithId(id: Int) = User(id = UserId(id), loginId = "loginId" + id , name = "name" + id, email = None, lastAccess = JodaUTC(0))

  def organization(id: Int) = Organization(name= "name" + id, creationDate=JodaUTC(0), updateDate=JodaUTC(0))

  def course(id: Int, organization : Organization, owner : User) = Course(name="name"+id, organizationId=organization.id, ownerId=owner.id, editCode = "", viewCode = None, creationDate = JodaUTC(0), updateDate = JodaUTC(0))

  def quiz(id: Int, owner : User) = Quiz(id = null, ownerId = owner.id, name = "name"+id, creationDate = JodaUTC(0), updateDate = JodaUTC(0))

  def skill(name: String) = Skill(null, name, name.substring(0,1), 1d, 1d, 1d)

  // ===== Question Frame =====
  def questionFrameSimple(info: String, userId : UserId = null, creationDate : DateTime = JodaUTC.zero, skills: Seq[Skill] = Seq(), archive : Short = 0): QuestionFrame = {
    val section = questionSectionFrameFn(info + " explanation", 0)(questionPartFunction(info + "summary", "<cn>1</cn>", 0))
    questionFrame(title = info + " title", description = info +" description", userId  = userId, creationDate = creationDate, skills = skills, Seq(section), archive = archive)
  }

  def questionFrame(title: String, description: String, userId : UserId = null, creationDate : DateTime = JodaUTC.zero, skills: Seq[Skill], questionSectionFrames: Seq[QuestionSectionFrame], archive : Short = 0, userConstants: QuestionUserConstantsFrame = QuestionUserConstantsFrame.empty) : QuestionFrame =
    QuestionFrame(
      Question(null, userId, title, description, Markdowner.html(description), archive, creationDate),
      Vector(questionSectionFrames:_*).zipWithIndex.map(s =>
        if(s._1.order != -1){s._1}
        else{s._1.copy( section = s._1.section.copy(order = s._2.toShort), parts = s._1.parts)}),
      Vector(skills:_*),
      userConstants
    )


  // --- User Constants ---
  def userConstantSet(name: String, values: Double*) = QuestionUserConstantSet(null, null, name, values.mkString(SetOfNumbers.separator), SetOfNumbers(values.map(Cn(_))))

  // --- Section Frame ---
//  def questionSectionFrame(explanation: String, order: Short = -1)(choices: QuestionPartChoice*)(functions: QuestionPartFunction*)(sequences: QuestionPartSequence*) =
//    QuestionSectionFrame(
//      QuestionSection(null, null, explanation, Markdowner.html(explanation), order),
//      Vector(choices:_*).zipWithIndex.map(c => if(c._1.order != -1){c._1}else{c._1.copy(order=c._2.toShort)}),
//      Vector(functions:_*).zipWithIndex.map(f => if(f._1.order != -1){f._1}else{f._1.copy(order=f._2.toShort)}),
//      Vector(sequences:_*).zipWithIndex.map(f => if(f._1.order != -1){f._1}else{f._1.copy(order=f._2.toShort)})
//    )

  def questionSectionFrameCh(explanation: String, order: Int = -1)(choices: QuestionPartChoice*) =
    QuestionSectionFrame(
      QuestionSection(null, null, explanation, Markdowner.html(explanation), order.toShort),
      Vector(choices:_*).zipWithIndex.map(c => if(c._1.order != -1){c._1}else{c._1.copy(order=c._2.toShort)}),
      Vector(),
      Vector()
    )

  def questionSectionFrameFn(explanation: String, order: Int = -1)(functions: QuestionPartFunction*) =
    QuestionSectionFrame(
      QuestionSection(null, null, explanation, Markdowner.html(explanation), order.toShort),
      Vector(),
      Vector(functions:_*).zipWithIndex.map(f => if(f._1.order != -1){f._1}else{f._1.copy(order=f._2.toShort)}),
      Vector()
    )

  def questionSectionFrameSe(explanation: String, order: Int = -1)(sequences: QuestionPartSequence*) =
    QuestionSectionFrame(
      QuestionSection(null, null, explanation, Markdowner.html(explanation), order.toShort),
      Vector(),
      Vector(),
      Vector(sequences:_*).zipWithIndex.map(f => if(f._1.order != -1){f._1}else{f._1.copy(order=f._2.toShort)})
    )

  def questionPartChoice(summary: String, correctChoice: Short, order: Short = -1) = QuestionPartChoice(null, null, null, summary, Markdowner.html(summary), correctChoice, order)

  def questionPartFunction(summary: String, function: String, order: Short = -1) = QuestionPartFunction(null, null, null, summary, Markdowner.html(summary), function, MathML(function).get, order)

  def questionPartSequence(summary: String, sequenceStr: String, sequenceMath: String, order: Short = -1) = QuestionPartSequence(null, null, null, summary, Markdowner.html(summary), sequenceStr, SequenceTokenOrMath(sequenceMath), order)

  // --- update ids ---
  def copyIds(questionFrameNoIds: QuestionFrame, questionFrameWithIds: QuestionFrame) : QuestionFrame =  {
    val questionFrame = questionFrameNoIds.id(questionFrameWithIds.question.id)
    questionFrame.copy(sections = copyIds(questionFrame.sections, questionFrameWithIds.sections))
  }

  def copyIds(questionSectionsNoIds: Vector[QuestionSectionFrame], questionSectionWithIds: Vector[QuestionSectionFrame]) : Vector[QuestionSectionFrame] = {
    if(questionSectionsNoIds.size != questionSectionWithIds.size) { throw new IllegalArgumentException("sections size didn't match"); }

    questionSectionsNoIds.zip(questionSectionWithIds).map(e => copyIds(e._1, e._2))
  }

  def copyIds(questionSectionNoIds: QuestionSectionFrame, questionSectionWithIds: QuestionSectionFrame) : QuestionSectionFrame =  {
    val questionSection = questionSectionNoIds.id(questionSectionWithIds.section.id)
    val parts : OneOfThree[ Vector[QuestionPartChoice], Vector[QuestionPartFunction], Vector[QuestionPartSequence] ] = (questionSection.parts, questionSectionWithIds.parts) match {
      case (First(left), First(right)) => First(copyChoiceIds(left, right))
      case (Second(left), Second(right)) => Second(copyFunctionIds(left, right))
      case (Third(left), Third(right)) => Third(copySequenceIds(left, right))
      case (_, _) => throw new IllegalArgumentException("part types did not match")
    }
    questionSection.copy(parts = parts)
  }

  def copyChoiceIds(partsNoIds: Vector[QuestionPartChoice], partsWithIds: Vector[QuestionPartChoice]) : Vector[QuestionPartChoice] =  {
    if(partsNoIds.size != partsWithIds.size) { throw new IllegalArgumentException("sections size didn't match"); }
    partsNoIds.zip(partsWithIds).map(e => e._1.copy(id = e._2.id))
  }

  def copyFunctionIds(partsNoIds: Vector[QuestionPartFunction], partsWithIds: Vector[QuestionPartFunction]) : Vector[QuestionPartFunction] =  {
    if(partsNoIds.size != partsWithIds.size) { throw new IllegalArgumentException("sections size didn't match"); }
    partsNoIds.zip(partsWithIds).map(e => e._1.copy(id = e._2.id))
  }

  def copySequenceIds(partsNoIds: Vector[QuestionPartSequence], partsWithIds: Vector[QuestionPartSequence]) : Vector[QuestionPartSequence] =  {
    if(partsNoIds.size != partsWithIds.size) { throw new IllegalArgumentException("sections size didn't match"); }
    partsNoIds.zip(partsWithIds).map(e => e._1.copy(id = e._2.id))
  }

  // ===== Answer Frame =====
  def answerFrameSimple(questionFrame: QuestionFrame, userId : UserId, correct: Boolean, creationDate : DateTime = JodaUTC.zero): AnswerFrame = {
    // Here we assume the question was created by questionFrameSimple
    val qSection0 = questionFrame.sections(0)
    val qSection0Part0 = qSection0.parts.second.get(0)

    val part = answerPart(if(correct){"<cn>1</cn>"}else{"<cn>0</cn>"}, qSection0Part0.id, qSection0.section.id, questionFrame.question.id, correct, 0)
    val section = answerSection(None, qSection0.section.id, questionFrame.question.id, correct, 0)
    val sections = Vector(AnswerSectionFrame(section, Vector(part), Vector(), false))
    val answer = Answer(null, userId, questionFrame.question.id,  NumericBoolean(correct), creationDate)
    AnswerFrame(answer, sections, false)
  }

  def answerSection(choice: Option[Short], questionSectionId : QuestionSectionId, questionId: QuestionId, correct : Boolean, order: Short) = AnswerSection(null, null, questionSectionId, questionId, choice, NumericBoolean(correct), order)

  def answerPart(function: String, questionPartId: QuestionPartId, questionSectionId : QuestionSectionId, questionId: QuestionId, correct : Boolean, order: Short = -1) = AnswerPartFunction(null, null, null, questionPartId, questionSectionId, questionId, function, MathML(function).get, NumericBoolean(correct), order)

}
