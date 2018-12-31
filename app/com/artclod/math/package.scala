package com.artclod

package object math {

  // https://stackoverflow.com/questions/51295199/limit-decimal-places-in-scala
  def limitDecimalPlaces(d: Double, p: Integer) :
     Double = {
        if (d >= 0) {
            BigDecimal(d).setScale(p, BigDecimal.RoundingMode.HALF_UP).toDouble
          }
        else
          d
       }
}
