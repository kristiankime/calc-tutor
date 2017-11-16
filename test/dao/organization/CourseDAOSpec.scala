package dao.organization

import com.artclod.slick.JodaUTC
import dao.TestData
import dao.user.UserDAO
import models.{Edit, Non, Own, View}
import models.organization.{Course, Organization}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import support.{CleanDatabaseAfterEach, EnhancedInjector}

class CourseDAOSpec extends PlaySpec with CleanDatabaseAfterEach {


  "access" should {

    "return Non if no access has been granted" in {
      val (userDAO, organizationDAO, courseDAO) = app.injector.instanceOf3[UserDAO, OrganizationDAO, CourseDAO]

      val owner = TestData.await(userDAO.insert(TestData.user(0)))
      val organization = TestData.await(organizationDAO.insert(TestData.organization(0)))
      val course = TestData.await(courseDAO.insert(TestData.course(0, organization, owner)))

      val user = TestData.await(userDAO.insert(TestData.user(1)))

      TestData.await(courseDAO.access(user.id, course.id)) mustBe(Non)
    }

    "return Own if user is the course owner" in {
      val (userDAO, organizationDAO, courseDAO) = app.injector.instanceOf3[UserDAO, OrganizationDAO, CourseDAO]

      val owner = TestData.await(userDAO.insert(TestData.user(0)))
      val organization = TestData.await(organizationDAO.insert(TestData.organization(0)))
      val course = TestData.await(courseDAO.insert(TestData.course(0, organization, owner)))

      TestData.await(courseDAO.access(owner.id, course.id)) mustBe(Own)
    }

    "return level that access was granted at if it was granted (View)" in {
      val (userDAO, organizationDAO, courseDAO) = app.injector.instanceOf3[UserDAO, OrganizationDAO, CourseDAO]

      val owner = TestData.await(userDAO.insert(TestData.user(0)))
      val organization = TestData.await(organizationDAO.insert(TestData.organization(0)))
      val course = TestData.await(courseDAO.insert(TestData.course(0, organization, owner)))

      val user = TestData.await(userDAO.insert(TestData.user(1)))
      TestData.await(courseDAO.grantAccess(user, course, View))

      TestData.await(courseDAO.access(user.id, course.id)) mustBe(View)
    }

    "return level that access was granted at if it was granted (Edit)" in {
      val (userDAO, organizationDAO, courseDAO) = app.injector.instanceOf3[UserDAO, OrganizationDAO, CourseDAO]

      val owner = TestData.await(userDAO.insert(TestData.user(0)))
      val organization = TestData.await(organizationDAO.insert(TestData.organization(0)))
      val course = TestData.await(courseDAO.insert(TestData.course(0, organization, owner)))

      val user = TestData.await(userDAO.insert(TestData.user(1)))
      TestData.await(courseDAO.grantAccess(user, course, Edit))

      TestData.await(courseDAO.access(user.id, course.id)) mustBe(Edit)
    }

    "return Own if user is owner even if access was granted at a different level" in {
      val (userDAO, organizationDAO, courseDAO) = app.injector.instanceOf3[UserDAO, OrganizationDAO, CourseDAO]

      val owner = TestData.await(userDAO.insert(TestData.user(0)))
      val organization = TestData.await(organizationDAO.insert(TestData.organization(0)))
      val course = TestData.await(courseDAO.insert(TestData.course(0, organization, owner)))

      TestData.await(courseDAO.grantAccess(owner, course, View))

      TestData.await(courseDAO.access(owner.id, course.id)) mustBe(Own)
    }
  }


}
