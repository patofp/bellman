package com.gsk.kg.sparqlparser

import com.gsk.kg.sparqlparser.StringFunc._
import fastparse.MultiLineWhitespace._
import fastparse._

object StringFuncParser {
  /*
  Functions on strings: https://www.w3.org/TR/sparql11-query/#func-strings
   */
  def uri[_:P]:P[Unit] = P("uri")
  def concat[_:P]:P[Unit] = P("concat")
  def str[_:P]:P[Unit] = P("str")
  def strafter[_:P]:P[Unit] = P("strafter")

  def uriParen[_:P]:P[URI] = P("(" ~ uri ~ stringPatterns ~ ")").map{ s => URI(s)}
  def concatParen[_:P]:P[CONCAT] = ("(" ~ concat ~ stringPatterns ~ stringPatterns ~ ")").map{
    c => CONCAT(c._1, c._2)
  }
  def strParen[_:P]:P[STR] = P("(" ~ str ~ stringPatterns ~ ")").map{ s => STR(s)}
  def strafterParen[_:P]:P[STRAFTER] = P("(" ~ strafter ~ stringPatterns ~ stringPatterns ~ ")").map{
    s => STRAFTER(s._1, s._2)
  }

  def stringPatterns[_:P]:P[StringLike] =
    P(uriParen
      | concatParen
      | strParen
      | strafterParen
      | StringValParser.string
      | StringValParser.variable)

  def parser[_:P]:P[StringLike] = P(stringPatterns)
}
