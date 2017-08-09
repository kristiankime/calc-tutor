package models.quiz

import models.{QuestionId, SectionId}
import play.twirl.api.Html

case class QuestionSection(sectionId: SectionId, questionId: QuestionId, explanationRaw: String, explanationHtml: Html, order: Short)