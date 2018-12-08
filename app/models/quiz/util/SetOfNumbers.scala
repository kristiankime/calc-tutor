package models.quiz.util

import com.artclod.mathml.scalar.MathMLElem
import com.artclod.mathml._

import scala.util.{Failure, Success}

case class SetOfNumbers(elements: Seq[MathMLElem]) {
//  def mathEquals(other: SequenceTokenOrMath) = {
//    if(this.elements.size != other.elements.size) {
//      false
//    } else {
//
//      this.elements.zip(other.elements).map(v => {
//        val m = (v._1, v._2) match {
//          case (Left(t),  Left(o) ) => if(t.trim == o.trim) { com.artclod.mathml.Yes } else { com.artclod.mathml.No}
//          case (Right(t), Right(o)) => MathMLEq.checkEq(t, o)
//          case _                    => com.artclod.mathml.No
//        }
//
//        m match {
//          case Yes => true
//          case No => false
//          case Inconclusive => throw new IllegalStateException("Got Inconclusive in SequenceTokenOrMath this should not be possible")
//        }
//
//      }).reduce( (a,b) => a && b )
//
//    }
//  }

  def stringVersion = elements.mkString(SetOfNumbers.separator)
//    elements.map(_ match {
//      case Left(token) => token
//      case Right(math) => math.toString
//    }).mkString(SequenceTokenOrMath.separator)

}


object SetOfNumbers {
  val separator = ";;;"

  def apply(text: String): SetOfNumbers = {
    if(text == null) {
      throw new IllegalArgumentException("text was null")
    } else if(text.isEmpty) {
      throw new IllegalArgumentException("text was empty")
    }

    val elements = text.split(separator).map(MathML(_).get).toVector
    SetOfNumbers(elements);
  }
}