package models.quiz

import models.support.HasOrder
import models.{PartId, QuestionId, SectionId}
import play.twirl.api.Html

case class QuestionPartChoice(id: PartId, sectionId: SectionId, questionId: QuestionId, descriptionRaw: String, descriptionHtml: Html, correctChoice: Short, order: Short) extends HasOrder[QuestionPartChoice]