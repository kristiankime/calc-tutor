package com.artclod.util

sealed abstract class OneOfThree[A,B,C] {
  def isFirst : Boolean
  def isSecond: Boolean
  def isThird : Boolean

  def first : ofthree.FirstProjection[A, B, C]
  def second: ofthree.SecondProjection[A, B, C]
  def third : ofthree.ThirdProjection[A, B, C]
}

package ofthree {

  final case class FirstProjection[A, B, C](e: OneOfThree[A, B, C]) {
    def get: A = e match {
      case First(a)  => a
      case Second(_) => throw new NoSuchElementException("OneOfThree.second.get on First")
      case Third(_) => throw new NoSuchElementException("OneOfThree.third.get on First")
    }

    def getOrElse[AA >: A](or: => AA): AA = e match {
      case First(a)  => a
      case Second(_) => or
      case Third(_)  => or
    }
  }

  final case class SecondProjection[A, B, C](e: OneOfThree[A, B, C]) {
    def get: B = e match {
      case First(_) => throw new NoSuchElementException("OneOfThree.first.get on Second")
      case Second(b) => b
      case Third(_) => throw new NoSuchElementException("OneOfThree.third.get on Second")
    }

    def getOrElse[BB >: B](or: => BB): BB = e match {
      case First(_)  => or
      case Second(b) => b
      case Third(_)  => or
    }
  }

  final case class ThirdProjection[A, B, C](e: OneOfThree[A, B, C]) {
    def get: C = e match {
      case First(_) => throw new NoSuchElementException("OneOfThree.first.get on Third")
      case Second(_) => throw new NoSuchElementException("OneOfThree.second.get on Third")
      case Third(c) => c
    }

    def getOrElse[CC >: C](or: => CC): CC = e match {
      case First(_)  => or
      case Second(_) => or
      case Third(c)  => c
    }
  }

  case class First[A,B,C](value: A) extends OneOfThree[A,B,C] {
    override def isFirst  = true
    override def isSecond = false
    override def isThird  = false

    override def first  = FirstProjection(this)
    override def second = throw new NoSuchElementException("First is not a Second")
    override def third  = throw new NoSuchElementException("First is not a Third")

  }

  case class Second[A,B,C](value: B) extends OneOfThree[A,B,C] {
    override def isFirst  = false
    override def isSecond = true
    override def isThird  = false

    override def first  = throw new NoSuchElementException("Second is not a First")
    override def second = SecondProjection(this)
    override def third  = throw new NoSuchElementException("Second is not a Third")
  }

  case class Third[A,B,C](value: C) extends OneOfThree[A,B,C] {
    override def isFirst  = false
    override def isSecond = false
    override def isThird  = true

    override def first  = throw new NoSuchElementException("Third is not a First")
    override def second = throw new NoSuchElementException("Third is not a Second")
    override def third  = ThirdProjection(this)
  }
}