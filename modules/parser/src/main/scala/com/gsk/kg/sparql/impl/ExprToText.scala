package com.gsk.kg.sparql.impl

import com.gsk.kg.sparqlparser.Conditional._
import com.gsk.kg.sparqlparser.BuildInFunc._
import com.gsk.kg.sparqlparser.StringVal._
import com.gsk.kg.sparqlparser.{Expression, Conditional, BuildInFunc, StringLike, StringVal}

object ExprToText {

  implicit class StringLikeToText(st: StringLike) {
    def text: String = {
      st match {
        case sv: StringVal =>
          sv match {
            case STRING(s,tag) =>
              "\"" + s + "\"" + {
                tag match {
                  case Some(t) if t.startsWith("@") => t
                  case Some(t) => s"^^${t}"
                  case None => ""
                }
              }
            case NUM(s) => s
            case VARIABLE(s) => s
            case URIVAL(s) => s
            case BLANK(s) => s
            case BOOL(s) => s
          }
        case sf: BuildInFunc =>
          sf match {
            case STR(s) => s"str(${s.text})"
            case URI(s) => s"uri(${s.text})"
            case STRAFTER(s, f) => s"strafter(${s.text}, ${f.text})"
            case CONCAT(appendTo, append) => s"concat(${appendTo.text}, ${append.text})"
            case ISBLANK(s) => s"isBlank(s)"
            case REPLACE(st, pattern, by) => s"replace(${st.text}, ${pattern.text}, ${by.text})"
            case REGEX(l, r) => s"regex(${l.text}, ${r.text})"
            case STRSTARTS(l, r) => s"strstarts(${l.text}, ${r.text})"
          }
      }
    }
  }

  implicit class ConditionalToText(st: Conditional) {
    def text: String = {
      st match {
        case EQUALS(l, r) => s"${l.text} = ${r.text}"
        case GT(l, r) => s"${l.text} > ${r.text}"
        case LT(l, r) => s"${l.text} < ${r.text}"
        case OR(l, r) => s"${l.text} || ${r.text}"
        case AND(l, r) => s"${l.text} && ${r.text}"
        case NEGATE(s) => s"! ${s}"
      }
    }
  }

  implicit class ExpressionToText(e: Expression) {
    def text: String = {
      e match {
        case st: StringLike => st.text
        case ff: Conditional => ff.text
      }
    }
  }

}
