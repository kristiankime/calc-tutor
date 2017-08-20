package com.artclod

import scala.concurrent.{ExecutionContext, Future}

package object util {

  def optionElse[V, R](option : Option[V])(f : V => R)(or : R) =
    if(option.nonEmpty){
      f(option.get)
    } else {
      or
    }

  case class BooleanOption(opt: Option[Boolean]){
    def noneFalse = opt match {
      case None => false
      case Some(bool) => bool
    }

    def noneTrue = opt match {
      case None => true
      case Some(bool) => bool
    }
  }

  implicit class TypeSafeEquals[T](a:T) {
    def ^==(b: T) = { a == b }

    def ^!=(b: T) = { a != b }
  }

  def eitherOp[A, B](opA: Option[A], opB: Option[B]) = (opA, opB) match {
    case (_, Some(b)) => Right(b)
    case (Some(a), _) => Left(a)
    case (None, None) => throw new IllegalArgumentException("neither options had values")
  }

  implicit class EitherEnhanced[A, B](e: Either[A, B]) {
    def leftOp = e.left.toOption

    def leftOp[X](f: A => X) = e.left.toOption.map(f(_))

    def rightOp = e.right.toOption

    def rightOp[X](f: B => X) = e.right.toOption.map(f(_))
  }

  // =========== Either Combine
  implicit class EitherCombine1[L, R1](e: Either[L, R1]) {
    def +[R](o: Either[L, R]) = (e, o) match {
        case (Left(l), _) => Left(l)
        case (_, Left(l)) => Left(l)
        case (Right(a), Right(b)) => Right((a, b))
    }
  }

  implicit class EitherCombine2[L, R1, R2](e: Either[L, (R1, R2)]) {
    def +[R](o: Either[L, R]) = (e, o) match {
        case (Left(l), _) => Left(l)
        case (_, Left(l)) => Left(l)
        case (Right(a), Right(b)) => Right((a._1, a._2, b))
      }
  }

  implicit class EitherCombine3[L, R1, R2, R3](e: Either[L, (R1, R2, R3)]) {
    def +[R](o: Either[L, R]) = (e, o) match {
        case (Left(l), _) => Left(l)
        case (_, Left(l)) => Left(l)
        case (Right(a), Right(b)) => Right((a._1, a._2, a._3, b))
      }
  }

  implicit class EitherCombine4[L, R1, R2, R3, R4](e: Either[L, (R1, R2, R3, R4)]) {
    def +[R](o: Either[L, R]) = (e, o) match {
      case (Left(l), _) => Left(l)
      case (_, Left(l)) => Left(l)
      case (Right(a), Right(b)) => Right((a._1, a._2, a._3, a._4, b))
    }
  }

  // =========== Future Either Combine
  implicit class FutureEitherCombine1[L, R1](future: Future[Either[L, R1]]) {
    def +&[R](other: Future[Either[L, R]])(implicit executionContext: ExecutionContext) : Future[Either[L, (R1, R)]] = future.flatMap(e => other.map(o => (e, o) match {
      case (Left(l), _) => Left(l)
      case (_, Left(l)) => Left(l)
      case (Right(a), Right(b)) => Right((a, b))
    }))

    def +^[O](other: Future[O])(implicit executionContext: ExecutionContext) : Future[Either[L, (R1, O)]] = future.flatMap(e => other.map(o => (e, o) match {
      case (Left(l), _) => Left(l)
      case (Right(a), _) => Right((a, o))
    }))
  }

  implicit class FutureEitherCombine2[L, R1, R2](future: Future[Either[L, (R1, R2)]]) {
    def +&[R](other: Future[Either[L, R]])(implicit executionContext: ExecutionContext) : Future[Either[L, (R1, R2, R)]] = future.flatMap(e => other.map(o => (e, o) match {
      case (Left(l), _) => Left(l)
      case (_, Left(l)) => Left(l)
      case (Right(a), Right(b)) => Right((a._1, a._2, b))
    }))

    def +^[O](other: Future[O])(implicit executionContext: ExecutionContext) : Future[Either[L, (R1, R2, O)]] = future.flatMap(e => other.map(o => (e, o) match {
      case (Left(l), _) => Left(l)
      case (Right(a), _) => Right((a._1, a._2, o))
    }))
  }

  implicit class FutureEitherCombine3[L, R1, R2, R3](future: Future[Either[L, (R1, R2, R3)]]) {
    def +&[R](other: Future[Either[L, R]])(implicit executionContext: ExecutionContext) : Future[Either[L, (R1, R2, R3, R)]] = future.flatMap(e => other.map(o => (e, o) match {
      case (Left(l), _) => Left(l)
      case (_, Left(l)) => Left(l)
      case (Right(a), Right(b)) => Right((a._1, a._2, a._3, b))
    }))

    def +^[O](other: Future[O])(implicit executionContext: ExecutionContext) : Future[Either[L, (R1, R2, R3, O)]] = future.flatMap(e => other.map(o => (e, o) match {
      case (Left(l), _) => Left(l)
      case (Right(a), _) => Right((a._1, a._2, a._3, o))
    }))
  }

  implicit class FutureEitherCombine4[L, R1, R2, R3, R4](future: Future[Either[L, (R1, R2, R3, R4)]]) {
    def +&[R](other: Future[Either[L, R]])(implicit executionContext: ExecutionContext) : Future[Either[L, (R1, R2, R3, R4, R)]] = future.flatMap(e => other.map(o => (e, o) match {
      case (Left(l), _) => Left(l)
      case (_, Left(l)) => Left(l)
      case (Right(a), Right(b)) => Right((a._1, a._2, a._3, a._4, b))
    }))

    def +^[O](other: Future[O])(implicit executionContext: ExecutionContext) : Future[Either[L, (R1, R2, R3, R4, O)]] = future.flatMap(e => other.map(o => (e, o) match {
      case (Left(l), _) => Left(l)
      case (Right(a), _) => Right((a._1, a._2, a._3, a._4, o))
    }))
  }

  implicit class FutureEitherCombine5[L, R1, R2, R3, R4, R5](future: Future[Either[L, (R1, R2, R3, R4, R5)]]) {
    def +&[R](other: Future[Either[L, R]])(implicit executionContext: ExecutionContext) : Future[Either[L, (R1, R2, R3, R4, R5, R)]] = future.flatMap(e => other.map(o => (e, o) match {
      case (Left(l), _) => Left(l)
      case (_, Left(l)) => Left(l)
      case (Right(a), Right(b)) => Right((a._1, a._2, a._3, a._4, a._5, b))
    }))

    def +^[O](other: Future[O])(implicit executionContext: ExecutionContext) : Future[Either[L, (R1, R2, R3, R4, R5, O)]] = future.flatMap(e => other.map(o => (e, o) match {
      case (Left(l), _) => Left(l)
      case (Right(a), _) => Right((a._1, a._2, a._3, a._4, a._5, o))
    }))
  }

}
