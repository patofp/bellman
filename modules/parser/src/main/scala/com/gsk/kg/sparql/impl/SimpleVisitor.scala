package com.gsk.kg.sparql.impl

import com.gsk.kg.sparql.{Visitor, Visitors}
import com.gsk.kg.sparqlparser._
import ExprToText._
import com.gsk.kg.sparqlparser.StringVal.VARIABLE

class SimpleVisitor extends Visitor[String] {
  override def visitTriple(triple: Expr.Triple): String = {
    s"${triple.s.text} ${triple.p.text} ${triple.o.text} ."
  }

  override def visitBGP(triples: Seq[String]): String = {
    s"${triples.mkString("\n")}\n"
  }

  override def visitLeftJoin(left: String, right: String): String = { //optional
    s"${left}OPTIONAL{$right}\n"
  }

  override def visitFilteredLeftJoinVisitor(left: String, right: String, f: Seq[Expression]): String = { //when optional follow with filter
    val exp = f.map(e =>s"FILTER(${e.text})").mkString("\n")
    s"${left}OPTIONAL{${right}${exp}}\n"
  }

  override def visitUnion(left: String, right: String): String = { //union
    s"{$left}\nUnion {$right}\n"
  }

  override def visitExtend(to: VARIABLE, from: Expression, d: String): String = { //bind
    s"${d}BIND(${from.text} as ${to.text}) .\n"
  }

  override def visitFilter(funcs: Seq[Expression], d: String): String = { //filter
    val fs = funcs.map(f => s"FILTER(${f.text})").mkString("\n")
    s"${d}${fs}\n"
  }

  override def visitJoin(l: String, r: String): String = { //join
    s"${l}${r}"
  }

  override def visitGraph(g: StringLike, d: String): String = { //graph
    s"GRAPH ${g.text} {\n${d}}\n"
  }

  def toConstruct(vars: Seq[StringVal.VARIABLE], bgp: Expr.BGP, d: String): String = { //construct
    val toCons = this.visitBGP(bgp.triples.map(this.visitTriple(_)))
    s"CONSTRUCT {\n$toCons} WHERE {\n${d}\n}\n"
  }

  override def visitSelect(vars: Seq[StringVal.VARIABLE], d: String): String = {
    s"SELECT ${vars.map(_.s).mkString(" ")} WHERE {\n${d}\n}\n"
  }

  override def visitOffsetLimit(off: Option[Long], lmt: Option[Long], d: String): String = {
    val o = if (!off.isEmpty) s"offset ${off.get}" else ""
    val l = if (!lmt.isEmpty) s"limit ${lmt.get}" else ""
    s"$o $l\n"
  }

  override def visitDistinct(e: String): String = s"DISTINCT ${e} "

  override def visitOpNil: String = ""

  override def visitTabUnit: String = ""
}

object SimpleVisitor {

  def apply(): SimpleVisitor = {
    new SimpleVisitor
  }

}
