import ExprParser.graphPattern
import fastparse._, MultiLineWhitespace._

object StringFuncParser {
  def uri[_:P]:P[Unit] = P("uri")
  def concat[_:P]:P[Unit] = P("concat")

  //TODO make these paren methods recursive to allow for arbitrary depth
  def uriParen[_:P]:P[URI] = P("(" ~ uri ~ CharsWhile(_ != ')').! ~ ")").map{URI}
  def concatParen[_:P]:P[CONCAT] = ("(" ~ concat ~ CharsWhile(_ != ' ').! ~ CharsWhile(_ != ')').!).map{
    c => CONCAT(c._1, c._2)
  }

  def stringPatterns[_:P]:P[StringFunc] = P(uriParen | concatParen)
  def parser[_:P]:P[StringFunc] = P(stringPatterns)
}
