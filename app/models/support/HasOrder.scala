package models.support

trait HasOrder[T <: HasOrder[T]] extends Ordered[T] {
  def order : Short

  override def compare(that: T): Int = this.order compareTo that.order
 }
