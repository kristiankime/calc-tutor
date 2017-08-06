package com.artclod.collection

/**
 * The purpose of the MustHandle classes is for future proofing.
 * The intent is that you will initially construct a MustHandle from the object's apply method and then use the -> method.
 * Then later on add another element to the MustHandle.apply call.
 * At this point the code will fail to compile and you will get an error at the appropriate -> call and be forced to update the code there.
 */
object MustHandle {

  def apply[V1](v1: V1) = MustHandle1(v1)

  def fromTuple[V1](v: Tuple1[V1]) = MustHandle1(v._1)

  def apply[V1, V2](v1: V1, v2: V2) = MustHandle2(v1, v2)

  def fromTuple[V1, V2](v: (V1, V2)) = MustHandle2(v._1, v._2)

  def apply[V1, V2, V3](v1: V1, v2: V2, v3: V3) = MustHandle3(v1, v2, v3)

  def fromTuple[V1, V2, V3](v: (V1, V2, V3)) = MustHandle3(v._1, v._2, v._3)

  def apply[V1, V2, V3, V4](v1: V1, v2: V2, v3: V3, v4: V4) = MustHandle4(v1, v2, v3, v4)

  def fromTuple[V1, V2, V3, V4](v: (V1, V2, V3, V4)) = MustHandle4(v._1, v._2, v._3, v._4)

  def apply[V1, V2, V3, V4, V5](v1: V1, v2: V2, v3: V3, v4: V4, v5: V5) = MustHandle5(v1, v2, v3, v4, v5)

  def fromTuple[V1, V2, V3, V4, V5](v: (V1, V2, V3, V4, V5)) = MustHandle5(v._1, v._2, v._3, v._4, v._5)

  def apply[V1, V2, V3, V4, V5, V6](v1: V1, v2: V2, v3: V3, v4: V4, v5: V5, v6: V6) = MustHandle6(v1, v2, v3, v4, v5, v6)

  def fromTuple[V1, V2, V3, V4, V5, V6](v: (V1, V2, V3, V4, V5, V6)) = MustHandle6(v._1, v._2, v._3, v._4, v._5, v._6)

  def apply[V1, V2, V3, V4, V5, V6, V7](v1: V1, v2: V2, v3: V3, v4: V4, v5: V5, v6: V6, v7: V7) = MustHandle7(v1, v2, v3, v4, v5, v6, v7)

  def fromTuple[V1, V2, V3, V4, V5, V6, V7](v: (V1, V2, V3, V4, V5, V6, V7)) = MustHandle7(v._1, v._2, v._3, v._4, v._5, v._6, v._7)

  def apply[V1, V2, V3, V4, V5, V6, V7, V8](v1: V1, v2: V2, v3: V3, v4: V4, v5: V5, v6: V6, v7: V7, v8: V8) = MustHandle8(v1, v2, v3, v4, v5, v6, v7, v8)

  def fromTuple[V1, V2, V3, V4, V5, V6, V7, V8](v: (V1, V2, V3, V4, V5, V6, V7, V8)) = MustHandle8(v._1, v._2, v._3, v._4, v._5, v._6, v._7, v._8)

}

case class MustHandle1[T1](v1: T1) {
  def ->[O1](f1: (T1) => O1) = f1(v1)

  def zip[Z1](z : MustHandle1[Z1]) = MustHandle( (v1, z.v1) )

  def size = 1
}

case class MustHandle2[T1, T2](v1: T1, v2: T2) {
  def ->[O1, O2](f1: (T1) => O1, f2: (T2) => O2) = (f1(v1), f2(v2))

  def zip[Z1, Z2](z : MustHandle2[Z1, Z2]) = MustHandle( (v1, z.v1), (v2, z.v2) )

  def size = 2
}

case class MustHandle3[T1, T2, T3](v1: T1, v2: T2, v3: T3) {
  def ->[O1, O2, O3](f1: (T1) => O1, f2: (T2) => O2, f3: (T3) => O3) = (f1(v1), f2(v2), f3(v3))

  def zip[Z1, Z2, Z3](z : MustHandle3[Z1, Z2, Z3]) = MustHandle( (v1, z.v1), (v2, z.v2), (v3, z.v3) )

  def size = 3
}

case class MustHandle4[T1, T2, T3, T4](v1: T1, v2: T2, v3: T3, v4: T4) {
  def ->[O1, O2, O3, O4](f1: (T1) => O1, f2: (T2) => O2, f3: (T3) => O3, f4: (T4) => O4) = (f1(v1), f2(v2), f3(v3), f4(v4))

  def zip[Z1, Z2, Z3, Z4](z : MustHandle4[Z1, Z2, Z3, Z4]) = MustHandle( (v1, z.v1), (v2, z.v2), (v3, z.v3), (v4, z.v4) )

  def size = 4
}

case class MustHandle5[T1, T2, T3, T4, T5](v1: T1, v2: T2, v3: T3, v4: T4, v5: T5) {
  def ->[O1, O2, O3, O4, O5](f1: (T1) => O1, f2: (T2) => O2, f3: (T3) => O3, f4: (T4) => O4, f5: (T5) => O5) = (f1(v1), f2(v2), f3(v3), f4(v4), f5(v5))

  def zip[Z1, Z2, Z3, Z4, Z5](z : MustHandle5[Z1, Z2, Z3, Z4, Z5]) = MustHandle( (v1, z.v1), (v2, z.v2), (v3, z.v3), (v4, z.v4), (v5, z.v5) )

  def size = 5
}

case class MustHandle6[T1, T2, T3, T4, T5, T6](v1: T1, v2: T2, v3: T3, v4: T4, v5: T5, v6: T6) {
  def ->[O1, O2, O3, O4, O5, O6](f1: (T1) => O1, f2: (T2) => O2, f3: (T3) => O3, f4: (T4) => O4, f5: (T5) => O5, f6: (T6) => O6) = (f1(v1), f2(v2), f3(v3), f4(v4), f5(v5), f6(v6))

  def zip[Z1, Z2, Z3, Z4, Z5, Z6](z : MustHandle6[Z1, Z2, Z3, Z4, Z5, Z6]) = MustHandle( (v1, z.v1), (v2, z.v2), (v3, z.v3), (v4, z.v4), (v5, z.v5), (v6, z.v6) )

  def size = 6
}

case class MustHandle7[T1, T2, T3, T4, T5, T6, T7](v1: T1, v2: T2, v3: T3, v4: T4, v5: T5, v6: T6, v7: T7) {
  def ->[O1, O2, O3, O4, O5, O6, O7](f1: (T1) => O1, f2: (T2) => O2, f3: (T3) => O3, f4: (T4) => O4, f5: (T5) => O5, f6: (T6) => O6, f7: (T7) => O7) = (f1(v1), f2(v2), f3(v3), f4(v4), f5(v5), f6(v6), f7(v7))

  def zip[Z1, Z2, Z3, Z4, Z5, Z6, Z7](z : MustHandle7[Z1, Z2, Z3, Z4, Z5, Z6, Z7]) = MustHandle( (v1, z.v1), (v2, z.v2), (v3, z.v3), (v4, z.v4), (v5, z.v5), (v6, z.v6), (v7, z.v7) )

  def zip[Z1, Z2, Z3, Z4, Z5, Z6, Z7, X1, X2, X3, X4, X5, X6, X7](z : MustHandle7[Z1, Z2, Z3, Z4, Z5, Z6, Z7], x : MustHandle7[X1, X2, X3, X4, X5, X6, X7]) = MustHandle( (v1, z.v1, x.v1), (v2, z.v2, x.v2), (v3, z.v3, x.v3), (v4, z.v4, x.v4), (v5, z.v5, x.v5), (v6, z.v6, x.v6), (v7, z.v7, x.v7) )

  def size = 7
}

case class MustHandle8[T1, T2, T3, T4, T5, T6, T7, T8](v1: T1, v2: T2, v3: T3, v4: T4, v5: T5, v6: T6, v7: T7, v8: T8) {
  def ->[O1, O2, O3, O4, O5, O6, O7, O8](f1: (T1) => O1, f2: (T2) => O2, f3: (T3) => O3, f4: (T4) => O4, f5: (T5) => O5, f6: (T6) => O6, f7: (T7) => O7, f8: (T8) => O8) = (f1(v1), f2(v2), f3(v3), f4(v4), f5(v5), f6(v6), f7(v7), f8(v8))

  def zip[Z1, Z2, Z3, Z4, Z5, Z6, Z7, Z8](z : MustHandle8[Z1, Z2, Z3, Z4, Z5, Z6, Z7, Z8]) = MustHandle( (v1, z.v1), (v2, z.v2), (v3, z.v3), (v4, z.v4), (v5, z.v5), (v6, z.v6), (v7, z.v7), (v8, z.v8) )

  def size = 8
}
