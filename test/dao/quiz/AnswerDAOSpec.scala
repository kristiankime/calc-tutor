package dao.quiz

import _root_.support.{CleanDatabaseAfterEach, EnhancedInjector}
import com.artclod.slick.{JodaUTC, NumericBoolean}
import dao.TestData
import dao.TestData.{questionPartChoice, questionPartFunction, questionSectionFrame}
import dao.organization.{CourseDAO, OrganizationDAO}
import dao.user.UserDAO
import models.quiz.{Answer, Question, Skill, UserAnswerCount}
import org.scalatestplus.play.PlaySpec
import play.twirl.api.Html

class AnswerDAOSpec extends PlaySpec with CleanDatabaseAfterEach {

  "updateSkillCounts" should {

    "update skill counts if the question has not been answered by the user before (correct)" in {
      val (userDAO, quizDAO, organizationDAO, courseDAO, questionDAO, skillDAO, answerDAO) = app.injector.instanceOf7[UserDAO, QuizDAO, OrganizationDAO, CourseDAO, QuestionDAO, SkillDAO, AnswerDAO]


      val user = TestData.await(userDAO.insert(TestData.user(0)))
      val skill0 = TestData.await(skillDAO.insert(Skill(null, "0", "s0", 0, 0, 0)))
      val skill1 = TestData.await(skillDAO.insert(Skill(null, "1", "s1", 0, 0, 0)))

      val question = TestData.await(questionDAO.insert(TestData.questionFrameSimple("q1", userId = user.id, skills = Vector(skill0, skill1))))

      TestData.await(answerDAO.updateSkillCounts(user.id, question.question.id, true))

      TestData.await(skillDAO.getCounts(user.id)).toSet mustBe Set(UserAnswerCount(user.id, skill0.id, 1, 0), UserAnswerCount(user.id, skill1.id, 1, 0))
    }

    "update skill counts if the question has not been answered by the user before (incorrect)" in {
      val (userDAO, quizDAO, organizationDAO, courseDAO, questionDAO, skillDAO, answerDAO) = app.injector.instanceOf7[UserDAO, QuizDAO, OrganizationDAO, CourseDAO, QuestionDAO, SkillDAO, AnswerDAO]


      val user = TestData.await(userDAO.insert(TestData.user(0)))
      val skill0 = TestData.await(skillDAO.insert(Skill(null, "0", "s0", 0, 0, 0)))
      val skill1 = TestData.await(skillDAO.insert(Skill(null, "1", "s1", 0, 0, 0)))

      val question = TestData.await(questionDAO.insert(TestData.questionFrameSimple("q1", userId = user.id, skills = Vector(skill0, skill1))))

      TestData.await(answerDAO.updateSkillCounts(user.id, question.question.id, false))

      TestData.await(skillDAO.getCounts(user.id)).toSet mustBe Set(UserAnswerCount(user.id, skill0.id, 0, 1), UserAnswerCount(user.id, skill1.id, 0, 1))
    }

    "not update skill counts if the question has been answered by the user before" in {
      val (userDAO, quizDAO, organizationDAO, courseDAO, questionDAO, skillDAO, answerDAO) = app.injector.instanceOf7[UserDAO, QuizDAO, OrganizationDAO, CourseDAO, QuestionDAO, SkillDAO, AnswerDAO]

      val user = TestData.await(userDAO.insert(TestData.user(0)))
      val skill0 = TestData.await(skillDAO.insert(Skill(null, "0", "s0", 0, 0, 0)))
      val skill1 = TestData.await(skillDAO.insert(Skill(null, "1", "s1", 0, 0, 0)))

      val question = TestData.await(questionDAO.insert(TestData.questionFrameSimple("q1", userId = user.id, skills = Vector(skill0, skill1))))
      TestData.await(answerDAO.updateSkillCounts(user.id, question.question.id, false))

      val answer = TestData.await(answerDAO.insert(Answer(null, user.id, question.question.id, 0, JodaUTC.zero)))
      TestData.await(answerDAO.updateSkillCounts(user.id, question.question.id, true))

      TestData.await(skillDAO.getCounts(user.id)).toSet mustBe Set(UserAnswerCount(user.id, skill0.id, 0, 1), UserAnswerCount(user.id, skill1.id, 0, 1))
    }

  }

  "numberOfAttempts" should {

    "be 0 when the user has never answered" in {
      val (userDAO, quizDAO, organizationDAO, courseDAO, questionDAO, skillDAO, answerDAO) = app.injector.instanceOf7[UserDAO, QuizDAO, OrganizationDAO, CourseDAO, QuestionDAO, SkillDAO, AnswerDAO]

      val user = TestData.await(userDAO.insert(TestData.user(0)))
      val skill0 = TestData.await(skillDAO.insert(Skill(null, "0", "s0", 0, 0, 0)))
      val skill1 = TestData.await(skillDAO.insert(Skill(null, "1", "s1", 0, 0, 0)))

      val question = TestData.await(questionDAO.insert(TestData.questionFrameSimple("q1", userId = user.id, skills = Vector(skill0, skill1))))
      TestData.await(answerDAO.updateSkillCounts(user.id, question.question.id, false))

      TestData.await(answerDAO.numberOfAttempts(user.id, question.question.id)) mustBe 0
    }

    "be 1 when the user has answered once" in {
      val (userDAO, quizDAO, organizationDAO, courseDAO, questionDAO, skillDAO, answerDAO) = app.injector.instanceOf7[UserDAO, QuizDAO, OrganizationDAO, CourseDAO, QuestionDAO, SkillDAO, AnswerDAO]

      val user = TestData.await(userDAO.insert(TestData.user(0)))
      val skill0 = TestData.await(skillDAO.insert(Skill(null, "0", "s0", 0, 0, 0)))
      val skill1 = TestData.await(skillDAO.insert(Skill(null, "1", "s1", 0, 0, 0)))

      val question = TestData.await(questionDAO.insert(TestData.questionFrameSimple("q1", userId = user.id, skills = Vector(skill0, skill1))))
      TestData.await(answerDAO.updateSkillCounts(user.id, question.question.id, false))

      val answer = TestData.await(answerDAO.insert(Answer(null, user.id, question.question.id, 0, JodaUTC.zero)))
      TestData.await(answerDAO.updateSkillCounts(user.id, question.question.id, true))

      TestData.await(answerDAO.numberOfAttempts(user.id, question.question.id)) mustBe 1
    }

  }


}
