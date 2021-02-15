package com.gsk.kg.sparqlparser

sealed trait FilterFunction extends Expression

object FilterFunction {
  final case class EQUALS(l:Expression, r:Expression) extends FilterFunction
  final case class REGEX(l:Expression, r:Expression) extends FilterFunction
  final case class STRSTARTS(l:Expression, r:Expression) extends FilterFunction
  final case class GT(l:Expression, r:Expression) extends FilterFunction
  final case class LT(l:Expression, r:Expression) extends FilterFunction
  final case class OR(l:Expression, r:Expression) extends FilterFunction
  final case class AND(l:Expression, r:Expression) extends FilterFunction
  final case class NEGATE(s: Expression) extends FilterFunction

}


