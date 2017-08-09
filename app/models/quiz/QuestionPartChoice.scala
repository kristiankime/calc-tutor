package models.quiz

import models.{QuestionId, SectionId}
import play.twirl.api.Html

case class QuestionPartChoice(id: Long, sectionId: SectionId, questionId: QuestionId, descriptionRaw: String, descriptionHtml: Html, correctChoice: Short)