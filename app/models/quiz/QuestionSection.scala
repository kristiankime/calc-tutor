package models.quiz

import models.{QuestionId, SectionId}
import play.twirl.api.Html

case class QuestionSection(id: SectionId, questionId: QuestionId, explanationRaw: String, explanationHtml: Html, order: Short)