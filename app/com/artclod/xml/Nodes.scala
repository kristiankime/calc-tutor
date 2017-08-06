package com.artclod.xml

import scala.xml.Node
import scala.annotation.tailrec

object Nodes {

  def nodeCount(e : Node) : Int =
    if(e.child.isEmpty){ 1 }
    else { 1 + e.child.map(n => nodeCount(n)).reduce(_ + _) }

}
