import ExprParser.graphPattern
import fastparse._, MultiLineWhitespace._

object StringFuncParser {
  /*
  Functions on strings: https://www.w3.org/TR/sparql11-query/#func-strings
   */
  def uri[_:P]:P[Unit] = P("uri")
  def concat[_:P]:P[Unit] = P("concat")
  def str[_:P]:P[Unit] = P("str")
  def strafter[_:P]:P[Unit] = P("strafter")
  def strstarts[_:P]:P[Unit] = P("strstarts")

  def uriParen[_:P]:P[URI] = P("(" ~ uri ~ stringPatterns ~ ")").map{ s => URI(s)}
  def concatParen[_:P]:P[CONCAT] = ("(" ~ concat ~ stringPatterns ~ stringPatterns ~ ")").map{
    c => CONCAT(c._1, c._2)
  }
  def strParen[_:P]:P[STR] = P("(" ~ str ~ stringPatterns ~ ")").map{ s => STR(s)}
  def strafterParen[_:P]:P[STRAFTER] = P("(" ~ strafter ~ stringPatterns ~ stringPatterns ~ ")").map{
    s => STRAFTER(s._1, s._2)
  }

  def strstartsParen[_:P]:P[STRSTARTS] = P("(" ~ strstarts ~ stringPatterns ~ stringPatterns ~ ")").map{
    s => STRSTARTS(s._1, s._2)
  }

  def stringPatterns[_:P]:P[StringLike] =
    P(uriParen
      | concatParen
      | strParen
      | strafterParen
      | strstartsParen
      | StringValParser.string
      | StringValParser.variable)

  def parser[_:P]:P[StringLike] = P(stringPatterns)
}
