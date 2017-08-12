package models

sealed trait AccessibleId {
  val v: Long
}

case class UserId(v: Long) {
  override def toString = "Us"+v
}

case class OrganizationId(v: Long) extends AccessibleId {
  override def toString = "Or"+v
}

case class CourseId(v: Long) extends AccessibleId {
  override def toString = "Co"+v
}

case class GameId(v: Long) {
  override def toString = "Ga"+v
}

case class QuizId(v: Long) {
  override def toString = "Qz"+v
}

case class QuestionId(v: Long) {
  override def toString = "Qn"+v
}

case class SectionId(v: Long) {
  override def toString = "Qn"+v
}

case class AnswerId(v: Long) {
  override def toString = "An"+v
}

case class AlertId(v: Long) {
  override def toString = "Al"+v
}

object AccessibleId {

  val organizationIdReg = "Or([0-9]*)".r
  val courseIdReg = "Co([0-9]*)".r

  def fromStr(string: String) : AccessibleId = string match {
    case organizationIdReg(v) => OrganizationId(v.toLong)
    case courseIdReg(v)       =>       CourseId(v.toLong)
    case _                    =>  throw new IllegalArgumentException("Unknown Accessible id pattern [" + string + "]")
  }
}
