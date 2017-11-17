package models.quiz

import com.artclod.mathml.MathML
import com.artclod.slick.{JodaUTC, NumericBoolean}
import controllers.quiz
import controllers.quiz.{QuestionJson, QuestionPartChoiceJson, QuestionPartFunctionJson, QuestionSectionJson}
import dao.TestData
import models.UserId
import org.scalatestplus.play._
import play.twirl.api.Html
import dao.TestData._

class QuestionFrameSpec extends PlaySpec {

	"convert JSON to model" should {

		"throw with no sections" in {
      // Scala
      a[java.lang.IllegalArgumentException] must be thrownBy {
        QuestionFrame(Question(null, UserId(0), "title", "questionRaw", Html("questionHtml"), JodaUTC.zero), Vector(), Vector(TestData.skill("a")))
      }
		}

    "throw with no skills" in {
      // Scala
      a[java.lang.IllegalArgumentException] must be thrownBy {
        val questionPartChoice = QuestionPartChoice(null, null, null, "summaryRaw", Html("summaryHtml"), 1, 0)
        val questionSection = QuestionSection(null, null, "explanationRaw", Html("explanationHtml"), 0)
        val sectionFrame = QuestionSectionFrame(questionSection, Left(Vector(questionPartChoice)))
        val questionFrame = QuestionFrame(Question(null, UserId(0), "title", "questionRaw", Html("questionHtml"), JodaUTC.zero), Vector(sectionFrame), Vector())
      }
    }

    "convert successfully with one choice section" in {
      // Json
      val questionPartChoiceJson = QuestionPartChoiceJson("summaryRaw", "summaryHtml")
      val questionSectionJson = QuestionSectionJson("explanationRaw", "explanationHtml", "choice", 0, Vector(questionPartChoiceJson), Vector())
      val questionJson = QuestionJson("title", "questionRaw", "questionHtml", Vector(questionSectionJson), Vector("a"))

      // Scala
      val skills = Vector(TestData.skill("a"))
      val skillMap = skills.groupBy(_.name).mapValues(_.head)
      val questionPartChoice = QuestionPartChoice(null, null, null, "summaryRaw", Html("summaryHtml"), 1, 0)
      val questionSection = QuestionSection(null, null, "explanationRaw", Html("explanationHtml"), 0)
      val sectionFrame = QuestionSectionFrame(questionSection, Left(Vector(questionPartChoice)))
      val questionFrame = QuestionFrame(Question(null, UserId(0), "title", "questionRaw", Html("questionHtml"), JodaUTC.zero), Vector(sectionFrame), skills)

      // Test
      QuestionFrame(questionJson, UserId(0), skillMap, JodaUTC.zero) mustBe(questionFrame)
    }

    "convert successfully with one function section" in {
      // Json
      val questionPartFunctionJson = QuestionPartFunctionJson("summaryRaw", "summaryHtml", "1", "<cn>1</cn>")
      val questionSectionJson = QuestionSectionJson("explanationRaw", "explanationHtml", "function", 0, Vector(), Vector(questionPartFunctionJson))
      val questionJson = QuestionJson("title", "questionRaw", "questionHtml", Vector(questionSectionJson), Vector("a"))

      // Scala
      val skills = Vector(TestData.skill("a"))
      val skillMap = skills.groupBy(_.name).mapValues(_.head)
      val questionPartFunction = QuestionPartFunction(null, null, null, "summaryRaw", Html("summaryHtml"), "1", MathML("<cn>1</cn>").get, 0)
      val questionSection = QuestionSection(null, null, "explanationRaw", Html("explanationHtml"), 0)
      val sectionFrame = QuestionSectionFrame(questionSection, Right(Vector(questionPartFunction)))
      val questionFrame = QuestionFrame(Question(null, UserId(0), "title", "questionRaw", Html("questionHtml"), JodaUTC.zero), Vector(sectionFrame), skills)

      // Test
      QuestionFrame(questionJson, UserId(0), skillMap, JodaUTC.zero) mustBe(questionFrame)
    }

    "convert successfully with one multiple sections" in {
      // Json
      val questionJson =
        QuestionJson("title", "description",
          Vector(
            QuestionSectionJson("explanation 1", 0)(QuestionPartChoiceJson("summary 1-1"))(),
            QuestionSectionJson("explanation 2")()(QuestionPartFunctionJson("summary 2-1", "<cn>1</cn>")),
            QuestionSectionJson("explanation 3", 1)(QuestionPartChoiceJson("summary 3-1"), QuestionPartChoiceJson("summary 3-2"))(),
            QuestionSectionJson("explanation 4")()(QuestionPartFunctionJson("summary 4-1", "<cn>2</cn>"), QuestionPartFunctionJson("summary 4-2", "<cn>3</cn>"))
          ),
          Vector("a")
        )

      // Scala
      val skills = Vector(TestData.skill("a"))
      val skillMap = skills.groupBy(_.name).mapValues(_.head)
      val questionFrame = TestData.questionFrame("title", "description", UserId(0), JodaUTC.zero,
      skills,
      Seq(
        questionSectionFrame("explanation 1")(questionPartChoice("summary 1-1", NumericBoolean.T))(),
        questionSectionFrame("explanation 2")()(questionPartFunction("summary 2-1", "<cn>1</cn>")),
        questionSectionFrame("explanation 3")(questionPartChoice("summary 3-1", NumericBoolean.F), questionPartChoice("summary 3-2", NumericBoolean.T))(),
        questionSectionFrame("explanation 4")()(questionPartFunction("summary 4-1", "<cn>2</cn>"), (questionPartFunction("summary 4-2", "<cn>3</cn>")))
      ))

      // Test
      QuestionFrame(questionJson, UserId(0), skillMap, JodaUTC.zero) mustBe(questionFrame)
    }

    "convert successfully with multiple skills" in {
      // Json
      val questionPartChoiceJson = QuestionPartChoiceJson("summaryRaw", "summaryHtml")
      val questionSectionJson = QuestionSectionJson("explanationRaw", "explanationHtml", "choice", 0, Vector(questionPartChoiceJson), Vector())
      val questionJson = QuestionJson("title", "questionRaw", "questionHtml", Vector(questionSectionJson), Vector("a", "b", "c"))

      // Scala
      val skills = Vector(TestData.skill("a"), TestData.skill("b"), TestData.skill("c"))
      val skillMap = skills.groupBy(_.name).mapValues(_.head)
      val questionPartChoice = QuestionPartChoice(null, null, null, "summaryRaw", Html("summaryHtml"), 1, 0)
      val questionSection = QuestionSection(null, null, "explanationRaw", Html("explanationHtml"), 0)
      val sectionFrame = QuestionSectionFrame(questionSection, Left(Vector(questionPartChoice)))
      val questionFrame = QuestionFrame(Question(null, UserId(0), "title", "questionRaw", Html("questionHtml"), JodaUTC.zero), Vector(sectionFrame), skills)

      // Test
      QuestionFrame(questionJson, UserId(0), skillMap, JodaUTC.zero) mustBe(questionFrame)
    }
	}

  "json roundtrip" should {

    "return to initial Json" in {
      // Json
      val questionJson =
        QuestionJson("title", "description",
          Vector(
            QuestionSectionJson("explanation 1", 0)(QuestionPartChoiceJson("summary 1-1"))(),
            QuestionSectionJson("explanation 2")()(QuestionPartFunctionJson("summary 2-1", "<cn>1</cn>")),
            QuestionSectionJson("explanation 3", 1)(QuestionPartChoiceJson("summary 3-1"), QuestionPartChoiceJson("summary 3-2"))(),
            QuestionSectionJson("explanation 4")()(QuestionPartFunctionJson("summary 4-1", "<cn>2</cn>"), QuestionPartFunctionJson("summary 4-2", "<cn>3</cn>"))
          ),
          Vector("a")
        )

      // Scala
      val skills = Vector(TestData.skill("a"))
      val skillMap = skills.groupBy(_.name).mapValues(_.head)
      val questionFrame = QuestionFrame(questionJson, UserId(0), skillMap, JodaUTC.zero)

      QuestionJson(questionFrame) mustEqual(questionJson)
    }

  }

}
