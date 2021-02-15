package com.gsk.kg.engine

import com.gsk.kg.sparqlparser.Expr
import com.gsk.kg.sparqlparser.StringVal.VARIABLE

sealed trait Predicate

object Predicate {
  case class SPO(s: String, p: String, o: String) extends Predicate
  case class SP(s: String, p: String) extends Predicate
  case class PO(p: String, o: String) extends Predicate
  case class SO(s: String, o: String) extends Predicate
  case class S(s: String) extends Predicate
  case class P(p: String) extends Predicate
  case class O(o: String) extends Predicate
  case object None extends Predicate


  def fromTriple(triple: Expr.Triple): Predicate =
    triple match {
      case Expr.Triple(VARIABLE(_), VARIABLE(_), VARIABLE(_)) => Predicate.None
      case Expr.Triple(s, VARIABLE(_), VARIABLE(_))           => Predicate.S(s.s)
      case Expr.Triple(VARIABLE(_), p, VARIABLE(_))           => Predicate.P(p.s)
      case Expr.Triple(VARIABLE(_), VARIABLE(_), o)           => Predicate.O(o.s)
      case Expr.Triple(s, p, VARIABLE(_))                     => Predicate.SP(s.s, p.s)
      case Expr.Triple(VARIABLE(_), p, o)                     => Predicate.PO(p.s, o.s)
      case Expr.Triple(s, VARIABLE(_), o)                     => Predicate.SO(s.s, o.s)
      case Expr.Triple(s, p, o)                               => Predicate.SPO(s.s, p.s, o.s)
    }
}
