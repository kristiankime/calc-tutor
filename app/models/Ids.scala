package models

import models.AccessibleId._

sealed trait AccessibleId {
  val v: Long
}

case class UserId(v: Long) {
  override def toString = "Us"+v
}

case class OrganizationId(v: Long) extends AccessibleId {
  override def toString = organizationPrefix+v
}

case class CourseId(v: Long) extends AccessibleId {
  override def toString = coursePrefix+v
}

case class GameId(v: Long) {
  override def toString = "Ga"+v
}

case class QuizId(v: Long) extends AccessibleId {
  override def toString = quizPrefix+v
}

case class QuestionId(v: Long) {
  override def toString = "Qn"+v
}

case class QuestionSectionId(v: Long) {
  override def toString = "Sn"+v
}

case class QuestionPartId(v: Long) {
  override def toString = "Pa"+v
}

case class AnswerId(v: Long) {
  override def toString = "An"+v
}

case class AnswerSectionId(v: Long) {
  override def toString = "As"+v
}

case class AnswerPartId(v: Long) {
  override def toString = "Ap"+v
}

case class AlertId(v: Long) {
  override def toString = "Al"+v
}

case class SkillId(v: Long) extends AccessibleId {
  override def toString = "Sk"+v
}


object AccessibleId {
  val organizationPrefix = "Or"
  val coursePrefix = "Co"
  val quizPrefix = "Qz"

  val organizationIdReg = (organizationPrefix + "([0-9]*)").r
  val courseIdReg = (coursePrefix + "([0-9]*)").r
  val quizIdReg = (quizPrefix + "([0-9]*)").r

  def fromStr(string: String) : AccessibleId = string match {
    case organizationIdReg(v) => OrganizationId(v.toLong)
    case courseIdReg(v)       => CourseId(v.toLong)
    case quizIdReg(v)         => QuizId(v.toLong)
    case _                    => throw new IllegalArgumentException("Unknown Accessible id pattern [" + string + "]")
  }
}
