package dao.quiz

import com.artclod.slick.{JodaUTC, NumericBoolean}
import dao.TestData
import dao.TestData.{questionPartChoice, questionPartFunction, questionSectionFrame}
import dao.organization.{CourseDAO, OrganizationDAO}
import dao.user.UserDAO
import models.quiz.{QuestionFrame, QuizFrame}
import models.{Edit, Non, Own, View}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import support.{CleanDatabaseAfterEach, EnhancedInjector}

class QuizDAOSpec extends PlaySpec with CleanDatabaseAfterEach {

  "byIds (course, quiz)" should {

    "return quiz if it's assocated with the course" in {
      val (userDAO, quizDAO, organizationDAO, courseDAO) = app.injector.instanceOf4[UserDAO, QuizDAO, OrganizationDAO, CourseDAO]

      // Create a quiz attached to a course
      val owner = TestData.await(userDAO.insert(TestData.user(0)))
      val org = TestData.await(organizationDAO.insert(TestData.organization(0)))
      val course = TestData.await(courseDAO.insert(TestData.course(0, org, owner) ))
      val quiz = TestData.await(quizDAO.insert(TestData.quiz(0, owner)))
      TestData.await(quizDAO.attach(course, quiz, false, None, None))

      TestData.await(quizDAO.byIds(course.id, quiz.id)).map(_._2)  mustBe(Some(quiz))
    }

    "return None if quiz is not assocated with the course" in {
      val (userDAO, quizDAO, organizationDAO, courseDAO) = app.injector.instanceOf4[UserDAO, QuizDAO, OrganizationDAO, CourseDAO]

      // Create a quiz attached to a course
      val owner = TestData.await(userDAO.insert(TestData.user(0)))
      val org = TestData.await(organizationDAO.insert(TestData.organization(0)))
      val course = TestData.await(courseDAO.insert(TestData.course(0, org, owner) ))
      val quiz = TestData.await(quizDAO.insert(TestData.quiz(0, owner)))

      TestData.await(quizDAO.byIds(course.id, quiz.id)).map(_._2)  mustBe(None)
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
      TestData.await(quizDAO.attach(course, quiz, false, None, None))

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
      TestData.await(quizDAO.attach(course, quiz, false, None, None))

      // Grant a user Edit access to the course
      val user = TestData.await(userDAO.insert(TestData.user(1)))
      TestData.await(courseDAO.grantAccess(user, course, Edit))

      // Revoke access to the course
      TestData.await(courseDAO.revokeAccess(user, course))

      TestData.await(quizDAO.access(user.id, quiz.id)) mustBe(Non)
    }

  }


  "frameById" should {

    "return quizFrame" in {
      val (userDAO, quizDAO, organizationDAO, courseDAO, questionDAO, skillDAO) = app.injector.instanceOf6[UserDAO, QuizDAO, OrganizationDAO, CourseDAO, QuestionDAO, SkillDAO]

      // Create a quiz
      val owner = TestData.await(userDAO.insert(TestData.user(0)))
      val quiz = TestData.await(quizDAO.insert(TestData.quiz(0, owner)))

      // Setup the skills for use in the questions
      val skillsNoId = Vector(TestData.skill("a"), TestData.skill("b"))
      skillDAO.insertAll(skillsNoId:_*)
      val skills = TestData.await(skillDAO.allSkills)

      // Create a Question for the quiz
      val questionFrame = TestData.questionFrame("title", "description", owner.id, JodaUTC.zero,
        skills,
        Seq(
          questionSectionFrame("explanation 1")(questionPartChoice("summary 1-1", NumericBoolean.T))(),
          questionSectionFrame("explanation 2")()(questionPartFunction("summary 2-1", "<cn>1</cn>")),
          questionSectionFrame("explanation 3")(questionPartChoice("summary 3-1", NumericBoolean.F), questionPartChoice("summary 3-2", NumericBoolean.T))(),
          questionSectionFrame("explanation 4")()(questionPartFunction("summary 4-1", "<cn>2</cn>"), (questionPartFunction("summary 4-2", "<cn>3</cn>")))
        ))
      val insertedQuestionFrame = TestData.await(questionDAO.insert(questionFrame))

      // Add Questions to Quiz
      TestData.await(quizDAO.attach(insertedQuestionFrame.question, quiz, owner.id))

      // compare (make sure to update id)
      TestData.await(quizDAO.frameById(quiz.id)) mustBe (Some(QuizFrame(quiz, Vector(insertedQuestionFrame))))
    }

  }


  "attach" should {

    "attach questions in order" in {
      val (userDAO, quizDAO, organizationDAO, courseDAO, questionDAO, skillDAO) = app.injector.instanceOf6[UserDAO, QuizDAO, OrganizationDAO, CourseDAO, QuestionDAO, SkillDAO]

      // Create a quiz
      val owner = TestData.await(userDAO.insert(TestData.user(0)))
      val quiz = TestData.await(quizDAO.insert(TestData.quiz(0, owner)))

      // Setup the skills for use in the questions
      val skillsNoId = Vector(TestData.skill("a"), TestData.skill("b"))
      skillDAO.insertAll(skillsNoId:_*)
      val skills = TestData.await(skillDAO.allSkills)

      // Create Questions for the quiz
      val question1Frame = TestData.await(questionDAO.insert(
        TestData.questionFrame("question 1", "description", owner.id, JodaUTC.zero, skills,  Seq(questionSectionFrame("explanation 1")()(questionPartFunction("summary 2-1", "<cn>1</cn>"))) )
      ))
      val question2Frame = TestData.await(questionDAO.insert(
        TestData.questionFrame("question 1", "description", owner.id, JodaUTC.zero, skills,  Seq(questionSectionFrame("explanation 1")()(questionPartFunction("summary 2-1", "<cn>1</cn>"))) )
      ))
      val question3Frame = TestData.await(questionDAO.insert(
        TestData.questionFrame("question 1", "description", owner.id, JodaUTC.zero, skills,  Seq(questionSectionFrame("explanation 1")()(questionPartFunction("summary 2-1", "<cn>1</cn>"))) )
      ))

      // Add Questions to Quiz
      TestData.await(quizDAO.attach(question1Frame.question, quiz, owner.id))
      TestData.await(quizDAO.attach(question2Frame.question, quiz, owner.id))
      TestData.await(quizDAO.attach(question3Frame.question, quiz, owner.id))

      // compare
      TestData.await(quizDAO.questionSummariesFor(quiz)) mustBe (Seq(question1Frame.question, question2Frame.question, question3Frame.question))
    }

    "attaching a question multiple time has no effect" in {
      val (userDAO, quizDAO, organizationDAO, courseDAO, questionDAO, skillDAO) = app.injector.instanceOf6[UserDAO, QuizDAO, OrganizationDAO, CourseDAO, QuestionDAO, SkillDAO]

      // Create a quiz
      val owner = TestData.await(userDAO.insert(TestData.user(0)))
      val quiz = TestData.await(quizDAO.insert(TestData.quiz(0, owner)))

      // Setup the skills for use in the questions
      val skillsNoId = Vector(TestData.skill("a"), TestData.skill("b"))
      skillDAO.insertAll(skillsNoId:_*)
      val skills = TestData.await(skillDAO.allSkills)

      // Create Questions for the quiz
      val question1Frame = TestData.await(questionDAO.insert(
        TestData.questionFrame("question 1", "description", owner.id, JodaUTC.zero, skills,  Seq(questionSectionFrame("explanation 1")()(questionPartFunction("summary 2-1", "<cn>1</cn>"))) )
      ))
      val question2Frame = TestData.await(questionDAO.insert(
        TestData.questionFrame("question 1", "description", owner.id, JodaUTC.zero, skills,  Seq(questionSectionFrame("explanation 1")()(questionPartFunction("summary 2-1", "<cn>1</cn>"))) )
      ))
      val question3Frame = TestData.await(questionDAO.insert(
        TestData.questionFrame("question 1", "description", owner.id, JodaUTC.zero, skills,  Seq(questionSectionFrame("explanation 1")()(questionPartFunction("summary 2-1", "<cn>1</cn>"))) )
      ))

      // Add Questions to Quiz
      TestData.await(quizDAO.attach(question1Frame.question, quiz, owner.id))
      TestData.await(quizDAO.attach(question2Frame.question, quiz, owner.id))
      TestData.await(quizDAO.attach(question3Frame.question, quiz, owner.id))

      // Here we attach some questions again
      TestData.await(quizDAO.attach(question3Frame.question, quiz, owner.id))
      TestData.await(quizDAO.attach(question2Frame.question, quiz, owner.id))

      // compare
      TestData.await(quizDAO.questionSummariesFor(quiz)) mustBe (Seq(question1Frame.question, question2Frame.question, question3Frame.question))
    }

  }


  "detach" should {

    "leave question order" in {
      val (userDAO, quizDAO, organizationDAO, courseDAO, questionDAO, skillDAO) = app.injector.instanceOf6[UserDAO, QuizDAO, OrganizationDAO, CourseDAO, QuestionDAO, SkillDAO]

      // Create a quiz
      val owner = TestData.await(userDAO.insert(TestData.user(0)))
      val quiz = TestData.await(quizDAO.insert(TestData.quiz(0, owner)))

      // Setup the skills for use in the questions
      val skillsNoId = Vector(TestData.skill("a"), TestData.skill("b"))
      skillDAO.insertAll(skillsNoId:_*)
      val skills = TestData.await(skillDAO.allSkills)

      // Create Questions for the quiz
      val question1Frame = TestData.await(questionDAO.insert(
        TestData.questionFrame("question 1", "description", owner.id, JodaUTC.zero, skills,  Seq(questionSectionFrame("explanation 1")()(questionPartFunction("summary 2-1", "<cn>1</cn>"))) )
      ))
      val question2Frame = TestData.await(questionDAO.insert(
        TestData.questionFrame("question 1", "description", owner.id, JodaUTC.zero, skills,  Seq(questionSectionFrame("explanation 1")()(questionPartFunction("summary 2-1", "<cn>1</cn>"))) )
      ))
      val question3Frame = TestData.await(questionDAO.insert(
        TestData.questionFrame("question 1", "description", owner.id, JodaUTC.zero, skills,  Seq(questionSectionFrame("explanation 1")()(questionPartFunction("summary 2-1", "<cn>1</cn>"))) )
      ))

      // Add Questions to Quiz
      TestData.await(quizDAO.attach(question1Frame.question, quiz, owner.id))
      TestData.await(quizDAO.attach(question2Frame.question, quiz, owner.id))
      TestData.await(quizDAO.attach(question3Frame.question, quiz, owner.id))

      // detach middle question
      TestData.await(quizDAO.detach(question2Frame.question, quiz))

      // compare
      TestData.await(quizDAO.questionSummariesFor(quiz)) mustBe (Seq(question1Frame.question, question3Frame.question))
    }

  }

  "access via course" should {

    "return Non if user has view access to a course but quiz is not in course" in {
      val (userDAO, quizDAO, courseDAO, organizationDAO) = app.injector.instanceOf4[UserDAO, QuizDAO, CourseDAO, OrganizationDAO]

      val owner = TestData.await(userDAO.insert(TestData.user(0)))
      val organization = TestData.await(organizationDAO.insert(TestData.organization(0)))
      val course = TestData.await(courseDAO.insert(TestData.course(0, organization, owner)))
      val quiz = TestData.await(quizDAO.insert(TestData.quiz(0, owner))) // Not attached to course
      val user = TestData.await(userDAO.insert(TestData.user(1)))

      courseDAO.grantAccess(user, course, View)

      TestData.await(quizDAO.access(user.id, quiz.id)) mustBe (Non)
    }

    "return View if user has view access to a course and quiz is in course" in {
      val (userDAO, quizDAO, courseDAO, organizationDAO) = app.injector.instanceOf4[UserDAO, QuizDAO, CourseDAO, OrganizationDAO]

      val owner = TestData.await(userDAO.insert(TestData.user(0)))
      val organization = TestData.await(organizationDAO.insert(TestData.organization(0)))
      val course = TestData.await(courseDAO.insert(TestData.course(0, organization, owner)))
      val quiz = TestData.await(quizDAO.insert(TestData.quiz(0, owner)))
      TestData.await(quizDAO.attach(course, quiz, false, None, None)) // Quiz attached without any Availability restrictions
      val user = TestData.await(userDAO.insert(TestData.user(1)))

      courseDAO.grantAccess(user, course, View)

      TestData.await(quizDAO.access(user.id, quiz.id)) mustBe (View)
    }

  }
}
