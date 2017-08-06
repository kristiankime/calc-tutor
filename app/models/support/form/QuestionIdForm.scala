package models.support.form

import models.QuestionId
import play.api.data.{FormError, Forms, Mapping}
import play.api.data.format.Formatter

// http://workwithplay.com/blog/2013/07/10/advanced-forms-techniques/
object QuestionIdForm {

  val userIdFormatter = new Formatter[QuestionId] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], QuestionId] = {
      data.get(key).map { value =>
        try {
          Right(QuestionId(value.toLong))
        } catch {
          case e: NumberFormatException => error(key, value + " is not a valid QuestionId")
        }
      }.getOrElse(error(key, "No QuestionId provided."))
    }

    private def error(key: String, msg: String) = Left(List(new FormError(key, msg)))

    override def unbind(key: String, value: QuestionId): Map[String, String] = {
      Map(key -> value.v.toString())
    }
  }

  def userId: Mapping[QuestionId] = Forms.of[QuestionId](userIdFormatter)

}
