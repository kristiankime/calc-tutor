package models.quiz

import com.artclod.slick.JodaUTC
import controllers.quiz.{QuestionJson, QuizJson}
import models.UserId
import org.joda.time.DateTime

case class QuizFrame(quiz: Quiz, questions: Vector[QuestionFrame])

object QuizFrame {

  // ==== JSON to Frame ====
  def apply(ownerId: UserId, quizJson: QuizJson, questions: Vector[QuestionJson], skillMap: Map[String, Skill], now : DateTime = JodaUTC.now): QuizFrame =
    QuizFrame(
      quiz = Quiz(null, ownerId, quizJson.name, now, now),
      questions = questions.map(q => QuestionFrame(q, ownerId, skillMap, now))
    )

}