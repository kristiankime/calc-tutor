package dao.organization

import dao.TestData
import dao.quiz.QuizDAO
import dao.user.UserDAO
import models.{Non, Own, View}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import support.EnhancedInjector

class QuizDAOSpec extends PlaySpec with GuiceOneAppPerTest {

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
  }


}
