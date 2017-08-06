package com.artclod.mathml

import com.artclod.mathml.scalar.apply._
import com.artclod.mathml.scalar.concept._

import scala.language.implicitConversions

package object scalar {
	// Simple Constants
	val `-1000` = Cn(-1000)
	val `-12` = Cn(-12)
	val `-11` = Cn(-11)
	val `-10` = Cn(-10)
	val `-9` = Cn(-9)
	val `-8` = Cn(-8)
	val `-7` = Cn(-7)
	val `-6` = Cn(-6)
	val `-5` = Cn(-5)
	val `-4` = Cn(-4)
	val `-3` = Cn(-3)
	val `-2` = Cn(-2)
	val `-1` = Cn(-1)
	val `0` = Cn(0)
	val `1` = Cn(1)
	val `2` = Cn(2)
	val `3` = Cn(3)
	val `4` = Cn(4)
	val `5` = Cn(5)
	val `6` = Cn(6)
	val `7` = Cn(7)
	val `8` = Cn(8)
	val `9` = Cn(9)
	val `10` = Cn(10)
	val `11` = Cn(11)
	val `12` = Cn(12)
	val `13` = Cn(13)
	val `14` = Cn(14)
	val `15` = Cn(15)
	val `16` = Cn(16)
	val `17` = Cn(17)
	val `18` = Cn(18)
	val `19` = Cn(19)
	val `20` = Cn(20)
	val `21` = Cn(21)
	val `22` = Cn(22)
	val `23` = Cn(23)
	val `24` = Cn(24)
	val `25` = Cn(25)
	val `26` = Cn(26)
	val `27` = Cn(27)
	val `28` = Cn(28)
	val `29` = Cn(29)
	val `30` = Cn(30)
	val `31` = Cn(31)
	val `32` = Cn(32)
	val `33` = Cn(33)
	val `34` = Cn(34)
	val `35` = Cn(35)
	val `36` = Cn(36)
	val `37` = Cn(37)
	val `38` = Cn(38)
	val `39` = Cn(39)
	val `100` = Cn(100)
	val `101` = Cn(101)
	val `1000` = Cn(1000)
	val `1001` = Cn(1001)

	val `-.5` = Cn(-.5)
	val `.5` = Cn(.5)
	val `1.5` = Cn(1.5)
	val `2.5` = Cn(2.5)
	val `3.5` = Cn(3.5)
	val `-100` = Cn(-100)
	val `-101` = Cn(-101)

	val ln_10 = Cn(math.log(10))

	// Important Constants
	val e = ExponentialE
	val π = Pi
	
	// Implicit number conversions
	implicit def short2Cn(v: Short) : ConstantInteger = Cn(v)
	implicit def int2Cn(v: Int): ConstantInteger = Cn(v)
	implicit def long2Cn(v: Long) : ConstantInteger = Cn(v)
	implicit def float2Cn(v: Float) : Constant = Cn(v)
	implicit def double2Cn(v: Double) : Constant = Cn(v)
	
	// Variables
	val x = Ci("x")
	val y = Ci("y")
	
	// Nice function names
	def √(e : MathMLElem) = ApplySqrt(e)
	def ∛(e : MathMLElem) = ApplyRoot(3, e)
	def ∜(e : MathMLElem) = ApplyRoot(4, e)
	def `n√`(n: BigDecimal)(e : MathMLElem) = ApplyRoot(n, e)
	
	val ln = ApplyLn
	val log = ApplyLog
	
	val cos = trig.ApplyCos
	val cot = trig.ApplyCot
	val csc = trig.ApplyCsc
	val sec = trig.ApplySec
	val sin = trig.ApplySin
	val tan = trig.ApplyTan
}
