package dao.quiz

import com.artclod.slick.{JodaUTC, NumericBoolean}
import dao.TestData
import dao.TestData.{questionPartChoice, questionPartFunction, questionSectionFrame}
import dao.organization.{CourseDAO, OrganizationDAO}
import dao.user.UserDAO
import models._
import org.scalatestplus.play.PlaySpec
import _root_.support.{CleanDatabaseAfterEach, EnhancedInjector}

class QuestionDAOSpec extends PlaySpec with CleanDatabaseAfterEach {

  "byId" should {

    "return question" in {
      val (userDAO, quizDAO, organizationDAO, courseDAO, questionDAO, skillDAO) = app.injector.instanceOf6[UserDAO, QuizDAO, OrganizationDAO, CourseDAO, QuestionDAO, SkillDAO]
      val user = TestData.await(userDAO.insert(TestData.user(0)))

      // Setup the skills
      val skillsNoId = Vector(TestData.skill("a"), TestData.skill("b"))
      skillDAO.insertAll(skillsNoId:_*)
      val skills = TestData.await(skillDAO.allSkills)

      // Create the question
      val questionFrame = TestData.questionFrame("title", "description", user.id, JodaUTC.zero,
        skills,
        Seq(
          questionSectionFrame("explanation 1")(questionPartChoice("summary 1-1", NumericBoolean.T))(),
          questionSectionFrame("explanation 2")()(questionPartFunction("summary 2-1", "<cn>1</cn>")),
          questionSectionFrame("explanation 3")(questionPartChoice("summary 3-1", NumericBoolean.F), questionPartChoice("summary 3-2", NumericBoolean.T))(),
          questionSectionFrame("explanation 4")()(questionPartFunction("summary 4-1", "<cn>2</cn>"), (questionPartFunction("summary 4-2", "<cn>3</cn>")))
        ))
      val insertedQuestionId = TestData.await(questionDAO.insert(questionFrame)).question.id
      val insertedQuestionFrame = TestData.await(questionDAO.frameById(insertedQuestionId)).get

      // To compare we need to make sure the ids match
      val idsUpdated = TestData.copyIds(questionFrame, insertedQuestionFrame)
      idsUpdated mustBe(insertedQuestionFrame)
    }

  }


}
