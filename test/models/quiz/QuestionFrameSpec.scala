package models.quiz

import com.artclod.mathml.MathML
import com.artclod.slick.JodaUTC
import controllers.quiz.{QuestionJson, QuestionPartChoiceJson, QuestionPartFunctionJson, QuestionSectionJson}
import models.{QuestionPartId, QuestionId, QuestionSectionId, UserId}
import org.scalatestplus.play._
import play.twirl.api.Html

import scala.collection.mutable

class QuestionFrameSpec extends PlaySpec {

	"convert to model" should {

		"throw with no sections" in {
      // Scala
      a[java.lang.IllegalArgumentException] must be thrownBy {
        QuestionFrame(Question(null, UserId(0), "title", "questionRaw", Html("questionHtml"), JodaUTC.zero), Vector())
      }
		}

    "convert successfully with one choice section" in {
      // Json
      val questionPartChoiceJson = QuestionPartChoiceJson("summaryRaw", "summaryHtml")
      val questionSectionJson = QuestionSectionJson("explanationRaw", "explanationHtml", "choice", 0, Vector(questionPartChoiceJson), Vector())
      val questionJson = QuestionJson("title", "questionRaw", "questionHtml", Vector(questionSectionJson))

      // Scala
      val questionPartChoice = QuestionPartChoice(null, null, null, "summaryRaw", Html("summaryHtml"), 1, 0)
      val questionSection = QuestionSection(null, null, "explanationRaw", Html("explanationHtml"), 0)
      val sectionFrame = QuestionSectionFrame(questionSection, Left(Vector(questionPartChoice)))
      val questionFrame = QuestionFrame(Question(null, UserId(0), "title", "questionRaw", Html("questionHtml"), JodaUTC.zero), Vector(sectionFrame))

      // Test
      QuestionFrame(questionJson, UserId(0), JodaUTC.zero) mustBe(questionFrame)
    }


    "convert successfully with one function section" in {
      // Json
      val questionPartFunctionJson = QuestionPartFunctionJson("summaryRaw", "summaryHtml", "1", "<cn>1</cn>")
      val questionSectionJson = QuestionSectionJson("explanationRaw", "explanationHtml", "function", 0, Vector(), Vector(questionPartFunctionJson))
      val questionJson = QuestionJson("title", "questionRaw", "questionHtml", Vector(questionSectionJson))

      // Scala
      val questionPartFunction = QuestionPartFunction(null, null, null, "summaryRaw", Html("summaryHtml"), "1", MathML("<cn>1</cn>").get, 0)
      val questionSection = QuestionSection(null, null, "explanationRaw", Html("explanationHtml"), 0)
      val sectionFrame = QuestionSectionFrame(questionSection, Right(Vector(questionPartFunction)))
      val questionFrame = QuestionFrame(Question(null, UserId(0), "title", "questionRaw", Html("questionHtml"), JodaUTC.zero), Vector(sectionFrame))

      // Test
      QuestionFrame(questionJson, UserId(0), JodaUTC.zero) mustBe(questionFrame)
    }
	}

}
