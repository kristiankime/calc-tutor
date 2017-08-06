package com.artclod.mathml.scalar.concept

import com.artclod.math.TrigonometryFix
import com.artclod.mathml.scalar._

object Trigonometry {

  def cos(v: Double) : Double = TrigonometryFix.cos0(v)

  def sin(v: Double) : Double = TrigonometryFix.sin0(v)

  def tan(v: Double) : Double = TrigonometryFix.tan0(v)

	def csc(v: Double) : Double = TrigonometryFix.csc0(v)
	
	def sec(v: Double) : Double = TrigonometryFix.sec0(v)
	
	def cot(v: Double) : Double = TrigonometryFix.cot0(v)
	
	def sin(value: Constant) = value match {
		case c: ConstantInteger => Cn(TrigonometryFix.sin0(c.v.doubleValue))
		case c: ConstantDecimal => Cn(TrigonometryFix.sin0(c.v.doubleValue))
	}

	def cos(value: Constant) = value match {
		case c: ConstantInteger => Cn(TrigonometryFix.cos0(c.v.doubleValue))
		case c: ConstantDecimal => Cn(TrigonometryFix.cos0(c.v.doubleValue))
	}
	
	def tan(value: Constant) = value match {
		case c: ConstantInteger => Cn(TrigonometryFix.tan0(c.v.doubleValue))
		case c: ConstantDecimal => Cn(TrigonometryFix.tan0(c.v.doubleValue))
	}
	
	def csc(value: Constant) : Constant = value match {
		case c: ConstantInteger => Cn(TrigonometryFix.csc0(c.v.doubleValue))
		case c: ConstantDecimal => Cn(TrigonometryFix.csc0(c.v.doubleValue))
	}
	
	def sec(value: Constant) : Constant = value match {
		case c: ConstantInteger => Cn(TrigonometryFix.sec0(c.v.doubleValue))
		case c: ConstantDecimal => Cn(TrigonometryFix.sec0(c.v.doubleValue))
	}
	
	def cot(value: Constant) : Constant = value match {
		case c: ConstantInteger => Cn(TrigonometryFix.cot0(c.v.doubleValue))
		case c: ConstantDecimal => Cn(TrigonometryFix.cot0(c.v.doubleValue))
	}
}