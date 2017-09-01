package com.artclod

import scala.collection.SeqLike
import scala.concurrent.{ExecutionContext, Future}

package object concurrent {


  // TODO why doesn't this work?
//  def raiseFuture[A, REPR <: SeqLike[A, REPR]](sequenceOfFutures: Seq[Future[A]], emptyFutureSequence: REPR)(implicit executionContext: ExecutionContext) : Future[REPR] = {
//    sequenceOfFutures.foldLeft(  Future.successful[REPR](emptyFutureSequence) )  ((futureCur, futureAdd) => futureCur.flatMap(cur => futureAdd.map(add => cur :+ add)) )
//    null
//  }

  def raiseFuture[A](sequenceOfFutures: Seq[Future[A]])(implicit executionContext: ExecutionContext) : Future[Vector[A]] = {
    sequenceOfFutures.foldLeft(  Future.successful(Vector[A]())               )  ((futureCur, futureAdd) => futureCur.flatMap(cur => futureAdd.map(add => cur :+ add)) )
  }

}

