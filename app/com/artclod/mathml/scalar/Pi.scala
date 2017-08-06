package com.artclod.mathml.scalar

import com.artclod.mathml.MathML
import com.artclod.mathml.scalar.concept.ConstantDecimal

object Pi extends ConstantDecimal("pi", MathML.h.attributes, true, BigDecimal(math.Pi), Seq(): _*) {
  override def toMathJS: String = "pi"
}