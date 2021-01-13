sealed trait Expr

final case class BGP(triples:Seq[Triple]) extends Expr
final case class Triple(s:StringVal, p:StringVal, o:StringVal) extends Expr
final case class LeftJoin(l:Expr, r:Expr) extends Expr
final case class Union(l:Expr, r:Expr) extends Expr
final case class Extend(bindTo:StringLike, bindFrom:StringLike, r:Expr) extends Expr
final case class FilterExpr(func:FilterFunction, left:StringVal, right:StringVal) extends Expr
final case class Filter(funcs:Seq[FilterExpr], expr:Expr) extends Expr
final case class Join(l:Expr, r:Expr) extends Expr
final case class Graph(g:StringVal, e:Expr) extends Expr
