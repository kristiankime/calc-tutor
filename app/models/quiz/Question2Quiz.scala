package models.quiz

import models.{QuestionId, QuizId, UserId}
import org.joda.time.DateTime

case class Question2Quiz(questionId: QuestionId, quizId: QuizId, ownerId: UserId, creationDate: DateTime, order: Int)