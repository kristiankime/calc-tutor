package models.quiz

import com.artclod.mathml.MathML
import com.artclod.slick.{JodaUTC, NumericBoolean}
import controllers.quiz.{AnswerSectionJson, _}
import dao.TestData
import dao.TestData.{questionPartChoice, questionPartFunction, questionPartSequence, questionSectionFrameCh, questionSectionFrameFn, questionSectionFrameSe}
import models.UserId
import models.quiz.util.SequenceTokenOrMath
import models.user.User
import org.joda.time.DateTime
import org.scalatestplus.play._
import play.twirl.api.Html

class AnswerFrameSpec extends PlaySpec {


  val someUser = TestData.userWithId(0)
  val someUId = someUser.id
  val correctNA = Int.MinValue
  val blankChoices : Seq[QuestionPartChoice] = Vector()
  val blankFunctions : Seq[QuestionPartFunction] = Vector()
  val blankSequences : Seq[QuestionPartSequence] = Vector()

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

    "figure out if questions is answered correctly with one section with two choices (correct)" in {
      val questionFrame = TestData.questionFrame("title", "description", UserId(0), JodaUTC.zero, Seq(TestData.skill("a")),
        Seq(questionSectionFrameCh("ex")(questionPartChoice("sum 1", NumericBoolean.F), questionPartChoice("sum 2", NumericBoolean.T))) )

      val guessAnswerJson = AnswerJson(correctNA, AnswerSectionJson(correctNA, 1) ) // Here we are guessing 1 which is correct
      val computedAnswerFrame = AnswerFrame(questionFrame, guessAnswerJson, someUser, JodaUTC.zero)
      val correctedAnswerJson = AnswerJson(AnswerJson.correctYes, AnswerSectionJson(AnswerJson.correctYes, 1) )

      AnswerJson(computedAnswerFrame) mustEqual(correctedAnswerJson)
    }

    "figure out if questions is answered correctly with one section with two choices (incorrect)" in {
      val questionFrame = TestData.questionFrame("title", "description", UserId(0), JodaUTC.zero, Seq(TestData.skill("a")),
        Seq( questionSectionFrameCh("ex")(questionPartChoice("sum 1", NumericBoolean.F), questionPartChoice("sum 2", NumericBoolean.T))) )

      val guessAnswerJson = AnswerJson(correctNA, AnswerSectionJson(correctNA, 0) ) // Here we are guessing 0 which is incorrect
      val computedAnswerFrame = AnswerFrame(questionFrame, guessAnswerJson, someUser, JodaUTC.zero)
      val correctedAnswerJson = AnswerJson(AnswerJson.correctNo, AnswerSectionJson(AnswerJson.correctNo, 0) )

      AnswerJson(computedAnswerFrame) mustEqual(correctedAnswerJson)
    }

    "figure out if questions is answered correctly with one section with two functions (correct)" in {
      val questionFrame = TestData.questionFrame("title", "description", UserId(0), JodaUTC.zero, Seq(TestData.skill("a")),
       Seq(questionSectionFrameFn("ex")(questionPartFunction("sum 1","<cn>1</cn>"), questionPartFunction("sum 2", "<cn>2</cn>"))))

      val guessAnswerJson = AnswerJson(correctNA, AnswerSectionJson(correctNA, -1, Vector(AnswerPartFunctionJson("<cn>1</cn>", correctNA), AnswerPartFunctionJson("<cn>2</cn>", correctNA)), Vector()) )
      val computedAnswerFrame = AnswerFrame(questionFrame, guessAnswerJson, someUser, JodaUTC.zero)
      val correctedAnswerJson = AnswerJson(AnswerJson.correctYes, AnswerSectionJson(AnswerJson.correctYes, -1, Vector(AnswerPartFunctionJson("<cn>1</cn>", AnswerJson.correctYes), AnswerPartFunctionJson("<cn>2</cn>", AnswerJson.correctYes)), Vector()) )

      AnswerJson(computedAnswerFrame) mustEqual(correctedAnswerJson)
    }

    "figure out if questions is answered correctly with one section with two functions (one correct one incorrect)" in {
      val questionFrame = TestData.questionFrame("title", "description", UserId(0), JodaUTC.zero, Seq(TestData.skill("a")),
        Seq(questionSectionFrameFn("ex")(questionPartFunction("sum 1","<cn>1</cn>"), questionPartFunction("sum 2", "<cn>3</cn>")))) // Here 3 is wrong
      val guessAnswerJson = AnswerJson(correctNA, AnswerSectionJson(correctNA, -1, Vector(AnswerPartFunctionJson("<cn>1</cn>", correctNA), AnswerPartFunctionJson("<cn>2</cn>", correctNA)), Vector()) )
      val computedAnswerFrame = AnswerFrame(questionFrame, guessAnswerJson, someUser, JodaUTC.zero)
      val correctedAnswerJson = AnswerJson(AnswerJson.correctNo, AnswerSectionJson(AnswerJson.correctNo, -1, Vector(AnswerPartFunctionJson("<cn>1</cn>", AnswerJson.correctYes), AnswerPartFunctionJson("<cn>2</cn>", AnswerJson.correctNo)), Vector()) )

      AnswerJson(computedAnswerFrame) mustEqual(correctedAnswerJson)
    }

    "figure out if questions is answered correctly (all parts are correct)" in {

      val questionFrame = TestData.questionFrame("title", "description", UserId(0), JodaUTC.zero, Seq(TestData.skill("a")),
        Seq(
          questionSectionFrameCh("explanation 1")(questionPartChoice("summary 1-1", NumericBoolean.T)),
          questionSectionFrameFn("explanation 2")(questionPartFunction("summary 2-1", "<cn>1</cn>")),
          questionSectionFrameCh("explanation 3")(questionPartChoice("summary 3-1", NumericBoolean.F), questionPartChoice("summary 3-2", NumericBoolean.T)),
          questionSectionFrameFn("explanation 4")(questionPartFunction("summary 4-1", "<cn>2</cn>"), (questionPartFunction("summary 4-2", "<cn>3</cn>")) ),
          questionSectionFrameSe("explanation 5")(questionPartSequence("summary 5-1", "1;2", """<cn type="integer">1</cn>""" + SequenceTokenOrMath.separator + """<cn type="integer">2</cn>"""))
        )
      )

      val guessAnswerJson =
        AnswerJson(correctNA,
          AnswerSectionJson(correctNA, 0),
          AnswerSectionJson(correctNA, AnswerJson.noChoiceSelected, AnswerPartFunctionJson("<cn>1</cn>", correctNA)),
          AnswerSectionJson(correctNA, 1),
          AnswerSectionJson(correctNA, AnswerJson.noChoiceSelected, AnswerPartFunctionJson("<cn>2</cn>", correctNA), AnswerPartFunctionJson("<cn>3</cn>", correctNA)),
          AnswerSectionJson(correctNA, AnswerJson.noChoiceSelected, Vector(), Vector(AnswerPartSequenceJson("1;2", """<cn type="integer">1</cn>""" + SequenceTokenOrMath.separator + """<cn type="integer">2</cn>""", correctNA)))
        )

      val computedAnswerFrame = AnswerFrame(questionFrame, guessAnswerJson, someUser, JodaUTC.zero)

      val correctedAnswerJson =
        AnswerJson(AnswerJson.correctYes,
          AnswerSectionJson(AnswerJson.correctYes, 0),
          AnswerSectionJson(AnswerJson.correctYes, AnswerJson.noChoiceSelected, AnswerPartFunctionJson("<cn>1</cn>", AnswerJson.correctYes)),
          AnswerSectionJson(AnswerJson.correctYes, 1),
          AnswerSectionJson(AnswerJson.correctYes, AnswerJson.noChoiceSelected, AnswerPartFunctionJson("<cn>2</cn>", AnswerJson.correctYes), AnswerPartFunctionJson("<cn>3</cn>", AnswerJson.correctYes)),
          AnswerSectionJson(AnswerJson.correctYes, AnswerJson.noChoiceSelected, Vector(), Vector(AnswerPartSequenceJson("1;2", """<cn type="integer">1</cn>""" + SequenceTokenOrMath.separator + """<cn type="integer">2</cn>""", AnswerJson.correctYes)))
        )

      AnswerJson(computedAnswerFrame) mustEqual(correctedAnswerJson)
    }

  }



}
