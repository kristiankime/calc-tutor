package models

import models.user.User

sealed abstract class Access extends Ordered[Access] {
	val v: Short
	def read: Boolean
	def write: Boolean

	def compare(that: Access): Int = this.v.compare(that.v)

	def ceilEdit = Seq(this, Edit).min

	def read[T](block: () => T): Option[T] = if (read) { Some(block()) } else { None }
	def write[T](block: () => T): Option[T] = if (write) { Some(block()) } else { None }
}

case object Own extends Access {
	val v = 40.toShort
	override def read = true
	override def write = true
}

case object Edit extends Access {
	val v = 30.toShort
	override def read = true
	override def write = true
}

case object View extends Access {
	val v = 20.toShort
	override def read = true
	override def write = false
}

case object Non extends Access { // This is called Non so as to avoid naming conflicts with the Option None
	val v = 10.toShort
	override def read = false
	override def write = false
}

object Access {
	val non: Access = Non // Just for type change
	val view: Access = View // Just for type change
	val edit: Access = Edit // Just for type change
	val own: Access = Own // Just for type change

	def toNum(access: Access): Short = access.v

	def fromNum(access: Short) = access match {
		case Own.v => Own
		case Edit.v => Edit
		case View.v => View
		case Non.v => Non
		case _ => throw new IllegalArgumentException("number " + access + " does not match an access level")
	}

	def apply(in: Option[Access]): Access = in match {
		case Some(access) => access
		case None => Non
	}

	def apply(user: User, owner: UserId): Access = if (user.id == owner) { Own } else { Non }

}
