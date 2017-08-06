package com.artclod

import scala.collection.mutable.ListBuffer

package object slick {

  def listGroupBy[E, A, B](list: List[E])(keyFunc : E => A, valFunc : E => B) : List[ListGroup[A, B]] =
    if(list.isEmpty) { List() }
    else {
      val ret = ListBuffer[ListGroup[A, B]]()
      var key = keyFunc(list(0))
      var currentListGroup = ListBuffer[B]()

      for(e <- list) {
        val checkKey = keyFunc(e)
        if(checkKey == key) {
          currentListGroup += valFunc(e)
        } else {
          ret += ListGroup(key, currentListGroup.toList)
          currentListGroup = ListBuffer(valFunc(e))
          key = checkKey
        }
      }

      if(currentListGroup.nonEmpty) {
        ret += ListGroup(key, currentListGroup.toList)
      }
      ret.toList
    }


}
