package models.organization

import models.{CourseId, QuizId}
import org.joda.time.DateTime

case class Course2Quiz(courseId: CourseId, quizId: QuizId, viewHide: Boolean, startDate: Option[DateTime], endDate: Option[DateTime]) {

  def isValidTime(access: models.Access, now: org.joda.time.DateTime) =
    if(access >= models.Edit) {
      true
    } else {
    (startDate, endDate) match {
      case (Some(start), Some(end)) => start.isBefore(now) && end.isAfter(now)
      case (Some(start), None     ) => start.isBefore(now)
      case (None,        Some(end)) =>                        end.isAfter(now)
      case (None,        None     ) => true
    }
  }

  def hide(access: models.Access) = (access <= models.View) && viewHide

  def show(access: models.Access) = !hide(access)

}