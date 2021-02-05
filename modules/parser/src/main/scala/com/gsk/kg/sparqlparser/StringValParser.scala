package com.gsk.kg.sparqlparser

import com.gsk.kg.sparqlparser.StringVal._
import fastparse.MultiLineWhitespace._
import fastparse._

object StringValParser {
  def string[_:P]:P[STRING] = P("\"" ~ CharsWhile(_ != '\"').! ~ "\"").map{STRING}
  //TODO improve regex. Include all valid sparql varnames in spec: https://www.w3.org/TR/sparql11-query/#rPN_CHARS_BASE
  def variable[_:P]:P[VARIABLE] = P("?" ~ CharsWhileIn("a-zA-Z0-9_")).!.map{VARIABLE}
  def urival[_:P]:P[URIVAL] = P("<" ~ (CharsWhile(_ != '>')) ~ ">").!.map{URIVAL}
  def num[_:P]:P[NUM] = P(CharsWhileIn("0-9")).!.map{NUM}

  def tripleValParser[_:P]:P[StringVal] = P(variable | urival | string | num)
}
