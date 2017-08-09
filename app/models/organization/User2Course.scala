package models.organization

import models.{CourseId, UserId, Access}

case class User2Course(userId: UserId, courseId: CourseId, access: Access)
