package com.artclod

import scala.collection.GenSeqLike

package object collection {

  def rescaleZero2One[T](s : Seq[T])(implicit num: Fractional[T]) = {
    if(s.isEmpty) {
      s
    } else if(s.size == 1) {
      s.map(_ => num.div(num.one, num.plus(num.one, num.one)) )
    } else {
      val max = s.max
      val min = s.min
      //         x - min
      // f(x) = ---------
      //        max - min
      s.map(x =>
        num.div(
          num.minus(x, min),
          num.minus(max, min)
        ))
    }
  }

//  def rescaleZero2One[T, S <: Seq[T]](s : S)(implicit num: Fractional[T]): S = {
//    if(s.isEmpty) {
//      s
//    } else if(s.size == 1) {
//      s.map(_ => num.div(num.one, num.plus(num.one, num.one)) ).asInstanceOf[S]
//    } else {
//      val max = s.max
//      val min = s.min
////         x - min
//// f(x) = ---------
////        max - min
//      s.map(v =>
//        num.div(
//          num.minus(v, min),
//          num.minus(max, min)
//        )).asInstanceOf[S]
//    }
//  }

  implicit class PimpedGenSeqLike[+A, +Repr](val seq: GenSeqLike[A, Repr]) {
    def indexOfOp[B >: A](elem: B): Option[Int] ={
      val indexOf = seq.indexOf(elem)
      if(indexOf == -1) { None }
      else { Some(indexOf) }
    }
  }

  implicit class PimpedOptionList[T](val op: Option[List[T]]) {
    def dropOption: List[T] = op match {
      case Some(list) => list
      case None => List()
    }
  }

	implicit class PimpedSeq[E](seq: Seq[E]) {
		def elementAfter(e: E) = {
			val index = seq.indexOf(e)
			if (index == -1) { None }
			else if (index == (seq.size - 1)) { None }
			else { Some(seq(index + 1)) }
		}

    def elementBefore(e: E) = {
      val index = seq.indexOf(e)
      if (index == -1) { None }
      else if (index == 0) { None }
      else { Some(seq(index - 1)) }
    }
	}

  def takeTuple2[E](s : TraversableOnce[E]) : (E, E) = s match {
    case Seq(e1, e2, xs@_* ) => (e1, e2)
    case _ => throw new IllegalArgumentException("There were not enough elements in " + s)
  }


}