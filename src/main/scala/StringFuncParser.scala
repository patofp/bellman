import ExprParser.graphPattern
import fastparse._, MultiLineWhitespace._

object StringFuncParser {
  def uri[_:P]:P[Unit] = P("uri")
  def concat[_:P]:P[Unit] = P("concat")

  //TODO make these paren methods recursive to allow for arbitrary depth
  def string[_:P]:P[STRING] = P("\"" ~ CharsWhile(_ != '\"').! ~ "\"").map{STRING}
  def variable[_:P]:P[VARIABLE] = P("?" ~ (CharsWhile(_ != ')') | CharsWhile(_ != ' '))).!.map{VARIABLE}

  def uriParen[_:P]:P[URI] = P("(" ~ uri ~ stringPatterns ~ ")").map{ s => URI(s)}
  def concatParen[_:P]:P[CONCAT] = ("(" ~ concat ~ stringPatterns ~ stringPatterns).map{
    c => CONCAT(c._1, c._2)
  }

  def stringPatterns[_:P]:P[StringFunc] = P(uriParen | concatParen | string | variable)
  def parser[_:P]:P[StringFunc] = P(stringPatterns)
}
