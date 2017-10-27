package models.quiz

import models.{SkillId, UserId}

case class UserAnswerCount(userId: UserId, skillId: SkillId, correct: Int, incorrect: Int)