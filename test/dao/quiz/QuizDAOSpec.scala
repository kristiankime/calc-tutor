package dao.quiz

import dao.TestData
import dao.organization.{CourseDAO, OrganizationDAO}
import dao.user.UserDAO
import models.{Non, Own, View, Edit}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import support.EnhancedInjector

class QuizDAOSpec extends PlaySpec with GuiceOneAppPerTest {

  "byIds (course, quiz)" should {

    "return quiz if it's assocated with the course" in {
      val (userDAO, quizDAO, organizationDAO, courseDAO) = app.injector.instanceOf4[UserDAO, QuizDAO, OrganizationDAO, CourseDAO]

      // Create a quiz attached to a course
      val owner = TestData.await(userDAO.insert(TestData.user(0)))
      val org = TestData.await(organizationDAO.insert(TestData.organization(0)))
      val course = TestData.await(courseDAO.insert(TestData.course(0, org, owner) ))
      val quiz = TestData.await(quizDAO.insert(TestData.quiz(0, owner)))
      TestData.await(quizDAO.attach(course, quiz))

      TestData.await(quizDAO.byIds(course.id, quiz.id)) mustBe(Some(quiz))
    }

    "return None if quiz is not assocated with the course" in {
      val (userDAO, quizDAO, organizationDAO, courseDAO) = app.injector.instanceOf4[UserDAO, QuizDAO, OrganizationDAO, CourseDAO]

      // Create a quiz attached to a course
      val owner = TestData.await(userDAO.insert(TestData.user(0)))
      val org = TestData.await(organizationDAO.insert(TestData.organization(0)))
      val course = TestData.await(courseDAO.insert(TestData.course(0, org, owner) ))
      val quiz = TestData.await(quizDAO.insert(TestData.quiz(0, owner)))

      TestData.await(quizDAO.byIds(course.id, quiz.id)) mustBe(None)
    }
  }

  "access" should {

    "return Non if no access has been granted" in {
      val (userDAO, quizDAO) = app.injector.instanceOf2[UserDAO, QuizDAO]

      val owner = TestData.await(userDAO.insert(TestData.user(0)))
      val quiz = TestData.await(quizDAO.insert(TestData.quiz(0, owner)))

      val user = TestData.await(userDAO.insert(TestData.user(1)))

      TestData.await(quizDAO.access(user.id, quiz.id)) mustBe(Non)
    }

    "return Own if user is the course owner" in {
      val (userDAO, quizDAO) = app.injector.instanceOf2[UserDAO, QuizDAO]

      val owner = TestData.await(userDAO.insert(TestData.user(0)))
      val quiz = TestData.await(quizDAO.insert(TestData.quiz(0, owner)))

      TestData.await(quizDAO.access(owner.id, quiz.id)) mustBe(Own)
    }

    "return level that access was granted at if it was granted" in {
      val (userDAO, quizDAO) = app.injector.instanceOf2[UserDAO, QuizDAO]

      val owner = TestData.await(userDAO.insert(TestData.user(0)))
      val quiz = TestData.await(quizDAO.insert(TestData.quiz(0, owner)))

      val user = TestData.await(userDAO.insert(TestData.user(1)))
      TestData.await(quizDAO.grantAccess(user, quiz, View))

      TestData.await(quizDAO.access(user.id, quiz.id)) mustBe(View)
    }

    "return Own if user is owner even if access was granted at a different level" in {
      val (userDAO, quizDAO) = app.injector.instanceOf2[UserDAO, QuizDAO]

      val owner = TestData.await(userDAO.insert(TestData.user(0)))
      val quiz = TestData.await(quizDAO.insert(TestData.quiz(0, owner)))

      TestData.await(quizDAO.grantAccess(owner, quiz, View))

      TestData.await(quizDAO.access(owner.id, quiz.id)) mustBe(Own)
    }

    "return Edit if user has Edit access to a course that the question has been added to" in {
      val (userDAO, quizDAO, organizationDAO, courseDAO) = app.injector.instanceOf4[UserDAO, QuizDAO, OrganizationDAO, CourseDAO]

      // Create a quiz attached to a course
      val owner = TestData.await(userDAO.insert(TestData.user(0)))
      val org = TestData.await(organizationDAO.insert(TestData.organization(0)))
      val course = TestData.await(courseDAO.insert(TestData.course(0, org, owner) ))
      val quiz = TestData.await(quizDAO.insert(TestData.quiz(0, owner)))
      TestData.await(quizDAO.attach(course, quiz))

      // Grant a user Edit access to the course
      val user = TestData.await(userDAO.insert(TestData.user(1)))
      TestData.await(courseDAO.grantAccess(user, course, Edit))

      TestData.await(quizDAO.access(user.id, quiz.id)) mustBe(Edit)
    }

    "return Non if user has access to a course (that had the quiz in it) revoked" in {
      val (userDAO, quizDAO, organizationDAO, courseDAO) = app.injector.instanceOf4[UserDAO, QuizDAO, OrganizationDAO, CourseDAO]

      // Create a quiz attached to a course
      val owner = TestData.await(userDAO.insert(TestData.user(0)))
      val org = TestData.await(organizationDAO.insert(TestData.organization(0)))
      val course = TestData.await(courseDAO.insert(TestData.course(0, org, owner) ))
      val quiz = TestData.await(quizDAO.insert(TestData.quiz(0, owner)))
      TestData.await(quizDAO.attach(course, quiz))

      // Grant a user Edit access to the course
      val user = TestData.await(userDAO.insert(TestData.user(1)))
      TestData.await(courseDAO.grantAccess(user, course, Edit))

      // Revoke access to the course
      TestData.await(courseDAO.revokeAccess(user, course))

      TestData.await(quizDAO.access(user.id, quiz.id)) mustBe(Non)
    }

  }


}
