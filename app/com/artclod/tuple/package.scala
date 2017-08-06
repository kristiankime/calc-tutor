package com.artclod

package object tuple {

  def listZip[A, B, C](a : Iterable[A], b : Iterable[B], c : Iterable[C]) = {
    a.zip(b).zip(c).map(t => (t._1._1, t._1._2, t._2)).toList
  }

  implicit class MapTuple2[T1, T2](t :(T1, T2)){
    def map[O](f : ((T1, T2)) => O) = f(t)

    def toList[O](f1: T1 => List[O], f2: T2 => List[O]) = f1(t._1) ++ f2(t._2)
  }

  implicit class MapTuple3[T1, T2, T3](t :(T1, T2, T3)){
    def map[O](f : ((T1, T2, T3)) => O) = f(t)

    def toList[O](f1: T1 => List[O], f2: T2 => List[O], f3: T3 => List[O]) = f1(t._1) ++ f2(t._2) ++ f3(t._3)
  }

  implicit class MapTuple4[T1, T2, T3, T4](t :(T1, T2, T3, T4)){
    def map[O](f : ((T1, T2, T3, T4)) => O) = f(t)

    def toList[O](f1: T1 => List[O], f2: T2 => List[O], f3: T3 => List[O], f4: T4 => List[O]) = f1(t._1) ++ f2(t._2) ++ f3(t._3) ++ f4(t._4)
  }

  implicit class MapTuple5[T1, T2, T3, T4, T5](t :(T1, T2, T3, T4, T5)){
    def map[O](f : ((T1, T2, T3, T4, T5)) => O) = f(t)

    def toList[O](f1: T1 => List[O], f2: T2 => List[O], f3: T3 => List[O], f4: T4 => List[O], f5: T5 => List[O]) = f1(t._1) ++ f2(t._2) ++ f3(t._3) ++ f4(t._4) ++ f5(t._5)
  }

  implicit class MapTuple6[T1, T2, T3, T4, T5, T6](t :(T1, T2, T3, T4, T5, T6)){
    def map[O](f : ((T1, T2, T3, T4, T5, T6)) => O) = f(t)

    def toList[O](f1: T1 => List[O], f2: T2 => List[O], f3: T3 => List[O], f4: T4 => List[O], f5: T5 => List[O], f6: T6 => List[O]) = f1(t._1) ++ f2(t._2) ++ f3(t._3) ++ f4(t._4) ++ f5(t._5) ++ f6(t._6)
  }

  implicit class MapTuple7[T1, T2, T3, T4, T5, T6, T7](t :(T1, T2, T3, T4, T5, T6, T7)){
    def map[O](f : ((T1, T2, T3, T4, T5, T6, T7)) => O) = f(t)

    def toList[O](f1: T1 => List[O], f2: T2 => List[O], f3: T3 => List[O], f4: T4 => List[O], f5: T5 => List[O], f6: T6 => List[O], f7: T7 => List[O]) = f1(t._1) ++ f2(t._2) ++ f3(t._3) ++ f4(t._4) ++ f5(t._5) ++ f6(t._6) ++ f7(t._7)
  }

  implicit class MapTuple8[T1, T2, T3, T4, T5, T6, T7, T8](t :(T1, T2, T3, T4, T5, T6, T7, T8)){
    def map[O](f : ((T1, T2, T3, T4, T5, T6, T7, T8)) => O) = f(t)

    def toList[O](f1: T1 => List[O], f2: T2 => List[O], f3: T3 => List[O], f4: T4 => List[O], f5: T5 => List[O], f6: T6 => List[O], f7: T7 => List[O], f8: T8 => List[O]) = f1(t._1) ++ f2(t._2) ++ f3(t._3) ++ f4(t._4) ++ f5(t._5) ++ f6(t._6) ++ f7(t._7) ++ f8(t._8)
  }
}
