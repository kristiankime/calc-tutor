package models.organizations

import com.artclod.slick.JodaUTC
import models.View
import models.organization.Course2Quiz
import org.scalatestplus.play._

class Course2QuizSpec extends PlaySpec {

	"isValidTime with View access" should {

		"return true if there are no availability limitations" in {
      Course2Quiz(null, null, false, None, None).isValidTime(View, JodaUTC.zero) mustEqual(true)
		}

    "ignores hide setting on" in {
      Course2Quiz(null, null, true, None, None).isValidTime(View, JodaUTC.zero) mustEqual(true)
    }

    "return true if start is before current time" in {
      Course2Quiz(null, null, false, Some(JodaUTC(-1)), None).isValidTime(View, JodaUTC.zero) mustEqual(true)
    }

    "return false if start is after current time" in {
      Course2Quiz(null, null, false, Some(JodaUTC(1)), None).isValidTime(View, JodaUTC.zero) mustEqual(false)
    }

    "return true if end is after current time" in {
      Course2Quiz(null, null, false, None, Some(JodaUTC(1))).isValidTime(View, JodaUTC.zero) mustEqual(true)
    }

    "return false if end is before current time" in {
      Course2Quiz(null, null, false, None, Some(JodaUTC(-1))).isValidTime(View, JodaUTC.zero) mustEqual(false)
    }

    "return true if current time is between start and end" in {
      Course2Quiz(null, null, false, Some(JodaUTC(-1)), Some(JodaUTC(1))).isValidTime(View, JodaUTC.zero) mustEqual(true)
    }

    "return false if current time is before start end interval" in {
      Course2Quiz(null, null, false, Some(JodaUTC(1)), Some(JodaUTC(2))).isValidTime(View, JodaUTC.zero) mustEqual(false)
    }

    "return false if current time is after start end interval" in {
      Course2Quiz(null, null, false, Some(JodaUTC(-2)), Some(JodaUTC(-1))).isValidTime(View, JodaUTC.zero) mustEqual(false)
    }

  }


}