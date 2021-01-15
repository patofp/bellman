import fastparse._, MultiLineWhitespace._

object ExprParser {
  /*
  Graph patterns
   */
  def bgp[_:P]:P[Unit] = P("bgp")
  def leftJoin[_:P]:P[Unit] = P("leftjoin")
  def union[_:P]:P[Unit] = P("union")
  def extend[_:P]:P[Unit] = P("extend")
  def filter[_:P]:P[Unit] = P("filter")
  def exprList[_:P]:P[Unit] = P("exprlist")
  def join[_:P]:P[Unit] = P("join")
  def graph[_:P]:P[Unit] = P("graph")

  def triple[_:P]:P[Triple] =
    P("(triple" ~
      StringValParser.tripleValParser ~
      StringValParser.tripleValParser ~
      StringValParser.tripleValParser ~ ")").map(t => Triple(t._1, t._2, t._3))

  def bgpParen[_:P]:P[BGP] = P("(" ~ bgp ~ triple.rep(1) ~ ")").map(BGP(_))

  def filterExprList[_:P]:P[Seq[FilterFunction]] =
    P("(" ~ exprList ~ FilterFunctionParser.parser.rep(2) ~ ")")

  def filterListParen[_:P]:P[Filter] =
    P("(" ~ filter ~ filterExprList ~ graphPattern ~ ")").map {
      p => Filter(p._1, p._2)
    }

  def filterSingleParen[_:P]:P[Filter] =
    P("(" ~ filter ~ FilterFunctionParser.parser ~ graphPattern ~ ")").map {
      p => Filter(List(p._1), p._2)
    }

  def leftJoinParen[_:P]:P[LeftJoin] = P("(" ~ leftJoin ~ graphPattern ~ graphPattern ~ ")").map{
    lj => LeftJoin(lj._1, lj._2)
  }

  def filteredLeftJoinParen[_:P]:P[FilteredLeftJoin] = P("(" ~ leftJoin ~ graphPattern ~ graphPattern ~ FilterFunctionParser.parser ~ ")").map{
    lj => FilteredLeftJoin(lj._1, lj._2, lj._3)
  }

  def unionParen[_:P]:P[Union] = P("(" ~ union ~ graphPattern ~ graphPattern ~ ")").map {
    u => Union(u._1, u._2)
  }
  def extendParen[_:P]:P[Extend] = P("(" ~
    extend ~ "((" ~ (StringValParser.tripleValParser | StringFuncParser.parser) ~
    (StringValParser.tripleValParser | StringFuncParser.parser) ~ "))" ~
    graphPattern ~ ")").map{
    ext => Extend(ext._1, ext._2, ext._3)
  }

  def joinParen[_:P]:P[Join] = P("(" ~ join ~ graphPattern ~ graphPattern ~ ")").map{
    p => Join(p._1, p._2)
  }

  def graphParen[_:P]:P[Graph] = P("(" ~ graph ~ StringValParser.urival ~ graphPattern ~ ")").map{
    p => Graph(p._1, p._2)
  }

  def graphPattern[_:P]:P[Expr] =
    P(leftJoinParen
      | filteredLeftJoinParen
      | joinParen
      | graphParen
      | bgpParen
      | unionParen
      | extendParen
      | filterSingleParen
      | filterListParen)

  def parser[_:P]:P[Expr] = P(graphPattern)
}
