package models.quiz

import models.{Access, QuizId, UserId}

case class User2Quiz(userId: UserId, quizId: QuizId, access: Access)