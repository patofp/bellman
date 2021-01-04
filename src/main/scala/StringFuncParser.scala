import ExprParser.graphPattern
import fastparse._, MultiLineWhitespace._

object StringFuncParser {
  def uri[_:P]:P[Unit] = P("uri")

  def uriParen[_:P]:P[URI] = P("(" ~ uri ~ CharsWhile(_ != ')').! ~ ")").map{URI}

  def stringPatterns[_:P]:P[StringFunc] = P(uriParen)
  def parser[_:P]:P[StringFunc] = P(stringPatterns)
}
