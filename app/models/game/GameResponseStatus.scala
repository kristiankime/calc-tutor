package models.game

sealed trait GameResponseStatus {
  val v: Short
}

object GameResponseStatus extends (Short => GameResponseStatus) {
  def apply(v: Short) = v match {
    case Requested.v => Requested
    case Accepted.v => Accepted
    case Rejected.v => Rejected
    case _ => throw new IllegalArgumentException("value [" + v + "] was not recognized as a Response Status")
  }

  val requested : GameResponseStatus = Requested
  val accepted : GameResponseStatus = Accepted
  val rejected : GameResponseStatus = Rejected
}

object Requested extends GameResponseStatus {
  val v: Short = 0
  override val toString = "Requested"
}

object Accepted extends GameResponseStatus {
  val v: Short = 1
  override val toString = "Accepted"
}

object Rejected extends GameResponseStatus {
  val v: Short = 2
  override val toString = "Rejected"
}
