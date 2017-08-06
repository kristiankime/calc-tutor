package com.artclod.collection

// ============
abstract sealed class AtMostOneOf2[A,B] {
  val _1 : Option[A]
  val _2 : Option[B]

  def none = _1.isEmpty && _2.isEmpty
  def first = _1.nonEmpty
  def second = _2.nonEmpty
}

case object NoneOfAt2 extends AtMostOneOf2[Nothing, Nothing] {
  val _1 = None
  val _2 = None
}

case class FirstOfAt2[A,B](a : A) extends AtMostOneOf2[A, B] {
  val _1 = Some(a)
  val _2 = None
}

case class SecondOfAt2[A,B](b : B) extends AtMostOneOf2[A, B] {
  val _1 = None
  val _2 = Some(b)
}