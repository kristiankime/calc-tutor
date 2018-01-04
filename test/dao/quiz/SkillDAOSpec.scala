package dao.quiz

import dao.TestData
import dao.organization.{CourseDAO, OrganizationDAO}
import dao.user.UserDAO
import models.quiz.{Skill, UserAnswerCount}
import models.{Edit, Non, Own, View}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import support.{CleanDatabaseAfterEach, EnhancedInjector}

class SkillDAOSpec extends PlaySpec with CleanDatabaseAfterEach {

  "insert" should {

    "insert skill based on name" in {
      val (userDAO, skillDAO) = app.injector.instanceOf2[UserDAO, SkillDAO]

      val skill0 = Skill(null, "0", "s0", 0, 0, 0)

      TestData.await(skillDAO.insert(skill0))

      val skill0ByName = TestData.await(skillDAO.byName("0")).get

      skill0.copy(id = skill0ByName.id) mustBe skill0ByName
    }

    "insert skills based on name, two skills" in {
      val (userDAO, skillDAO) = app.injector.instanceOf2[UserDAO, SkillDAO]

      val skill0 = Skill(null, "0", "s0", 0, 0, 0)
      val skill1 = Skill(null, "1", "s1", 1, 1, 1)

      TestData.await(skillDAO.insert(skill0))
      TestData.await(skillDAO.insert(skill1))

      val skill0ByName = TestData.await(skillDAO.byName("0")).get
      val skill1ByName = TestData.await(skillDAO.byName("1")).get

      skill0.copy(id = skill0ByName.id) mustBe skill0ByName
      skill1.copy(id = skill1ByName.id) mustBe skill1ByName
    }

  }

  "insertAll" should {

    "insert skills based on name" in {
      val (userDAO, quizDAO, organizationDAO, courseDAO, skillDAO) = app.injector.instanceOf5[UserDAO, QuizDAO, OrganizationDAO, CourseDAO, SkillDAO]

      val skill0 = Skill(null, "0", "s0", 0, 0, 0)
      val skill1 = Skill(null, "1", "s1", 1, 1, 1)
      skillDAO.insertAll(skill0, skill1)

      val skill0ByName = TestData.await(skillDAO.byName("0")).get
      val skill1ByName = TestData.await(skillDAO.byName("1")).get

      skill0.copy(id = skill0ByName.id) mustBe skill0ByName
      skill1.copy(id = skill1ByName.id) mustBe skill1ByName
    }

  }

  "incrementCount" should {

    "create a record if none exists" in {
      val (userDAO, skillDAO) = app.injector.instanceOf2[UserDAO, SkillDAO]

      val user = TestData.await(userDAO.insert(TestData.user(0)))
      val skill0 = TestData.await(skillDAO.insert(Skill(null, "0", "s0", 0, 0, 0)))

      TestData.await(skillDAO.incrementCount(user.id, skill0.id, 1 , 2))

      TestData.await(skillDAO.getCounts(user.id)).toSet mustBe Set(UserAnswerCount(user.id, skill0.id, 1, 2))
    }

    "update a record if one exists" in {
      val (userDAO, skillDAO) = app.injector.instanceOf2[UserDAO, SkillDAO]

      val user = TestData.await(userDAO.insert(TestData.user(0)))
      val skill0 = TestData.await(skillDAO.insert(Skill(null, "0", "s0", 0, 0, 0)))

      TestData.await(skillDAO.incrementCount(user.id, skill0.id, 1 , 2))
      TestData.await(skillDAO.incrementCount(user.id, skill0.id, 2 , 3))
      TestData.await(skillDAO.getCounts(user.id)).toSet mustBe Set(UserAnswerCount(user.id, skill0.id, 3, 5))
    }

  }

  "incrementCounts" should {

    "create records if none exist" in {
      val (userDAO, skillDAO) = app.injector.instanceOf2[UserDAO, SkillDAO]

      val user = TestData.await(userDAO.insert(TestData.user(0)))
      val skill0 = TestData.await(skillDAO.insert(Skill(null, "0", "s0", 0, 0, 0)))
      val skill1 = TestData.await(skillDAO.insert(Skill(null, "1", "s1", 0, 0, 0)))

      TestData.await(skillDAO.incrementCounts(user.id, (skill0.id, 1 , 2), (skill1.id, 3, 4)))

      TestData.await(skillDAO.getCounts(user.id)).toSet mustBe Set(UserAnswerCount(user.id, skill0.id, 1, 2), UserAnswerCount(user.id, skill1.id, 3, 4))
    }

    "update records if they exist" in {
      val (userDAO, skillDAO) = app.injector.instanceOf2[UserDAO, SkillDAO]

      val user = TestData.await(userDAO.insert(TestData.user(0)))
      val skill0 = TestData.await(skillDAO.insert(Skill(null, "0", "s0", 0, 0, 0)))
      val skill1 = TestData.await(skillDAO.insert(Skill(null, "1", "s1", 0, 0, 0)))

      TestData.await(skillDAO.incrementCounts(user.id, (skill0.id, 1, 2), (skill1.id, 3, 4)))

      val skill2 = TestData.await(skillDAO.insert(Skill(null, "2", "s2", 0, 0, 0)))

      TestData.await(skillDAO.incrementCounts(user.id, (skill0.id, 10, 20), (skill1.id, 30, 40), (skill2.id, 5 , 6)))

      TestData.await(skillDAO.getCounts(user.id)).toSet mustBe Set(UserAnswerCount(user.id, skill0.id, 11, 22), UserAnswerCount(user.id, skill1.id, 33, 44), UserAnswerCount(user.id, skill2.id, 5, 6))
    }

  }

}
