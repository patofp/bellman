package com.gsk.kg.sparqlparser

import com.gsk.kg.sparqlparser.BuildInFunc._
import fastparse.MultiLineWhitespace._
import fastparse._

object BuildInFuncParser {
  /*
  Functions on strings: https://www.w3.org/TR/sparql11-query/#func-strings
   */
  def uri[_:P]:P[Unit] = P("uri")
  def concat[_:P]:P[Unit] = P("concat")
  def str[_:P]:P[Unit] = P("str")
  def strafter[_:P]:P[Unit] = P("strafter")
  def isBlank[_:P]:P[Unit] = P("isBlank")
  def replace[_:P]:P[Unit] = P("replace")
  def regex[_:P]:P[Unit] = P("regex")
  def strstarts[_:P]:P[Unit] = P("strstarts")

  def uriParen[_:P]:P[URI] = P("(" ~ uri ~ ExpressionParser.parser ~ ")").map{ s => URI(s)}
  def concatParen[_:P]:P[CONCAT] = ("(" ~ concat ~ ExpressionParser.parser ~ ExpressionParser.parser ~ ")").map{
    c => CONCAT(c._1, c._2)
  }
  def strParen[_:P]:P[STR] = P("(" ~ str ~ ExpressionParser.parser ~ ")").map{ s => STR(s)}
  def strafterParen[_:P]:P[STRAFTER] = P("(" ~ strafter ~ ExpressionParser.parser ~ ExpressionParser.parser ~ ")").map{
    s => STRAFTER(s._1, s._2)
  }

  def isBlankParen[_:P]:P[ISBLANK] = P("(" ~ isBlank ~ ExpressionParser.parser ~ ")").map(ISBLANK(_))

  def replaceParen[_:P]:P[REPLACE] = P("(" ~ replace ~ ExpressionParser.parser ~ ExpressionParser.parser ~ ExpressionParser.parser ~")").map{
    s => REPLACE(s._1, s._2, s._3)
  }

  def regexParen[_:P]:P[REGEX] =
    P("(" ~ regex ~ ExpressionParser.parser ~ ExpressionParser.parser ~ ")").map(
      f => REGEX(f._1, f._2)
    )

  def strstartsParen[_:P]:P[STRSTARTS] =
    P("(" ~ strstarts ~ ExpressionParser.parser ~ ExpressionParser.parser ~ ")").map(
      f => STRSTARTS(f._1, f._2)
    )


  def funcPatterns[_:P]:P[StringLike] =
    P(uriParen
      | concatParen
      | strParen
      | strafterParen
      | strstartsParen
      | isBlankParen
      | replaceParen
      | regexParen)
//      | StringValParser.string
//      | StringValParser.variable)

  def parser[_:P]:P[StringLike] = P(funcPatterns)
}
