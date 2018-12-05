package models.quiz

import models.quiz.util.SetOfNumbers
import models.{QuestionId, QuestionUserConstantId, UserId}
import org.joda.time.DateTime
import play.twirl.api.Html

case class QuestionUserConstantInteger(id: QuestionUserConstantId, questionId: QuestionId, name: String, lower: Int, upper: Int) {
  if(lower > upper) throw new IllegalArgumentException("lower > upper (" + lower + "/" + upper + ")")
}

case class QuestionUserConstantDecimal(id: QuestionUserConstantId, questionId: QuestionId, name: String, lower: Double, upper: Double, precision: Int){
  if(lower > upper) throw new IllegalArgumentException("lower > upper (" + lower + "/" + upper + ")")
  if(precision < 0) throw new IllegalArgumentException("precision must be non negative")
}

case class QuestionUserConstantSet(id: QuestionUserConstantId, questionId: QuestionId, name: String, valuesRaw: String, valuesMath: SetOfNumbers)