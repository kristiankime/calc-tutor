package models.support.form

import models.UserId
import play.api.data.{FormError, Forms, Mapping}
import play.api.data.format.Formatter

// http://workwithplay.com/blog/2013/07/10/advanced-forms-techniques/
object UserIdForm {

  val userIdFormatter = new Formatter[UserId] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], UserId] = {
      data.get(key).map { value =>
        try {
          Right(UserId(value.toLong))
        } catch {
          case e: NumberFormatException => error(key, value + " is not a valid UserId")
        }
      }.getOrElse(error(key, "No UserId provided."))
    }

    private def error(key: String, msg: String) = Left(List(new FormError(key, msg)))

    override def unbind(key: String, value: UserId): Map[String, String] = {
      Map(key -> value.v.toString())
    }
  }

  def userId: Mapping[UserId] = Forms.of[UserId](userIdFormatter)

}
