package com.artclod.collection

// ============
abstract sealed class OneOf2[A,B] {
  val _1 : Option[A]
  val _2 : Option[B]

  def first = _1.nonEmpty
  def second = _2.nonEmpty
}

case class FirstOf2[A,B](a : A) extends OneOf2[A, B] {
  val _1 = Some(a)
  val _2 = None
}

case class SecondOf2[A,B](b : B) extends OneOf2[A, B] {
  val _1 = None
  val _2 = Some(b)
}

// ============
abstract sealed class OneOf3[A,B, C] {
  val _1 : Option[A]
  val _2 : Option[B]
  val _3 : Option[C]

  def first = _1.nonEmpty
  def second = _2.nonEmpty
  def third = _3.nonEmpty
}

case class FirstOf3[A,B,C](a : A) extends OneOf3[A, B, C] {
  val _1 = Some(a)
  val _2 = None
  val _3 = None
}

case class SecondOf3[A,B, C](b : B) extends OneOf3[A, B, C] {
  val _1 = None
  val _2 = Some(b)
  val _3 = None
}

case class ThirdOf3[A,B, C](c : C) extends OneOf3[A, B, C] {
  val _1 = None
  val _2 = None
  val _3 = Some(c)
}