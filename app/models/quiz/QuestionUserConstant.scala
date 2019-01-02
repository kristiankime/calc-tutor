package models.quiz

import com.artclod.collection
import com.artclod.mathml.scalar.{Ci, Cn, IdentifierText, MathMLElem, NoMathMLChildren, OneMathMLChild, SomeMathMLChildren, TwoMathMLChildren}
import controllers.quiz.QuestionCreate
import models.user.User
import models.quiz.util.SetOfNumbers
import models.{QuestionId, QuestionUserConstantId, UserId}
import org.joda.time.DateTime
import play.twirl.api.Html
import controllers.quiz.QuestionCreate.{D_, I_, S_}

import scala.util.Random
import scala.util.matching.Regex.Match

case class QuestionUserConstantInteger(id: QuestionUserConstantId, questionId: QuestionId, name: String, lower: Int, upper: Int) extends UserConstant {
  if(lower > upper) throw new IllegalArgumentException("lower > upper (" + lower + "/" + upper + ")")

  def replaceValue(user: User) = {
    val ran = random(user)
    val spread = upper - lower
    if(spread == 0) {
      lower
    } else {
      ran.nextInt(upper - lower) + lower
    }
  }

  override def replaceStr(user: User) = {
    "\\$\\$" + replaceValue(user) + "\\$\\$"
  }

  override def replaceMathML(user: User) = {
    Cn(replaceValue(user))
  }
}

case class QuestionUserConstantDecimal(id: QuestionUserConstantId, questionId: QuestionId, name: String, lower: Double, upper: Double, precision: Int) extends UserConstant{
  if(lower > upper) throw new IllegalArgumentException("lower > upper (" + lower + "/" + upper + ")")
  if(precision < 0) throw new IllegalArgumentException("precision must be non negative")

  private def replaceValue(user: User) = {
    val ran = random(user)
    val value = (ran.nextDouble * (upper - lower)) + lower
    com.artclod.math.limitDecimalPlaces(value, precision)
  }

  override def replaceStr(user: User) = {
    val value: Double = replaceValue(user)
    "\\$\\$" + value.toString + "\\$\\$"
  }

  override def replaceMathML(user: User) = {
    Cn(replaceValue(user))
  }
}

case class QuestionUserConstantSet(id: QuestionUserConstantId, questionId: QuestionId, name: String, valuesRaw: String, valuesMath: SetOfNumbers) extends UserConstant {
  private def replaceValue(user: User) = {
    val ran = random(user)
    val index = ran.nextInt(valuesMath.elements.size)
    val elem = valuesMath.elements(index)
    elem
  }

  override def replaceStr(user: User) = {
    val elem: MathMLElem = replaceValue(user)
    "<math>" + elem.toString + "</math>"
  }

  override def replaceMathML(user: User) = {
    replaceValue(user)
  }
}


trait UserConstant {
  val name : String
  def regexName = name.replace("$", "\\$")
  def matchStr : String =  regexName
  def replaceStr(user: User) : String
  def replaceMathML(user: User) : MathMLElem
  protected def seed(user: User) : Int = user.id.v.toInt * name.hashCode
  protected def random(user: User) : Random = new Random(seed(user))
}

object UserConstant {
  val I = "I"
  val D = "D"
  val S = "S"

  val I_ = "$" + I + "$"
  val D_ = "$" + D + "$"
  val S_ = "$" + S + "$"

  val matchAll = "\\$[" + I + D + S + "]\\$[0-9]+"; // Note this is duplicated in calctutor-mathjs.js
  val matchAllReg = matchAll.r

  val matchI = I_.replace("$", "\\$") + "[0-9]+"
  val matchIReg = matchI.r
  val matchD = D_.replace("$", "\\$") + "[0-9]+"
  val matchDReg = matchD.r
  val matchS = S_.replace("$", "\\$") + "[0-9]+"
  val matchSReg = matchS.r

  def defaultUCInteger(name: String) = QuestionUserConstantInteger(null, null, name, 2, 6)
  def defaultUCDecimal(name: String) = QuestionUserConstantDecimal(null, null, name, 2d, 6d, 2)
  private val defaultSetValues = Range(2, 6)
  def defaultUCSet(name: String) = QuestionUserConstantSet(null, null, name, defaultSetValues.mkString(SetOfNumbers.separator), SetOfNumbers(defaultSetValues.map(Cn(_))))

  implicit class EnhancedHtml(html: Html) {
    def fixConstants(user: User, userConstants: QuestionUserConstantsFrame) = {
      val cache = new UserConstantCache(user, userConstants, (uc: UserConstant, u: User) => uc.replaceStr(u))
      val htmlStr = html.toString()
      val retStr = UserConstant.matchAllReg.replaceAllIn(htmlStr, (m : Match) => {
        val matchStr = m.source.subSequence(m.start, m.end).toString
        cache.apply(matchStr)
      })
      Html(retStr)
    }
  }

  implicit class EnhancedMathMLElem(mathMLElem: MathMLElem) {
    def fixConstants(user: User, userConstants: QuestionUserConstantsFrame) : MathMLElem = {
      // Find all <ci> "user constant" </ci> and replace with the right <cn> value </cn>
      updateConstants(new UserConstantCache(user, userConstants, (uc: UserConstant, u: User) => uc.replaceMathML(u)), mathMLElem)
    }

    private def updateConstants(cache: UserConstantCache[MathMLElem], node : MathMLElem) : MathMLElem = node match {
      case Ci(IdentifierText(id @ UserConstant.matchAllReg())) => cache.apply(id)
      case n : NoMathMLChildren => n
      case n : OneMathMLChild => n.copy( updateConstants(cache, n.mathMLChild) )
      case n : TwoMathMLChildren => n.copy( updateConstants(cache, n.mathMLChildren._1), updateConstants(cache, n.mathMLChildren._2) )
      case n : SomeMathMLChildren => n.copy( n.mathMLChildren.map(updateConstants(cache, _)):_* )
    }
  }

}

class UserConstantCache[V](user: User, userConstants: QuestionUserConstantsFrame, toValue: (UserConstant, User) => V) extends (String => V) {
  val cache = _root_.scala.collection.mutable.Map[String, V]();

  override def apply(ucName: String): V = {
    val vt = ucName.trim
    cache.get(vt) match {
      case Some(math) => math
      case None => {
        val ret = math(vt)
        cache.put(vt, ret)
        ret
      }
    }
  }

  // If the value isn't cached compute one using defaults
  def math(ucName: String): V =
    userConstants.constant(ucName) match {
      case Some(const) => toValue(const, user)
      case None => {
        val uc = ucName match {
          case UserConstant.matchIReg() => UserConstant.defaultUCInteger(ucName)
          case UserConstant.matchDReg() => UserConstant.defaultUCDecimal(ucName)
          case UserConstant.matchSReg() => UserConstant.defaultUCSet(ucName)
          case _ => throw new IllegalArgumentException("Could not parse [" + ucName + "] as a user constant name")
        }
        val ret = toValue(uc, user)
        cache.put(ucName, ret)
        ret
      }
    }

}