import ExprParser.graphPattern
import fastparse._, MultiLineWhitespace._

object StringFuncParser {
  /*
  Functions on strings: https://www.w3.org/TR/sparql11-query/#func-strings
   */
  def uri[_:P]:P[Unit] = P("uri")
  def concat[_:P]:P[Unit] = P("concat")

  def uriParen[_:P]:P[URI] = P("(" ~ uri ~ stringPatterns ~ ")").map{ s => URI(s)}
  def concatParen[_:P]:P[CONCAT] = ("(" ~ concat ~ stringPatterns ~ stringPatterns).map{
    c => CONCAT(c._1, c._2)
  }

  def stringPatterns[_:P]:P[StringLike] = P(uriParen | concatParen | StringValParser.string | StringValParser.variable)
  def parser[_:P]:P[StringLike] = P(stringPatterns)
}
