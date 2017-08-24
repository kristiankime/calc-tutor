package models.quiz

import models.{QuizId, Secured, UserId}
import org.joda.time.DateTime

case class Quiz(id: QuizId, ownerId: UserId, name: String, creationDate: DateTime, updateDate: DateTime) extends Secured