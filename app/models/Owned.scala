package models

trait Owned {

  def ownerId : UserId

//  def owner(implicit s: Session) = LoginsTable.findById(ownerId).get

}
