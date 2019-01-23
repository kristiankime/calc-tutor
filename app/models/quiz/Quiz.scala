package models.quiz

import models.{QuizId, Secured, UserId}
import org.joda.time.DateTime
import play.twirl.api.Html

case class Quiz(id: QuizId, ownerId: UserId, name: String, descriptionRaw: String, descriptionHtml: Html, creationDate: DateTime, updateDate: DateTime) extends Secured