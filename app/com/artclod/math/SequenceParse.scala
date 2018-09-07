package com.artclod.math

import scala.util.{Success, Try}

object SequenceParse {
//  private val infinityStrings = Set("infinity", "inf", "∞")
//  private val positiveStrings = for( a <- Set("", "+", "pos", "positive"); b <- Set("", " ")) yield { a + b }
//  private val negativeStrings = for( a <- Set(    "-", "neg", "negative"); b <- Set("", " ")) yield { a + b }
//  val positiveInfinityStrings = for(p <- positiveStrings; i <- infinityStrings) yield { p + i }
//  val negativeInfinityStrings = for(p <- negativeStrings; i <- infinityStrings) yield { p + i }
//  val allInf = positiveInfinityStrings ++ negativeInfinityStrings
//  val allInfForRegex = allInf.map(infStr =>  infStr.replace("+", "\\+") );

  def apply(str: String): Try[Vector[Double]] = null
//    if(     positiveInfinityStrings.contains(str.toLowerCase)) { Success(Double.PositiveInfinity)}
//    else if(negativeInfinityStrings.contains(str.toLowerCase)) { Success(Double.NegativeInfinity)}
//    else{                                                            Try(str.toDouble)           }


}
