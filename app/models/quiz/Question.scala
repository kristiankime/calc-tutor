package models.quiz

import com.artclod.slick.JodaUTC
import models.{QuestionId, UserId}
import org.joda.time.DateTime
import play.twirl.api.Html

case class Question(id: QuestionId, ownerId: UserId, title: String, descriptionRaw: String, descriptionHtml: Html, archivedNum: Short = 0, creationDate: DateTime) {
  def isArchived = archivedNum != 0

  def titleArchived = if(isArchived){title + views.html.tag.archivedIcon.apply()}else{title}
}