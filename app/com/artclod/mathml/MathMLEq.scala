package com.artclod.mathml

import com.artclod.mathml.scalar._

import scala.util._

//object Match extends Enumeration {
//	type Match = Value
//	val Yes, No, Inconclusive = Value
//}

sealed trait Match {
	val num : Short
}

object Yes extends Match { val num = 0.toShort; }
object No extends Match { val num = 1.toShort; }
object Inconclusive extends Match { val num = 2.toShort; }


object Match {
	def from(num: Short): Match = {
		num match {
			case 0 => Yes
			case 1 => No
			case 2 => Inconclusive
		}
	}
}

/**
 * This object has methods for checking to see if two functions (of a single variable) are the same.
 * To test equality between functions we evaluate them a series of random points and check to see if the values are the same.
 * Currently we use a fixed set of random values so the process is "deterministic".
 * Note that it is possible for the function to not evaluate (i.e. 0/0, -1/0 etc) at any of the points.
 * For this reason we can return Yes, No or Inconclusive.
 */
object MathMLEq {
	// At least for now use a fixed set of pseudo random values
	private val ran = new Random(0L)
	// We generate our random numbers from two buckets
	// a "tight" range from -10 to 10
	val tightRange = 10d;
	private val tightRange2 = tightRange * 2d;
	// and a "wide" range from -400 to 400
	val wideRange = 400d;
	val wideRange2 = wideRange * 2;
	// which then get appended together
	val valsTight = Vector.fill(40)((ran.nextDouble * tightRange2) - tightRange)
	val vals = (Vector.fill(20)((ran.nextDouble * wideRange2) - wideRange) ++ valsTight).sorted

	// Since Double arithmetic is not exact we must decide things like "how close is good enough",
  // And generally ignore numbers that are too large or small, etc
	private val tooSmall = 1E-154 // LATER figure out how small is too small :( i.e. 1e-312 works for most tests...
	private val tooBig = 1E154 // LATER figure out how big is too big
	private val ε = .00001d
  private val closeEnoughTo0 = 1E-13 // LATER how close is close enough :(

	/**
	 * The main method that checks for equality of two functions (of one specified variable)
	 */
	def checkEq(variableName: String, eq1: MathMLElem, eq2: MathMLElem) = checkEval(variableName, eq1, eq2, vals)

	def checkEval(variable: String, eq1: MathMLElem, eq2: MathMLElem, vals: Seq[Double]): Match = {
		val eq1s = vals.map(value => eq1.evalT(variable -> value))
		val eq2s = vals.map(value => eq2.evalT(variable -> value))
		val matches = eq1s.zip(eq2s).map(v => closeEnough(v._1, v._2))

//    System.err.println("eq1")
//    System.err.println(eq1)
//    System.err.println("eq2")
//    System.err.println(eq2)
//    System.err.println("eq2")
//		System.err.println((vals, eq1s.zip(eq2s), matches).zipped.map((a, b, c) => "val=[" + a + "] evals=[" + b + "] match=[" + c + "]\n"))

    matches.reduce(matchCombine)
	}

  def matchCombine(a : Match, b: Match) : Match = (a , b) match {
    case (No, _) => No
    case (_, No) => No // If we ever see a No they are not a match
    case (Yes, _) => Yes
    case (_, Yes) => Yes // If we have Inconclusive and yes conclude yes
    case (Inconclusive, Inconclusive) => Inconclusive // If we only have Inconclusive...
  }

	private def closeEnough(v1: Try[Double], v2: Try[Double]) =
		(v1, v2) match {
			case (Success(x), Success(y)) => doubleCloseEnough(x, y)
			case (_, _) => Inconclusive
		}

	def doubleCloseEnough(x: Double, y: Double) : Match  = {
		if (x.isNaN || y.isNaN || x.isInfinite || y.isInfinite) Inconclusive
		else if (x == y) Yes
    else if (x == 0d && y.abs <= closeEnoughTo0) Yes
    else if (y == 0d && x.abs <= closeEnoughTo0) Yes
		else if (x.abs < tooSmall && y.abs < tooSmall) Inconclusive
		else if (x.abs > tooBig && y.abs > tooBig) Inconclusive
		else if (doubleNumbersCloseEnough(x, y)) Yes
		else No
	}

	def doubleNumbersCloseEnough(x: Double, y: Double) = (x - y).abs <= ε * (x.abs + y.abs)

	//	def doubleNumbersCloseEnough(x: Double, y: Double) = {
	//		val ret = (x - y).abs <= ε * (x.abs + y.abs)
	//
	//		if (!ret) {
	//			System.err.println("x                   = " + x)
	//			System.err.println("y                   = " + y)
	//			System.err.println("ε * (x.abs + y.abs) = " + ε * (x.abs + y.abs))
	//			System.err.println("(x - y).abs         = " + (x - y).abs)
	//			System.err.println("ratio               = " + (x - y).abs / (x.abs + y.abs))
	//			System.err.println
	//		}
	//
	//		ret
	//	}

  //  def main(args: Array[String]) {
  //
  //    var x = -1d
  //    while( math.pow(math.E, x) == (2.718281828459045 * math.pow(math.E, x - 1d)) ) {
  //      System.err.println(x)
  //      System.err.println(math.pow(math.E, x))
  //      System.err.println((2.718281828459045 * math.pow(math.E, x - 1d)))
  //      x = x - 1d
  //    }
  //
  //    System.err.println("failed at")
  //    System.err.println(x)
  //    System.err.println(math.pow(math.E, x))
  //    System.err.println((2.718281828459045 * math.pow(math.E, x - 1d)))
  //
  //  }
}
