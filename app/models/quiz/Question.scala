package models.quiz

import models.{QuestionId, UserId}
import org.joda.time.DateTime
import play.twirl.api.Html

case class Question(id: QuestionId, ownerId: UserId, title: String, descriptionRaw: String, descriptionHtml: Html, creationDate: DateTime)
