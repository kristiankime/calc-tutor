package models.organization

import models.{CourseId, QuizId}
import org.joda.time.DateTime

case class Course2Quiz(courseId: CourseId, quizId: QuizId, startDate: Option[DateTime] = None, endDate: Option[DateTime] = None)