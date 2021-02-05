package com.gsk.kg.sparqlparser

sealed trait FilterFunction

object FilterFunction {
  final case class EQUALS(l:StringLike, r:StringLike) extends FilterFunction
  final case class REGEX(l:StringLike, r:StringLike) extends FilterFunction
  final case class STRSTARTS(l:StringLike, r:StringLike) extends FilterFunction
  final case class GT(l:StringLike, r:StringLike) extends FilterFunction
  final case class LT(l:StringLike, r:StringLike) extends FilterFunction
}


