package models.quiz

import models.user.User
import models.quiz.util.SetOfNumbers
import models.{QuestionId, QuestionUserConstantId, UserId}
import org.joda.time.DateTime
import play.twirl.api.Html
import controllers.quiz.QuestionCreate.{I_, D_, S_}
import scala.util.Random

trait UserConstant {
  val name : String
  def regexName = name.replace("$", "\\$")
  def matchStr : String =  regexName
  def replaceStr(user: User) : String
  protected def seed(user: User) : Int = user.id.v.toInt * user.name.hashCode
  protected def random(user: User) : Random = new Random(seed(user))
}

case class QuestionUserConstantInteger(id: QuestionUserConstantId, questionId: QuestionId, name: String, lower: Int, upper: Int) extends UserConstant {
  if(lower > upper) throw new IllegalArgumentException("lower > upper (" + lower + "/" + upper + ")")

  override def replaceStr(user: User) = {
    val ran = random(user)
    val value = ran.nextInt(upper - lower) + lower
    "\\$\\$" + value.toString + "\\$\\$"
  }

}

case class QuestionUserConstantDecimal(id: QuestionUserConstantId, questionId: QuestionId, name: String, lower: Double, upper: Double, precision: Int) extends UserConstant{
  if(lower > upper) throw new IllegalArgumentException("lower > upper (" + lower + "/" + upper + ")")
  if(precision < 0) throw new IllegalArgumentException("precision must be non negative")

  override def replaceStr(user: User) = {
    val ran = random(user)
    val value = (ran.nextDouble * (upper - lower)) + lower
    "\\$\\$" + value.toString + "\\$\\$"
  }

}

case class QuestionUserConstantSet(id: QuestionUserConstantId, questionId: QuestionId, name: String, valuesRaw: String, valuesMath: SetOfNumbers) extends UserConstant {

  override def replaceStr(user: User) = {
    val ran = random(user)
    val index = ran.nextInt(valuesMath.elements.size)
    "<math>" + valuesMath.elements(index).toString + "</math>"
  }

}