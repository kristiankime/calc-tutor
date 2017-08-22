package models

object AccessName {
  def op(access: Access, pre : Option[String] = Some(" ")) : Option[String] = access match {
    case Non => None
    case other => Some(pre.getOrElse("") + apply(other))
  }

	def apply(access: Access) = access match {
		case Own => "Administrator"
		case Edit => "Teacher"
		case View => "Student"
		case Non => "None"
	}

  def an(access: Access) = access match {
    case Own => "an: Administrator"
    case Edit => "a: Teacher"
    case View => "a: Student"
    case Non => "a: n/a"
  }
}