package com.gsk.kg.sparqlparser

import com.gsk.kg.sparqlparser.FilterFunction._
import fastparse.MultiLineWhitespace._
import fastparse._

object FilterFunctionParser {
  def equals[_:P]:P[Unit] = P("=")
  def regex[_:P]:P[Unit] = P("regex")
  def strstarts[_:P]:P[Unit] = P("strstarts")

  def equalsParen[_:P]:P[EQUALS] =
    P("(" ~ equals ~ (StringFuncParser.parser | StringValParser.tripleValParser) ~
      (StringFuncParser.parser | StringValParser.tripleValParser) ~ ")").map(
     f => EQUALS(f._1, f._2)
    )

  def regexParen[_:P]:P[REGEX] =
    P("(" ~ regex ~ (StringFuncParser.parser | StringValParser.tripleValParser) ~
      (StringFuncParser.parser | StringValParser.tripleValParser) ~ ")").map(
      f => REGEX(f._1, f._2)
    )

  def strstartsParen[_:P]:P[STRSTARTS] =
    P("(" ~ strstarts ~ (StringFuncParser.parser | StringValParser.tripleValParser) ~
      (StringFuncParser.parser | StringValParser.tripleValParser) ~ ")").map(
      f => STRSTARTS(f._1, f._2)
    )
  def parser[_:P]:P[FilterFunction] =
    P(equalsParen
    | regexParen
    | strstartsParen)
}
