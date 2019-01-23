package models.quiz

import com.artclod.slick.JodaUTC
import controllers.quiz.{QuestionJson, QuizJson}
import models.UserId
import org.joda.time.DateTime
import play.twirl.api.Html

case class QuizFrame(quiz: Quiz, questions: Vector[QuestionFrame])

object QuizFrame {

  // ==== JSON to Frame ====
  def apply(ownerId: UserId, quizJson: QuizJson, skillMap: Map[String, Skill], now : DateTime = JodaUTC.now): QuizFrame =
    QuizFrame(
      quiz = Quiz(null, ownerId, quizJson.name, quizJson.descriptionRaw, Html(quizJson.descriptionHtml), now, now),
      questions = quizJson.questions.map(q => QuestionFrame(q, ownerId, skillMap, now))
    )

}