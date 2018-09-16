package models.quiz.util

import org.scalatestplus.play.PlaySpec
import SequenceTokenOrMath.separator
import com.artclod.mathml.MathML
import com.artclod.mathml.scalar.MathMLElem

class SequenceMathOrTokenSpec extends PlaySpec {

  "apply " should {

    "parse tokens" in {
      val sequenceTokenOrMath = SequenceTokenOrMath("a" + separator + "b" )
      sequenceTokenOrMath mustEqual(SequenceTokenOrMath(Seq(Left("a"), Left("b"))))
    }

    "parse math" in {
      val sequenceTokenOrMath = SequenceTokenOrMath("<cn>1</cn>" + separator + "<cn>2</cn>" )
      sequenceTokenOrMath mustEqual(SequenceTokenOrMath(Seq(Right(MathML("<cn>1</cn>").get), Right(MathML("<cn>2</cn>").get))))
    }

    "parse a mixture of tokens and math" in {
      val sequenceTokenOrMath = SequenceTokenOrMath("a" + separator + "<cn>1</cn>" + separator + "<cn>2</cn>" )
      sequenceTokenOrMath mustEqual(SequenceTokenOrMath(Seq(Left("a"), Right(MathML("<cn>1</cn>").get), Right(MathML("<cn>2</cn>").get))))
    }

  }

  "roundtrip" should {

    "work from class to string and back" in {
      val text = "a" + separator + """<cn type="integer">1</cn>""" + separator + """<cn type="integer">2</cn>"""

      SequenceTokenOrMath(text).stringVersion mustEqual(text)
    }

    "work from string to class and back" in {
      val sequenceTokenOrMath = SequenceTokenOrMath(Seq(Left("a"), Right(MathML("<cn>1</cn>").get), Right(MathML("<cn>2</cn>").get)))

      SequenceTokenOrMath(sequenceTokenOrMath.stringVersion) mustEqual(sequenceTokenOrMath)
    }

  }


  "mathEquals" should {

    "be equal for equivalent one math entry" in {
      val oneWay = SequenceTokenOrMath("""<apply> <plus/> <cn>1</cn> <cn>1</cn> </apply>""")
      val anotherWay = SequenceTokenOrMath("""<cn>2</cn>""")

      oneWay.mathEquals(anotherWay) mustEqual (true)
    }

    "not be equal for two math entries to aren't equal" in {
      val oneWay = SequenceTokenOrMath("""<apply> <plus/> <cn>1</cn> <cn>1</cn> </apply>""")
      val anotherWay = SequenceTokenOrMath("""<cn>3</cn>""")

      oneWay.mathEquals(anotherWay) mustEqual (false)
    }

    "be equal for equivalent one token entry modulo trimming" in {
      val oneWay = SequenceTokenOrMath(""" abc """)
      val anotherWay = SequenceTokenOrMath("""abc""")

      oneWay.mathEquals(anotherWay) mustEqual (true)
    }

    "be not equal for not equivalent one token entries " in {
      val oneWay = SequenceTokenOrMath("""abc""")
      val anotherWay = SequenceTokenOrMath("""efg""")

      oneWay.mathEquals(anotherWay) mustEqual (false)
    }

    "be equal for equivalent multiple entries" in {
      val oneWay = SequenceTokenOrMath("""<apply> <plus/> <cn>1</cn> <cn>1</cn> </apply>"""
        + separator + "abc"
        + separator + """<apply> <plus/> <cn>2</cn> <cn>2</cn> </apply>""")

      val anotherWay = SequenceTokenOrMath("""<cn>2</cn>""" + separator + "abc" + separator + "<cn>4</cn>")

      oneWay.mathEquals(anotherWay) mustEqual (true)
    }
  }

}