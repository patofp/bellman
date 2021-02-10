package com.gsk.kg.sparql.impl

import com.gsk.kg.sparqlparser.FilterFunction._
import com.gsk.kg.sparqlparser.StringFunc.{CONCAT, ISBLANK, STR, STRAFTER, URI}
import com.gsk.kg.sparqlparser.StringVal._
import com.gsk.kg.sparqlparser.{FilterFunction, StringFunc, StringLike, StringVal}

object ExprToText {

  implicit class StringLikeToText(st: StringLike) {
    def text: String = {
      st match {
        case sv: StringVal =>
          sv match {
            case STRING(s) => "\"" + s + "\""
            case NUM(s) => s
            case VARIABLE(s) => s
            case URIVAL(s) => s
            case BLANK(s) => s
          }
        case sf: StringFunc =>
          sf match {
            case STR(s) => s"str(${s.text})"
            case URI(s) => s"uri(${s.text})"
            case STRAFTER(s, f) => s"strafter(${s.text}, ${f.text})"
            case CONCAT(appendTo, append) => s"concat(${appendTo.text}, ${append.text})"
            case ISBLANK(s) => s"isBlank(s)"
          }
      }
    }
  }

  implicit class FilterFunctionToText(st: FilterFunction) {
    def text: String = {
      st match {
        case REGEX(l, r) => s"regex(${l.text}, ${r.text})"
        case EQUALS(l, r) => s"${l.text} = ${r.text}"
        case GT(l, r) => s"${l.text} > ${r.text}"
        case LT(l, r) => s"${l.text} < ${r.text}"
        case STRSTARTS(l, r) => s"strstarts(${l.text}, ${r.text})"
        case NEGATE(s) => s"! ${s}"
        case STRFUN(f) => f.text
      }
    }
  }

}
