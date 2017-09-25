package models.quiz

import com.artclod.mathml.MathML
import com.artclod.slick.JodaUTC
import controllers.quiz._
import models.UserId
import org.scalatestplus.play._
import play.twirl.api.Html

class AnswerFrameSpec extends PlaySpec {

  val correctNA = Int.MinValue


  val answerJson =
    AnswerJson(AnswerJson.correctNo,
      AnswerSectionJson(AnswerJson.correctYes, 0),
      AnswerSectionJson(AnswerJson.correctYes, AnswerJson.noChoiceSelected, AnswerPartFunctionJson("<cn>1</cn>", AnswerJson.correctYes)),
      AnswerSectionJson(AnswerJson.correctNo, 1),
      AnswerSectionJson(AnswerJson.correctNo, AnswerJson.noChoiceSelected, AnswerPartFunctionJson("<cn>2</cn>", AnswerJson.correctNo))
    )


//	"blank creation" should {
//
//    "create a blank answer, section and part for each" in {
//      // Json
//      //      val answerPartChoiceJson = AnswerPartChoiceJson("summaryRaw", "summaryHtml")
//      val answerSectionJson = AnswerSectionJson("explanationRaw", "explanationHtml", "choice", 0, Vector(answerPartChoiceJson), Vector())
//      val answerJson = AnswerJson("title", "answerRaw", "answerHtml", Vector(answerSectionJson))
//
//      // Scala
//      val answerPartChoice = AnswerPartChoice(null, null, null, "summaryRaw", Html("summaryHtml"), 1, 0)
//      val answerSection = AnswerSection(null, null, "explanationRaw", Html("explanationHtml"), 0)
//      val sectionFrame = AnswerSectionFrame(answerSection, Left(Vector(answerPartChoice)))
//      val answerFrame = AnswerFrame(Answer(null, UserId(0), "title", "answerRaw", Html("answerHtml"), JodaUTC.zero), Vector(sectionFrame))
//
//      // Test
//      AnswerFrame(answerJson, UserId(0), JodaUTC.zero) mustBe (answerFrame)
//    }
//  }

  "combining AnswerJson with QuestionFrame " should {

    "figure out if choices are correct" in {


      val questionPartChoice = QuestionPartChoice(null, null, null, "summaryRaw", Html("summaryHtml"), 1, 0)
      val questionSection = QuestionSection(null, null, "explanationRaw", Html("explanationHtml"), 0)
      val sectionFrame = QuestionSectionFrame(questionSection, Left(Vector(questionPartChoice)))
      val questionFrame = QuestionFrame(Question(null, UserId(0), "title", "questionRaw", Html("questionHtml"), JodaUTC.zero), Vector(sectionFrame))


      val answerFrame = AnswerFrame(null, answerJson, UserId(0), JodaUTC.zero)

      AnswerJson(answerFrame) mustEqual(answerJson)
    }

  }



}
