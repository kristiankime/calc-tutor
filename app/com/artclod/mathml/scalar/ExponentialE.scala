package com.artclod.mathml.scalar

import com.artclod.mathml.MathML
import com.artclod.mathml.scalar.concept.ConstantDecimal

object ExponentialE extends ConstantDecimal("exponentiale", MathML.h.attributes, true, BigDecimal(math.E), Seq(): _*){
  override def toMathJS: String = "e"
}