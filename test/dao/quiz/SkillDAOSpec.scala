package dao.quiz

import dao.TestData
import dao.organization.{CourseDAO, OrganizationDAO}
import dao.user.UserDAO
import models.quiz.Skill
import models.{Edit, Non, Own, View}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import support.EnhancedInjector

class SkillDAOSpec extends PlaySpec with GuiceOneAppPerTest {

  "insertAll" should {

    "insert skills based on name" in {
      val (userDAO, quizDAO, organizationDAO, courseDAO, skillDAO) = app.injector.instanceOf5[UserDAO, QuizDAO, OrganizationDAO, CourseDAO, SkillDAO]

      val skill0 = Skill(null, "0", 0, 0, 0)
      val skill1 = Skill(null, "1", 1, 1, 1)
      skillDAO.insertAll(skill0, skill1)

      val skill0ByName = TestData.await(skillDAO.byName("0")).get
      val skill1ByName = TestData.await(skillDAO.byName("1")).get

      skill0.copy(id = skill0ByName.id) mustBe skill0ByName
      skill1.copy(id = skill1ByName.id) mustBe skill1ByName
    }

  }

}
