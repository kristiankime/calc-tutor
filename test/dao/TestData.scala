package dao

import java.util.concurrent.TimeUnit

import com.artclod.markup.Markdowner
import com.artclod.mathml.MathML
import com.artclod.slick.JodaUTC
import models.{QuizId, UserId}
import models.organization.{Course, Organization}
import models.quiz.{QuestionPartChoice, QuestionPartFunction, _}
import models.user.User
import org.joda.time.DateTime

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Awaitable}

object TestData {
  val duration = Duration(5, TimeUnit.SECONDS)

  def await[A](awaitable: Awaitable[A]) = Await.result(awaitable, duration)
  def await[A, B](a1: Awaitable[A], a2: Awaitable[B]) = (Await.result(a1, duration), Await.result(a2, duration))
  def await[A, B, C](a1: Awaitable[A], a2: Awaitable[B], a3: Awaitable[C]) = (Await.result(a1, duration), Await.result(a2, duration), Await.result(a3, duration))
  def await[A, B, C, D](a1: Awaitable[A], a2: Awaitable[B], a3: Awaitable[C], a4: Awaitable[D]) = (Await.result(a1, duration), Await.result(a2, duration), Await.result(a3, duration), Await.result(a4, duration))
  def await[A, B, C, D, E](a1: Awaitable[A], a2: Awaitable[B], a3: Awaitable[C], a4: Awaitable[D], a5: Awaitable[E]) = (Await.result(a1, duration), Await.result(a2, duration), Await.result(a3, duration), Await.result(a4, duration), Await.result(a5, duration))

  def user(id: Int) = User(id = null, loginId = "loginId" + id , name = "name" + id, email = None, lastAccess = JodaUTC(0))

  def organization(id: Int) = Organization(name= "name" + id, creationDate=JodaUTC(0), updateDate=JodaUTC(0))

  def course(id: Int, organization : Organization, owner : User) = Course(name="name"+id, organizationId=organization.id, ownerId=owner.id, editCode = "", viewCode = None, creationDate = JodaUTC(0), updateDate = JodaUTC(0))

  def quiz(id: Int, owner : User) = Quiz(id = null, ownerId = owner.id, name = "name"+id, creationDate = JodaUTC(0), updateDate = JodaUTC(0))

  // ===== Question Frame =====
  def questionFrame(title: String, description: String, userId : UserId = null, creationDate : DateTime = JodaUTC.zero)(questionSectionFrames: QuestionSectionFrame*) =
    QuestionFrame(
      Question(null, userId, title, description, Markdowner.html(description), creationDate),
      Vector(questionSectionFrames:_*).zipWithIndex.map(s =>
        if(s._1.order != -1){s._1}
        else{s._1.copy( section = s._1.section.copy(order = s._2.toShort), parts = s._1.parts)})
    )

  def questionSectionFrame(explanation: String, order: Short = -1)(choices: QuestionPartChoice*)(functions: QuestionPartFunction*) =
    QuestionSectionFrame(
      QuestionSection(null, null, explanation, Markdowner.html(explanation), order),
      Vector(choices:_*).zipWithIndex.map(c => if(c._1.order != -1){c._1}else{c._1.copy(order=c._2.toShort)}),
      Vector(functions:_*).zipWithIndex.map(f => if(f._1.order != -1){f._1}else{f._1.copy(order=f._2.toShort)})
    )

  def questionPartChoice(summary: String, correctChoice: Short, order: Short = -1) = QuestionPartChoice(null, null, null, summary, Markdowner.html(summary), correctChoice, order)

  def questionPartFunction(summary: String, function: String, order: Short = -1) = QuestionPartFunction(null, null, null, summary, Markdowner.html(summary), function, MathML(function).get, order)


}
