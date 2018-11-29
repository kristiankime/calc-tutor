package controllers.quiz

import models.quiz.QuizFrame

// -----------
case class QuizJson(name: String, questions: Vector[QuestionJson])

object QuizJson {

  def apply(quizFrame: QuizFrame) : QuizJson =
    QuizJson(
      quizFrame.quiz.name,
      quizFrame.questions.map(QuestionJson(_))
    )

}
