sealed trait Expr

final case class BGP(triples:Seq[Triple]) extends Expr
final case class Triple(s:String, p:String, o:String) extends Expr
final case class LeftJoin(l:Expr, r:Expr) extends Expr
final case class Union(l:Expr, r:Expr) extends Expr
final case class Extend(bindTo:String, bindFrom:String, r:Expr) extends Expr
final case class FilterFunction(func:String, left:String, right:String) extends Expr
final case class Filter(funcs:Seq[FilterFunction], expr:Expr) extends Expr
final case class Join(l:Expr, r:Expr) extends Expr
final case class Graph(g:String, e:Expr) extends Expr