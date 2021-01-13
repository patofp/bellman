import ExprParser.leftJoinParen
import fastparse.P

object FilterFunctionParser {
  def equals[_:P]:P[EQUALS] = P("=").map(s => EQUALS())
  def regex[_:P]:P[REGEX] = P("regex").map(s => REGEX())

  def parse[_:P]:P[FilterFunction] =
    P(equals
    | regex)
}
