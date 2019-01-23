package controllers.quiz

import models.quiz.QuizFrame

// -----------
case class QuizJson(name: String, descriptionRaw: String, descriptionHtml: String, questions: Vector[QuestionJson])

object QuizJson {

  def apply(quizFrame: QuizFrame) : QuizJson =
    QuizJson(
      quizFrame.quiz.name,
      quizFrame.quiz.descriptionRaw,
      quizFrame.quiz.descriptionHtml.toString,
      quizFrame.questions.map(QuestionJson(_))
    )

}
