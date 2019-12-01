package dao

import com.artclod.math.Interval
import com.artclod.mathml.Math
import com.artclod.mathml.scalar.Cn
import com.google.common.annotations.VisibleForTesting
import models._
import play.api.mvc.{PathBindable, QueryStringBindable}
import play.twirl.api.Html

/**
 * This object contains all the mappers/binders for the DB (TypeMapper),
 * the urls (PathBindable and QueryStringBindable)
 * and the forms (Mapping)
 * 
 * For a path binding example http://julien.richard-foy.fr/blog/2012/04/09/how-to-implement-a-custom-pathbindable-with-play-2/
 * For a query string binding check out https://gist.github.com/julienrf/2344517
 * For a form binding check out http://workwithplay.com/blog/2013/07/10/advanced-forms-techniques/
 */
package object binders {

  trait HasId[I]{
    def id : I
  }

//	// ==========================
//	// Access
//	// ==========================
//	implicit def short2Access = MappedColumnType.base[Access, Short](
//		access => Access.toNum(access),
//		short => Access.fromNum(short))
//
//  // ==========================
//  // ResponseStatus
//  // ==========================
//  implicit def short2ResponseStatus = MappedColumnType.base[GameResponseStatus, Short](
//    responseStatus => responseStatus.v,
//    short => GameResponseStatus(short))
//
//	// ==========================
//	// UserId
//	// ==========================
  implicit def userId = models.support.form.UserIdForm.userId

//	implicit def long2UserId = MappedColumnType.base[UserId, Long](
//		id => id.v,
//		long => UserId(long))

	implicit def userIdPathBindable(implicit longBinder: PathBindable[Long]) = new PathBindable[UserId] {
		def bind(key: String, value: String): Either[String, UserId] = {
			try { Right(UserId(value.toLong)) }
			catch { case e: NumberFormatException => Left("Could not parse " + value + " as a UserId => " + e.getMessage) }
		}

		def unbind(key: String, id: UserId): String = longBinder.unbind(key, id.v)
	}

  // ==========================
  // OrganizationId
  // ==========================
//  implicit def long2OrganizationId = MappedColumnType.base[OrganizationId, Long](
//    id => id.v,
//    long => OrganizationId(long))

  implicit def organizationIdPathBindable(implicit longBinder: PathBindable[Long]) = new PathBindable[OrganizationId] {
    def bind(key: String, value: String): Either[String, OrganizationId] = {
      try { Right(OrganizationId(value.toLong)) }
      catch { case e: NumberFormatException => Left("Could not parse " + value + " as a OrganizationId => " + e.getMessage) }
    }

    def unbind(key: String, id: OrganizationId): String = longBinder.unbind(key, id.v)
  }

  implicit def organizationIdQueryStringBindable(implicit longBinder: QueryStringBindable[Long]) = new QueryStringBindable[OrganizationId] {
    def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, OrganizationId]] = {
      for { either <- longBinder.bind(key, params) } yield {
        either match {
          case Right(long) => Right(OrganizationId(long))
          case _ => Left("Unable to bind a OrganizationId for key " + key)
        }
      }
    }

    def unbind(key: String, id: OrganizationId): String = longBinder.unbind(key, id.v)
  }

	// ==========================
	// CourseId
	// ==========================
//	implicit def long2CourseId = MappedColumnType.base[CourseId, Long](
//		id => id.v,
//		long => CourseId(long))

	implicit def courseIdPathBindable(implicit longBinder: PathBindable[Long]) = new PathBindable[CourseId] {
		def bind(key: String, value: String): Either[String, CourseId] = {
			try { Right(CourseId(value.toLong)) }
			catch { case e: NumberFormatException => Left("Could not parse " + value + " as a CourseId => " + e.getMessage) }
		}

		def unbind(key: String, id: CourseId): String = longBinder.unbind(key, id.v)
	}

	implicit def courseIdQueryStringBindable(implicit longBinder: QueryStringBindable[Long]) = new QueryStringBindable[CourseId] {
		def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, CourseId]] = {
			for { either <- longBinder.bind(key, params) } yield {
				either match {
					case Right(long) => Right(CourseId(long))
					case _ => Left("Unable to bind a CourseId for key " + key)
				}
			}
		}

		def unbind(key: String, id: CourseId): String = longBinder.unbind(key, id.v)
	}

  // ==========================
  // GameId
  // ==========================
//  implicit def long2GameId = MappedColumnType.base[GameId, Long](
//    id => id.v,
//    long => GameId(long))

  implicit def gameIdPathBindable(implicit longBinder: PathBindable[Long]) = new PathBindable[GameId] {
    def bind(key: String, value: String): Either[String, GameId] = {
      try { Right(GameId(value.toLong)) }
      catch { case e: NumberFormatException => Left("Could not parse " + value + " as a GameId => " + e.getMessage) }
    }

    def unbind(key: String, id: GameId): String = longBinder.unbind(key, id.v)
  }

  implicit def gameIdQueryStringBindable(implicit longBinder: QueryStringBindable[Long]) = new QueryStringBindable[GameId] {
    def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, GameId]] = {
      for { either <- longBinder.bind(key, params) } yield {
        either match {
          case Right(long) => Right(GameId(long))
          case _ => Left("Unable to bind a GameId for key " + key)
        }
      }
    }

    def unbind(key: String, id: GameId): String = longBinder.unbind(key, id.v)
  }

  // ==========================
	// QuizId
	// ==========================
//	implicit def long2QuizId = MappedColumnType.base[QuizId, Long](
//		id => id.v,
//		long => QuizId(long))

	implicit def quizIdPathBindable(implicit longBinder: PathBindable[Long]) = new PathBindable[QuizId] {
		def bind(key: String, value: String): Either[String, QuizId] = {
			try { Right(QuizId(value.toLong)) }
			catch { case e: NumberFormatException => Left("Could not parse " + value + " as a QuizId => " + e.getMessage) }
		}

		def unbind(key: String, id: QuizId): String = longBinder.unbind(key, id.v)
	}

	implicit def quizIdQueryStringBindable(implicit longBinder: QueryStringBindable[Long]) = new QueryStringBindable[QuizId] {
		def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, QuizId]] = {
			for { either <- longBinder.bind(key, params) } yield {
				either match {
					case Right(long) => Right(QuizId(long))
					case _ => Left("Unable to bind a QuizId for key " + key)
				}
			}
		}

		def unbind(key: String, id: QuizId): String = longBinder.unbind(key, id.v)
	}

	// ==========================
	// QuestionId
	// ==========================
  implicit def questionId = models.support.form.QuestionIdForm.userId

//  implicit def long2QuestionId = MappedColumnType.base[QuestionId, Long](
//		id => id.v,
//		long => QuestionId(long))

	implicit def questionIdPathBindable(implicit longBinder: PathBindable[Long]) = new PathBindable[QuestionId] {
		def bind(key: String, value: String): Either[String, QuestionId] = {
			try { Right(QuestionId(value.toLong)) }
			catch { case e: NumberFormatException => Left("Could not parse " + value + " as a QuestionId => " + e.getMessage) }
		}

		def unbind(key: String, id: QuestionId): String = longBinder.unbind(key, id.v)
	}

	implicit def questionIdQueryStringBindable(implicit longBinder: QueryStringBindable[Long]) = new QueryStringBindable[QuestionId] {
		def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, QuestionId]] = {
			for { either <- longBinder.bind(key, params) } yield {
				either match {
					case Right(long) => Right(QuestionId(long))
					case _ => Left("Unable to bind a QuestionId for key " + key)
				}
			}
		}

		def unbind(key: String, id: QuestionId): String = longBinder.unbind(key, id.v)
	}

	// ==========================
	// AnswerId
	// ==========================
//	implicit def long2answerId = MappedColumnType.base[AnswerId, Long](
//		id => id.v,
//		long => AnswerId(long))

	implicit def answerIdPathBindable(implicit longBinder: PathBindable[Long]) = new PathBindable[AnswerId] {
		def bind(key: String, value: String): Either[String, AnswerId] = {
			try { Right(AnswerId(value.toLong)) }
			catch { case e: NumberFormatException => Left("Could not parse " + value + " as a AnswerId => " + e.getMessage) }
		}

		def unbind(key: String, id: AnswerId): String = longBinder.unbind(key, id.v)
	}

  implicit def answerIdQueryStringBindable(implicit longBinder: QueryStringBindable[Long]) = new QueryStringBindable[AnswerId] {
    def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, AnswerId]] = {
      for { either <- longBinder.bind(key, params) } yield {
        either match {
          case Right(long) => Right(AnswerId(long))
          case _ => Left("Unable to bind a AnswerId for key " + key)
        }
      }
    }

    def unbind(key: String, id: AnswerId): String = longBinder.unbind(key, id.v)
  }

//	// ==========================
//	// AlertId
//	// ==========================
//	implicit def long2AlertId = MappedColumnType.base[AlertId, Long](
//		id => id.v,
//		long => AlertId(long))
//
//	// ==========================
//	// Vector of Ints
//	// ==========================
//	implicit def string2VectorInt = MappedColumnType.base[Vector[Int], String](
//		vec => vec.mkString(","),
//		str => Vector(str.split(",").map(_.toInt):_*))
//
//	// ==========================
//	// PolynomialZoneType
//	// ==========================
//	implicit def short2PolynomialZoneType = MappedColumnType.base[PolynomialZoneType, Short](
//		zone => zone.order,
//		short => PolynomialZoneType(short))
//
//	// ==========================
//	// Vector of Intervals
//	// ==========================
//	implicit def string2VectorIntervals = MappedColumnType.base[Vector[Interval], String](
//		vec => vectorInterval2String(vec),
//		str => string2VectorInterval(str))
//
//	@VisibleForTesting
//	def vectorInterval2String(vec: Vector[Interval]) = vec.mkString(",")
//
//	@VisibleForTesting
//	def string2VectorInterval(str: String) : Vector[Interval] = {
//		val split = str.split("""(?<=\)),""") // http://stackoverflow.com/questions/4416425/how-to-split-string-with-some-separator-but-without-removing-that-separator-in-j
//		val intervalOps = split.map( s => Interval(s) )
//		val intervals = for(internalOp <- intervalOps) yield {
//			internalOp match {
//				case None => throw new IllegalArgumentException("was unable to parse [" + str + "] as intervals")
//				case Some(interval) => interval
//			}
//		}
//		intervals.toVector
//	}
//
//	// ==========================
//	// MathML
//	// ==========================
//	implicit def string2mathML = MappedColumnType.base[MathMLElem, String](
//		mathML => mathML.toString,
//		string => MathML(string).getOrElse(Math(Cn(-123456))))
//
//	// ==========================
//	// HTML
//	// ==========================
//	implicit def string2Html = MappedColumnType.base[Html, String](
//		html => html.toString,
//		string => Html(string))

}


