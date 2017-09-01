package models.quiz

import com.artclod.mathml.MathML
import com.artclod.slick.JodaUTC
import controllers.quiz.{QuestionJson, QuestionPartChoiceJson, QuestionPartFunctionJson, QuestionSectionJson}
import models.{PartId, QuestionId, SectionId, UserId}
import org.scalatestplus.play._
import play.twirl.api.Html

import scala.collection.mutable

class QuestionFrameSpec extends PlaySpec {

	"convert to model" should {

		"convert successfully with no sections" in {
      // Json
      val questionJson = QuestionJson("title", "questionRaw", "questionHtml", Vector())

      // Scala
      val questionFrame = QuestionFrame(Question(null, UserId(0), "title", "questionRaw", Html("questionHtml"), JodaUTC.zero), Vector())

      // Test
      QuestionFrame(questionJson, UserId(0), JodaUTC.zero) mustBe(questionFrame)
		}

    "convert successfully with one choice section" in {
      // Json
      val questionPartChoiceJson = QuestionPartChoiceJson("sec0c0", "summaryRaw", "summaryHtml")
      val questionSectionJson = QuestionSectionJson("sec0", "explanationRaw", "explanationHtml", "choice", "sec0c0", Vector(questionPartChoiceJson), Vector())
      val questionJson = QuestionJson("title", "questionRaw", "questionHtml", Vector(questionSectionJson))

      // Scala
      val questionPartChoice = QuestionPartChoice(null, null, null, "summaryRaw", Html("summaryHtml"), 1, 0)
      val questionSection = QuestionSection(null, null, "explanationRaw", Html("explanationHtml"), 0)
      val sectionFrame = SectionFrame(questionSection, Left(Vector(questionPartChoice)))
      val questionFrame = QuestionFrame(Question(null, UserId(0), "title", "questionRaw", Html("questionHtml"), JodaUTC.zero), Vector(sectionFrame))

      // Test
      QuestionFrame(questionJson, UserId(0), JodaUTC.zero) mustBe(questionFrame)
    }


    "convert successfully with one function section" in {
      // Json
      val questionPartFunctionJson = QuestionPartFunctionJson("sec0f0", "summaryRaw", "summaryHtml", "1", "<cn>1</cn>")
      val questionSectionJson = QuestionSectionJson("sec0", "explanationRaw", "explanationHtml", "function", "sec0f0", Vector(), Vector(questionPartFunctionJson))
      val questionJson = QuestionJson("title", "questionRaw", "questionHtml", Vector(questionSectionJson))

      // Scala
      val questionPartFunction = QuestionPartFunction(null, null, null, "summaryRaw", Html("summaryHtml"), "1", MathML("<cn>1</cn>").get, 0)
      val questionSection = QuestionSection(null, null, "explanationRaw", Html("explanationHtml"), 0)
      val sectionFrame = SectionFrame(questionSection, Right(Vector(questionPartFunction)))
      val questionFrame = QuestionFrame(Question(null, UserId(0), "title", "questionRaw", Html("questionHtml"), JodaUTC.zero), Vector(sectionFrame))

      // Test
      QuestionFrame(questionJson, UserId(0), JodaUTC.zero) mustBe(questionFrame)
    }
	}

}
