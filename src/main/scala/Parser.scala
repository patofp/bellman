import fastparse._, MultiLineWhitespace._

object Parser {
  def bgp[_:P] = P("bgp")
  def leftJoin[_:P] = P("leftjoin")
  def union[_:P] = P("union")
  def extend[_:P] = P("extend")
  def filter[_:P] = P("filter")
  def exprList[_:P] = P("exprlist")

  def triple[_:P] =
    P("(triple" ~
      CharsWhile(_ != ' ').! ~
      CharsWhile(_ != ' ').! ~
      CharsWhile(_ != ')').! ~ ")").map(t => Triple(t._1, t._2, t._3))

  def filterFunction[_:P] =
    P("(" ~ CharsWhile(_ != ' ').! ~ CharsWhile(_ != ' ').! ~ CharsWhile(_ != ')').! ~ ")").map {
      f => FilterFunction(f._1, f._2, f._3)
    }

  def filterExprList[_:P] =
    P("(" ~ exprList ~ filterFunction.rep(2) ~ ")")

  def filterListParen[_:P] =
    P("(" ~ filter ~ filterExprList ~ graphPattern ~ ")").map {
      p => Filter(p._1, p._2)
    }

  def filterSingleParen[_:P] =
    P("(" ~ filter ~ filterFunction ~ graphPattern ~ ")").map {
      p => Filter(List(p._1), p._2)
    }

  def graphPattern[_:P]:P[Expr] = P(leftJoinParen | bgpParen | unionParen | extendParen | filterSingleParen | filterListParen)
  def bgpParen[_:P]:P[BGP] = P("(" ~ bgp ~ triple.rep(1) ~ ")").map(BGP(_))

  def leftJoinParen[_:P]:P[LeftJoin] = P("(" ~ leftJoin ~ graphPattern ~ bgpParen ~ ")").map{
    lj => LeftJoin(lj._1, lj._2)
  }

  def unionParen[_:P]:P[Union] = P("(" ~ union ~ graphPattern ~ graphPattern ~ ")").map {
    u => Union(u._1, u._2)
  }
  def extendParen[_:P]:P[Extend] = P("(" ~
    extend ~ "((" ~ CharsWhile(_ != ' ').! ~
    CharsWhile(_ != ')').! ~ "))" ~
    graphPattern ~ ")").map{
    ext => Extend(ext._1, ext._2, ext._3)
  }
  def parser[_:P]:P[Expr] = P(graphPattern)
}