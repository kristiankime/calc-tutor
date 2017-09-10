package models.quiz

import models.support.HasOrder
import models.{QuestionId, QuestionSectionId}
import play.twirl.api.Html

case class QuestionSection(id: QuestionSectionId, questionId: QuestionId, explanationRaw: String, explanationHtml: Html, order: Short) extends HasOrder[QuestionSection]