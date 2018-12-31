package models.quiz.util

import com.artclod.mathml.{MathML, MathMLEq}
import com.artclod.mathml.scalar.MathMLElem
import com.artclod.mathml.{Inconclusive, No, Yes}
import models.quiz.QuestionUserConstantsFrame
import models.user.User
import models.quiz.UserConstant.EnhancedMathMLElem
import scala.util.{Failure, Success}

case class SequenceTokenOrMath(elements: Seq[Either[String, MathMLElem]]) {
  def mathEquals(other: SequenceTokenOrMath) = {
    if(this.elements.size != other.elements.size) {
      false
    } else {

      this.elements.zip(other.elements).map(v => {
        val m = (v._1, v._2) match {
          case (Left(t),  Left(o) ) => if(t.trim == o.trim) { com.artclod.mathml.Yes } else { com.artclod.mathml.No}
          case (Right(t), Right(o)) => MathMLEq.checkEq(t, o)
          case _                    => com.artclod.mathml.No
        }

        m match {
          case Yes => true
          case No => false
          case Inconclusive => throw new IllegalStateException("Got Inconclusive in SequenceTokenOrMath this should not be possible")
        }

      }).reduce( (a,b) => a && b )

    }
  }

  def stringVersion =
    elements.map(_ match {
      case Left(token) => token
      case Right(math) => math.toString
    }).mkString(SequenceTokenOrMath.separator)


  def fixConstants(user: User, userConstants: QuestionUserConstantsFrame) =
    SequenceTokenOrMath(
      elements.map(_ match {
        case v @ Left(str) => v
        case Right(math) => Right(math.fixConstants(user, userConstants))
      })
    )
}


object SequenceTokenOrMath {
//  val token = "Token :"
//  val math = "Math :"
  val separator = ";;;"

  def apply(text: String): SequenceTokenOrMath = {
    if(text == null) {
      throw new IllegalArgumentException("text was null")
    } else if(text.isEmpty) {
      throw new IllegalArgumentException("text was empty")
    } else if(text.contains(",")) {
      throw new IllegalArgumentException("text had a comma")
    }
    val split = text.split(separator)

    val elements = split.map(v => {
      MathML(v) match {
        case Failure(exception) => Left(v)
        case Success(math)      => Right(math)
      }

//      if(v.startsWith(token)) {
//        Left(v.substring(token.size))
//      } else if (v.startsWith(math)) {
//        Right()
//      } else {
//        throw new IllegalArgumentException("part did not start with a know prefix [" + v + "] know prefixes are [" + token + "] and [" + math + "]")
//      }

    })

    SequenceTokenOrMath(elements);
  }
}