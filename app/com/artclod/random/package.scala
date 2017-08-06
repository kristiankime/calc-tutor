package com.artclod

import com.artclod.collection._

import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds
import scala.util.Random

package object random {

  def pickNFrom[T, CC[X] <: TraversableOnce[X]](n: Int, r: Random)(xs: CC[T])(implicit bf: CanBuildFrom[CC[T], T, CC[T]]): CC[T] = {
    val shuffled = r.shuffle(xs)

    val it = shuffled.toIterator
    val ret = bf()
    var i = 0

    while(it.hasNext && i < n){
      ret += it.next
      i += 1
    }
    ret.result
  }

  implicit class NPicker[T, CC[X] <: TraversableOnce[X]](val t : CC[T]) {
    def pickNFrom(n: Int, r: Random)(implicit bf: CanBuildFrom[CC[T], T, CC[T]]): CC[T] = random.pickNFrom(n, r)(t)(bf)
    def pickNFrom(n: Int)(implicit bf: CanBuildFrom[CC[T], T, CC[T]], r: Random): CC[T] = random.pickNFrom(n, r)(t)(bf)

    def pick2From(r: Random)(implicit bf: CanBuildFrom[CC[T], T, CC[T]]): (T, T) = takeTuple2(random.pickNFrom(2, r)(t)(bf))
    def pick2From(implicit bf: CanBuildFrom[CC[T], T, CC[T]], r: Random): (T, T) = takeTuple2(random.pickNFrom(2, r)(t)(bf))
  }
}
