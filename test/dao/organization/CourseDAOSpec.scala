package dao.organization

import com.artclod.slick.JodaUTC
import dao.TestData
import dao.user.UserDAO
import models.Non
import models.organization.{Course, Organization}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest

class CourseDAOSpec extends PlaySpec with GuiceOneAppPerTest {

  "access" should {

    "return Non if no access has been granted" in {
      val userDAO = app.injector.instanceOf[UserDAO]
      val organizationDAO = app.injector.instanceOf[OrganizationDAO]
      val courseDAO = app.injector.instanceOf[CourseDAO]

      val owner = TestData.data(userDAO.insert(TestData.user(0)))
      val organization = TestData.data(organizationDAO.insert(Organization(name= "name", creationDate=JodaUTC(0), updateDate=JodaUTC(0))))
      val course = TestData.data(courseDAO.insert(TestData.course(0, organization, owner)))

      val user = TestData.data(userDAO.insert(TestData.user(1)))

      TestData.data(courseDAO.access(user.id, course.id)) mustBe(Non)
    }

  }


}
