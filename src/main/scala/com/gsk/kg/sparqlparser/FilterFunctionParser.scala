package com.gsk.kg.sparqlparser

import com.gsk.kg.sparqlparser.FilterFunction._
import fastparse.MultiLineWhitespace._
import fastparse._

object FilterFunctionParser {
  def equals[_:P]:P[Unit] = P("=")
  def regex[_:P]:P[Unit] = P("regex")
  def strstarts[_:P]:P[Unit] = P("strstarts")
  def gt[_:P]:P[Unit] = P(">")
  def lt[_:P]:P[Unit] = P("<")

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

  def gtParen[_:P]:P[GT] =
    P("(" ~ gt ~ (StringFuncParser.parser | StringValParser.tripleValParser) ~
      (StringFuncParser.parser | StringValParser.tripleValParser) ~ ")").map(
      f => GT(f._1, f._2)
    )

  def ltParen[_:P]:P[LT] =
    P("(" ~ lt ~ (StringFuncParser.parser | StringValParser.tripleValParser) ~
      (StringFuncParser.parser | StringValParser.tripleValParser) ~ ")").map(
      f => LT(f._1, f._2)
    )
  def parser[_:P]:P[FilterFunction] =
    P(equalsParen
    | regexParen
    | strstartsParen
    | gtParen
    | ltParen)
}
