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
  def and[_:P]:P[Unit] = P("&&")
  def or[_:P]:P[Unit] = P("||")
  def negate[_:P]:P[Unit] = P("!")

  def equalsParen[_:P]:P[EQUALS] =
    P("(" ~ equals ~ (FilterFunctionParser.parser | StringFuncParser.parser | StringValParser.tripleValParser) ~
      (FilterFunctionParser.parser | StringFuncParser.parser | StringValParser.tripleValParser) ~ ")").map(
     f => EQUALS(f._1, f._2)
    )

  def regexParen[_:P]:P[REGEX] =
    P("(" ~ regex ~ (FilterFunctionParser.parser | StringFuncParser.parser | StringValParser.tripleValParser) ~
      (FilterFunctionParser.parser | StringFuncParser.parser | StringValParser.tripleValParser) ~ ")").map(
      f => REGEX(f._1, f._2)
    )

  def strstartsParen[_:P]:P[STRSTARTS] =
    P("(" ~ strstarts ~ (FilterFunctionParser.parser | StringFuncParser.parser | StringValParser.tripleValParser) ~
      (FilterFunctionParser.parser | StringFuncParser.parser | StringValParser.tripleValParser) ~ ")").map(
      f => STRSTARTS(f._1, f._2)
    )

  def gtParen[_:P]:P[GT] =
    P("(" ~ gt ~ (FilterFunctionParser.parser | StringFuncParser.parser | StringValParser.tripleValParser) ~
      (FilterFunctionParser.parser | StringFuncParser.parser | StringValParser.tripleValParser) ~ ")").map(
      f => GT(f._1, f._2)
    )

  def ltParen[_:P]:P[LT] =
    P("(" ~ lt ~ (FilterFunctionParser.parser | StringFuncParser.parser | StringValParser.tripleValParser) ~
      (FilterFunctionParser.parser | StringFuncParser.parser | StringValParser.tripleValParser) ~ ")").map(
      f => LT(f._1, f._2)
    )

  def andParen[_:P]:P[AND] =
    P("(" ~ and ~ (FilterFunctionParser.parser | StringFuncParser.parser | StringValParser.tripleValParser) ~
      (FilterFunctionParser.parser | StringFuncParser.parser | StringValParser.tripleValParser) ~ ")").map(
      f => AND(f._1, f._2)
    )
  def orParen[_:P]:P[OR] =
    P("(" ~ or ~ (FilterFunctionParser.parser | StringFuncParser.parser | StringValParser.tripleValParser) ~
      (FilterFunctionParser.parser | StringFuncParser.parser | StringValParser.tripleValParser) ~ ")").map(
      f => OR(f._1, f._2)
    )

  def negateParen[_:P]:P[NEGATE] =
    P("(" ~ negate ~(FilterFunctionParser.parser | StringFuncParser.parser | StringValParser.tripleValParser) ~ ")").map(NEGATE(_))

  def parser[_:P]:P[FilterFunction] =
    P(equalsParen
    | regexParen
    | strstartsParen
    | gtParen
    | ltParen
    | andParen
    | orParen
    | negateParen)
}
