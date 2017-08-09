package models

case class UserId(v: Long) {
  override def toString = "Us"+v
}

case class OrganizationId(v: Long) {
  override def toString = "Or"+v
}

case class CourseId(v: Long) {
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