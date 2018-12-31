package models.quiz

import com.artclod.mathml.MathML
import com.artclod.mathml.scalar.MathMLElem
import com.artclod.slick.{JodaUTC, NumericBoolean}
import com.artclod.util.ofthree.{First, Second, Third}
import controllers.quiz._
import dao.TestData
import dao.TestData._
import models.UserId
import models.quiz.util.SequenceTokenOrMath
import org.scalatestplus.play._
import play.twirl.api.Html
import models.quiz.UserConstant.EnhancedMathMLElem
import models.quiz.UserConstant.EnhancedHtml

class QuestionUserConstantSpec extends PlaySpec {

	"EnhancedMathMLElem.fixConstants" should {

		"replace an integer constant" in {
      val user = TestData.userWithId(0)
      val uc = QuestionUserConstantInteger(null, null, UserConstant.I_ + 0, 1, 10)
      val ucf = QuestionUserConstantsFrame(Vector(uc), Vector(), Vector())

      val math : MathMLElem = (MathML(<ci>{ uc.name }</ci>).get)
      val fixed = EnhancedMathMLElem(math).fixConstants(user, ucf)

      fixed mustEqual(uc.replaceMathML(user))
		}

    "replace a decimal constant" in {
      val user = TestData.userWithId(0)
      val uc = QuestionUserConstantDecimal(null, null, UserConstant.D_ + 0, 1d, 10d, 2)
      val ucf = QuestionUserConstantsFrame(Vector(), Vector(uc), Vector())

      val math : MathMLElem = (MathML(<ci>{ uc.name }</ci>).get)
      val fixed = EnhancedMathMLElem(math).fixConstants(user, ucf)

      fixed mustEqual(uc.replaceMathML(user))
    }

    "replace a set constant" in {
      val user = TestData.userWithId(0)
      val uc = TestData.userConstantSet(UserConstant.S_ + 0, 1d, 2d, 3d)
      val ucf = QuestionUserConstantsFrame(Vector(), Vector(), Vector(uc))

      val math : MathMLElem = (MathML(<ci>{ uc.name }</ci>).get)
      val fixed = EnhancedMathMLElem(math).fixConstants(user, ucf)

      fixed mustEqual(uc.replaceMathML(user))
    }

    "replace multiple constants" in {
      val user = TestData.userWithId(0)
      val uci = QuestionUserConstantInteger(null, null, UserConstant.I_ + 10, 1, 10)
      val ucd = QuestionUserConstantDecimal(null, null, UserConstant.D_ + 12, 1d, 10d, 2)
      val ucs = TestData.userConstantSet(UserConstant.S_ + 15, 1d, 2d, 3d)

      val ucf = QuestionUserConstantsFrame(Vector(uci), Vector(ucd), Vector(ucs))

      val math : MathMLElem = (MathML(<apply> <plus/> <ci>{ uci.name }</ci> <ci>{ ucd.name }</ci> <ci>{ ucs.name }</ci> </apply>).get)
      val fixed = EnhancedMathMLElem(math).fixConstants(user, ucf)

      fixed mustEqual(MathML(<apply> <plus/> { uci.replaceMathML(user) } { ucd.replaceMathML(user) } { ucs.replaceMathML(user) } </apply>).get)
    }

    "replace unspecified constants with defaults" in {
      val user = TestData.userWithId(0)
      val ucf = QuestionUserConstantsFrame(Vector(), Vector(), Vector())

      // Notice these come after ucf so they are not included
      val uci = UserConstant.defaultUCInteger(UserConstant.I_ + 15)
      val ucd = UserConstant.defaultUCDecimal(UserConstant.D_ + 16)
      val ucs = UserConstant.defaultUCSet(UserConstant.S_ + 17)

      val math : MathMLElem = (MathML(<apply> <plus/> <ci>{ uci.name }</ci> <ci>{ ucd.name }</ci> <ci>{ ucs.name }</ci> </apply>).get)
      val fixed = EnhancedMathMLElem(math).fixConstants(user, ucf)

      fixed mustEqual(MathML(<apply> <plus/> { uci.replaceMathML(user) } { ucd.replaceMathML(user) } { ucs.replaceMathML(user) } </apply>).get)
    }
  }


  def htmlIze(str: String) = Html(str.replace("\\$", "$"))

  "EnhancedHtml.fixConstants" should {

    "replace an integer constant" in {
      val user = TestData.userWithId(0)
      val uc = QuestionUserConstantInteger(null, null, UserConstant.I_ + 0, 1, 10)
      val ucf = QuestionUserConstantsFrame(Vector(uc), Vector(), Vector())

      val html = Html("<p> " + uc.name + " </p>")
      val fixed = EnhancedHtml(html).fixConstants(user, ucf)

      fixed mustEqual(htmlIze("<p> " + uc.replaceStr(user) + " </p>"))
    }

    "replace a decimal constant" in {
      val user = TestData.userWithId(0)
      val uc = QuestionUserConstantDecimal(null, null, UserConstant.D_ + 0, 1d, 10d, 2)
      val ucf = QuestionUserConstantsFrame(Vector(), Vector(uc), Vector())

      val html = Html("<p> " + uc.name + " </p>")
      val fixed = EnhancedHtml(html).fixConstants(user, ucf)

      fixed mustEqual(htmlIze("<p> " + uc.replaceStr(user) + " </p>"))
    }

    "replace a set constant" in {
      val user = TestData.userWithId(0)
      val uc = TestData.userConstantSet(UserConstant.S_ + 0, 1d, 2d, 3d)
      val ucf = QuestionUserConstantsFrame(Vector(), Vector(), Vector(uc))

      val html = Html("<p> " + uc.name + " </p>")
      val fixed = EnhancedHtml(html).fixConstants(user, ucf)

      fixed mustEqual(htmlIze("<p> " + uc.replaceStr(user) + " </p>"))
    }

    "replace multiple constants" in {
      val user = TestData.userWithId(0)
      val uci = QuestionUserConstantInteger(null, null, UserConstant.I_ + 10, 1, 10)
      val ucd = QuestionUserConstantDecimal(null, null, UserConstant.D_ + 12, 1d, 10d, 2)
      val ucs = TestData.userConstantSet(UserConstant.S_ + 15, 1d, 2d, 3d)
      val ucf = QuestionUserConstantsFrame(Vector(uci), Vector(ucd), Vector(ucs))

      val html = Html("<p> " + uci.name + " " + uci.name + " " + uci.name + " </p>")
      val fixed = EnhancedHtml(html).fixConstants(user, ucf)

      fixed mustEqual(htmlIze("<p> " + uci.replaceStr(user) + " " + uci.replaceStr(user) + " " + uci.replaceStr(user) + " </p>"))
    }

    "replace unspecified constants with defaults" in {
      val user = TestData.userWithId(0)
      val ucf = QuestionUserConstantsFrame(Vector(), Vector(), Vector())
      // Notice these come after ucf so they are not included
      val uci = UserConstant.defaultUCInteger(UserConstant.I_ + 15)
      val ucd = UserConstant.defaultUCDecimal(UserConstant.D_ + 16)
      val ucs = UserConstant.defaultUCSet(UserConstant.S_ + 17)

      val html = Html("<p> " + uci.name + " "  + uci.name + " " + uci.name + " </p>")
      val fixed = EnhancedHtml(html).fixConstants(user, ucf)

      fixed mustEqual(htmlIze("<p> " +  uci.replaceStr(user) + " " + uci.replaceStr(user) + " " + uci.replaceStr(user) + " </p>"))
    }
  }
}
