package com.artclod.util

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent._
import scala.util.{Success, Failure, Random, Try}

object TryUtil {
  val r = new Random(0)

  def retryOnFail[A](f: () => Try[A], maxRetries: Int = 5, maxWaitBetweenCalls: Int = 100) : Try[A] = {
    var result = f()
    var i = 0
    while(result.isFailure && i < maxRetries){
      // http://stackoverflow.com/questions/10317041/exception-handling-in-case-of-thread-sleep-and-wait-method-in-case-of-threads
      if(maxWaitBetweenCalls > 0) {
        Future { blocking(Thread.sleep(r.nextInt(maxWaitBetweenCalls) + 1)); "done" }
        // Thread.sleep(r.nextInt(maxWaitTime) + 1)
      }
      result = f()
      i = i + 1
    }
    result
  }

  implicit class TryFunctionEnhanced[A](f : () => Try[A]) {
    def retryOnFail(maxRetries: Int = 5, maxWaitBetweenCalls: Int = 100) = TryUtil.retryOnFail(f, maxRetries, maxWaitBetweenCalls)
  }

  implicit class EitherPimp[L <: Throwable,R](e:Either[L,R]){
    def toTry:Try[R] = e.fold(Failure(_), Success(_))
  }

  implicit class TryPimp[T](t:Try[T]){
    def toEither:Either[Throwable,T] = t.transform(s => Success(Right(s)), f => Success(Left(f))).get
  }
}
