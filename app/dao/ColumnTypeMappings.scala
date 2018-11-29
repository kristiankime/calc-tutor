package dao

import com.artclod.math.Interval
import com.artclod.mathml.{Math, MathML}
import com.artclod.mathml.scalar.{Cn, MathMLElem}
import com.google.common.annotations.VisibleForTesting
import models._
import models.game.GameResponseStatus
import models.quiz.util.{SequenceTokenOrMath, SetOfNumbers}
import org.joda.time.{DateTime, DateTimeZone, Duration}
import play.api.db.slick.HasDatabaseConfigProvider
import play.twirl.api.Html
import slick.driver.JdbcProfile

trait ColumnTypeMappings extends HasDatabaseConfigProvider[JdbcProfile] {

//  override val profile: JdbcProfile = dbConfig.driver
  import profile.api._

  // ==========================
  // Access
  // ==========================
  implicit def short2Access = MappedColumnType.base[Access, Short](
    access => Access.toNum(access),
    short => Access.fromNum(short))

  // ==========================
  // ResponseStatus
  // ==========================
  implicit def short2ResponseStatus = MappedColumnType.base[GameResponseStatus, Short](
    responseStatus => responseStatus.v,
    short => GameResponseStatus(short))

  // ==========================
  // UserId
  // ==========================
  implicit def long2UserId = MappedColumnType.base[UserId, Long](
    id => id.v,
    long => UserId(long))

  // ==========================
  // OrganizationId
  // ==========================
  implicit def long2OrganizationId = MappedColumnType.base[OrganizationId, Long](
    id => id.v,
    long => OrganizationId(long))

  // ==========================
  // CourseId
  // ==========================
  implicit def long2CourseId = MappedColumnType.base[CourseId, Long](
    id => id.v,
    long => CourseId(long))

  // ==========================
  // GameId
  // ==========================
  implicit def long2GameId = MappedColumnType.base[GameId, Long](
    id => id.v,
    long => GameId(long))

  // ==========================
  // QuizId
  // ==========================
  implicit def long2QuizId = MappedColumnType.base[QuizId, Long](
    id => id.v,
    long => QuizId(long))

  // ==========================
  // QuestionId
  // ==========================
  implicit def long2QuestionId = MappedColumnType.base[QuestionId, Long](
    id => id.v,
    long => QuestionId(long))

  // ==========================
  // SectionId
  // ==========================
  implicit def long2SectionId = MappedColumnType.base[QuestionSectionId, Long](
    id => id.v,
    long => QuestionSectionId(long))

  // ==========================
  // PartId
  // ==========================
  implicit def long2PartId = MappedColumnType.base[QuestionPartId, Long](
    id => id.v,
    long => QuestionPartId(long))

  // ==========================
  // QuestionUserConstantId
  // ==========================
  implicit def long2QuestionUserConstantId = MappedColumnType.base[QuestionUserConstantId, Long](
    id => id.v,
    long => QuestionUserConstantId(long))

  // ==========================
  // AnswerId
  // ==========================
  implicit def long2answerId = MappedColumnType.base[AnswerId, Long](
    id => id.v,
    long => AnswerId(long))

  // ==========================
  // AnswerSectionId
  // ==========================
  implicit def long2answerSectionId = MappedColumnType.base[AnswerSectionId, Long](
    id => id.v,
    long => AnswerSectionId(long))

  // ==========================
  // AnswerPartId
  // ==========================
  implicit def long2answerPartId = MappedColumnType.base[AnswerPartId, Long](
    id => id.v,
    long => AnswerPartId(long))

  // ==========================
  // AlertId
  // ==========================
  implicit def long2AlertId = MappedColumnType.base[AlertId, Long](
    id => id.v,
    long => AlertId(long))

  // ==========================
  // AlertId
  // ==========================
  implicit def long2SkillId = MappedColumnType.base[SkillId, Long](
    id => id.v,
    long => SkillId(long))


  // ==========================
  // Vector of Ints
  // ==========================
  implicit def string2VectorInt = MappedColumnType.base[Vector[Int], String](
    vec => vec.mkString(","),
    str => Vector(str.split(",").map(_.toInt):_*))

  // ==========================
  // Vector of Intervals
  // ==========================
  implicit def string2VectorIntervals = MappedColumnType.base[Vector[Interval], String](
    vec => vectorInterval2String(vec),
    str => string2VectorInterval(str))

  @VisibleForTesting
  def vectorInterval2String(vec: Vector[Interval]) = vec.mkString(",")

  @VisibleForTesting
  def string2VectorInterval(str: String) : Vector[Interval] = {
    val split = str.split("""(?<=\)),""") // http://stackoverflow.com/questions/4416425/how-to-split-string-with-some-separator-but-without-removing-that-separator-in-j
    val intervalOps = split.map( s => Interval(s) )
    val intervals = for(internalOp <- intervalOps) yield {
      internalOp match {
        case None => throw new IllegalArgumentException("was unable to parse [" + str + "] as intervals")
        case Some(interval) => interval
      }
    }
    intervals.toVector
  }

  // ==========================
  // Math / MathML
  // ==========================
  implicit def string2mathML = MappedColumnType.base[MathMLElem, String](
    mathML => mathML.toString,
    string => MathML(string).getOrElse(Math(Cn(-123456))))

  implicit def string2Sequence = MappedColumnType.base[SequenceTokenOrMath, String](
    math => math.stringVersion,
    string => SequenceTokenOrMath(string))

  implicit def string2SetNum = MappedColumnType.base[SetOfNumbers, String](
    math => math.stringVersion,
    string => SetOfNumbers(string))

  // ==========================
  // HTML
  // ==========================
  implicit def string2Html = MappedColumnType.base[Html, String](
    html => html.toString,
    string => Html(string))

  // ==========================
  // Joda
  // ==========================
  implicit def long2Duration = MappedColumnType.base[Duration, Long](
    duration => duration.getMillis,
    long => Duration.millis(long))

  implicit def timestamp2DateTime = MappedColumnType.base[DateTime, java.sql.Timestamp](
    dateTime => if(dateTime == null) { null } else { new java.sql.Timestamp(dateTime.getMillis()) },
    date => if(date == null) { null } else { new DateTime(date, DateTimeZone.UTC) } )



}
