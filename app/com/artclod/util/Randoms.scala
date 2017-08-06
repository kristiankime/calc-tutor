package com.artclod.util

import scala.util.Random

/**
  * Created by kristiankime on 1/6/16.
  */
object Randoms {

  def randomFrom[A](values : IndexedSeq[A]) = {
    val rand = new Random(System.currentTimeMillis())
    val random_index = rand.nextInt(values.length)
    values(random_index)
  }

}
