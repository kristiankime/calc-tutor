package dao.quiz

import com.artclod.slick.{JodaUTC, NumericBoolean}
import dao.TestData
import dao.TestData.{questionPartChoice, questionPartFunction, questionSectionFrameFn, questionSectionFrameCh, questionSectionFrameSe}
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
          questionSectionFrameCh("explanation 1")(questionPartChoice("summary 1-1", NumericBoolean.T)),
          questionSectionFrameFn("explanation 2")(questionPartFunction("summary 2-1", "<cn>1</cn>")),
          questionSectionFrameCh("explanation 3")(questionPartChoice("summary 3-1", NumericBoolean.F), questionPartChoice("summary 3-2", NumericBoolean.T)),
          questionSectionFrameFn("explanation 4")(questionPartFunction("summary 4-1", "<cn>2</cn>"), (questionPartFunction("summary 4-2", "<cn>3</cn>")))
        ))
      val insertedQuestionId = TestData.await(questionDAO.insert(questionFrame)).question.id
      val insertedQuestionFrame = TestData.await(questionDAO.frameById(insertedQuestionId)).get

      // To compare we need to make sure the ids match
      val idsUpdated = TestData.copyIds(questionFrame, insertedQuestionFrame)
      idsUpdated mustBe(insertedQuestionFrame)
    }

  }

  "questionSearchSet" should {

    "return the question if wild card match" in {
      val (userDAO, questionDAO, skillDAO) = app.injector.instanceOf3[UserDAO, QuestionDAO, SkillDAO]
      val user = TestData.await(userDAO.insert(TestData.user(0)))
      val Seq(sA, sB) = TestData.await( skillDAO.insertAll(TestData.skill("a"), TestData.skill("b")) )
      val questionFrame = TestData.await(questionDAO.insert(TestData.questionFrameSimple(info = "a", userId = user.id, skills = Seq(sA, sB))))

      val questions = TestData.await(questionDAO.questionSearchSet("%", Seq(), Seq()))

      questions mustBe(  Seq((questionFrame.question, Set(sA, sB)))  )
    }

    "not return the question if title doesn't match" in {
      val (userDAO, questionDAO, skillDAO) = app.injector.instanceOf3[UserDAO, QuestionDAO, SkillDAO]
      val user = TestData.await(userDAO.insert(TestData.user(0)))
      val Seq(sA, sB) = TestData.await( skillDAO.insertAll(TestData.skill("a"), TestData.skill("b")) )
      val questionFrame = TestData.await(questionDAO.insert(TestData.questionFrameSimple(info = "a", userId = user.id, skills = Seq(sA, sB))))

      val questions = TestData.await(questionDAO.questionSearchSet("this should not match", Seq(), Seq()))

      questions mustBe(  Seq()  )
    }

    "return only questions that match the title search pattern" in {
      val (userDAO, questionDAO, skillDAO) = app.injector.instanceOf3[UserDAO, QuestionDAO, SkillDAO]
      val user = TestData.await(userDAO.insert(TestData.user(0)))
      val Seq(sA, sB) = TestData.await( skillDAO.insertAll(TestData.skill("a"), TestData.skill("b")) )
      val questionFrame1 = TestData.await(questionDAO.insert(TestData.questionFrameSimple(info = "1", userId = user.id, skills = Seq(sA, sB))))
      val questionFrame2 = TestData.await(questionDAO.insert(TestData.questionFrameSimple(info = "2", userId = user.id, skills = Seq(sA, sB))))

      val questions = TestData.await(questionDAO.questionSearchSet("1%", Seq(), Seq()))

      questions mustBe(  Seq((questionFrame1.question, Set(sA, sB)))  )
    }

    "return only questions that match the required skills" in {
      val (userDAO, questionDAO, skillDAO) = app.injector.instanceOf3[UserDAO, QuestionDAO, SkillDAO]
      val user = TestData.await(userDAO.insert(TestData.user(0)))
      val Seq(sA, sB) = TestData.await( skillDAO.insertAll(TestData.skill("a"), TestData.skill("b")) )
      val questionFrame1 = TestData.await(questionDAO.insert(TestData.questionFrameSimple(info = "1", userId = user.id, skills = Seq(sA))))
      val questionFrame2 = TestData.await(questionDAO.insert(TestData.questionFrameSimple(info = "2", userId = user.id, skills = Seq(sB))))

      val questions = TestData.await(questionDAO.questionSearchSet("%", Seq(sA.name), Seq()))

      questions mustBe(  Seq((questionFrame1.question, Set(sA)))  )
    }


    "excluded questions that have the banned skills" in {
      val (userDAO, questionDAO, skillDAO) = app.injector.instanceOf3[UserDAO, QuestionDAO, SkillDAO]
      val user = TestData.await(userDAO.insert(TestData.user(0)))
      val Seq(sA, sB) = TestData.await( skillDAO.insertAll(TestData.skill("a"), TestData.skill("b")) )
      val questionFrame1 = TestData.await(questionDAO.insert(TestData.questionFrameSimple(info = "1", userId = user.id, skills = Seq(sA))))
      val questionFrame2 = TestData.await(questionDAO.insert(TestData.questionFrameSimple(info = "2", userId = user.id, skills = Seq(sB))))

      val questions = TestData.await(questionDAO.questionSearchSet("%", Seq(), Seq(sA.name)))

      questions mustBe(  Seq((questionFrame2.question, Set(sB)))  )
    }

  }

}
