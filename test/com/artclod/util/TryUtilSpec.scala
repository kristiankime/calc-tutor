package com.artclod.util

import org.junit.runner.RunWith
import org.scalatestplus.play._
import play.api.test.Helpers._
import org.junit.runner.RunWith
import com.artclod.util.TryUtil._
import org.scalatest.junit.JUnitRunner

import scala.util.{Failure, Success, Try}

class TryUtilSpec extends PlaySpec {

	"retryOnFail" should {

		"succeed if the function always succeeds" in {
      (Succeed() retryOnFail()).isSuccess mustBe(true)

		}

    "fail if the function always fails" in {
      (Fail() retryOnFail()).isFailure mustBe(true)
    }

    "succeed if the function succeeds eventually" in {
      (SucceedAfter(5) retryOnFail(10)).isSuccess mustBe(true)
    }

    "fail if the function would succeed eventually but we don't try enough times" in {
      (SucceedAfter(10) retryOnFail(5)).isFailure mustBe(true)
    }

	}

}

case class SucceedAfter(failNum: Int) extends (() => Try[String]) {
  var count = 0

  def apply : Try[String] = {
    count = count + 1
    if(count > failNum) Success("success")
    else Failure(new IllegalStateException())
  }
}

case class Fail() extends (() => Try[String]) {
  def apply : Try[String] =Failure(new IllegalStateException())
}

case class Succeed() extends (() => Try[String]) {
  def apply : Try[String] = Success("success")
}