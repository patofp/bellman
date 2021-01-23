package com.gsk.kg.sparqlparser

import com.gsk.kg.sparqlparser.StringVal._
import com.gsk.kg.sparqlparser.Expr._
import fastparse.Parsed.{Failure, Success}
import org.apache.jena.graph.Node
import org.apache.jena.query.QueryFactory
import org.apache.jena.sparql.algebra.Algebra
import org.apache.jena.sparql.core.Quad

import scala.jdk.CollectionConverters.CollectionHasAsScala

object QueryConstruct {

  case class SparqlParsingError(s: String) extends Exception(s)

  def parseADT(sparql: String): Expr = {
    println("TESTING SCALASTYLE FAIL")
    val query = QueryFactory.create(sparql)
    val algebra = fastparse.parse(Algebra.compile(query).toString(), ExprParser.parser(_)) match {
      case Success(value, index) => value
      case Failure(str, i, extra) =>
        throw SparqlParsingError(s"$str at position $i, $extra")
    }
    if (query.isConstructType) {
      val template = query.getConstructTemplate
      val vars = query.getProjectVars.asScala.map(v => VARIABLE(v.toString())).toSeq
      val bgp = toBGP(template.getQuads.asScala.toSeq)
      Construct(vars, bgp, algebra)
    } else if (query.isSelectType) {
      val vars = query.getProjectVars.asScala.map(v => VARIABLE(v.getVarName)).toSeq
      Select(vars, algebra)
    } else {
      throw SparqlParsingError(s"The query type: ${query.queryType()} is not supported yet")
    }
  }

  def getAllVariableNames(bgp: BGP): Set[String] = {
    bgp.triples.foldLeft(Set.empty[String]) {
      (acc, t) =>
        acc ++ Set(t.s, t.p, t.o).flatMap { e =>
          e match {
            case VARIABLE(v) => Some(v)
            case _ => None
          }
        }
    }
  }

  def toBGP(quads: Iterable[Quad]): BGP = {
    BGP(quads.map(toTriple(_)).toSeq)
  }

  def toTriple(quad: Quad): Triple = {
    def toStringVal(n: Node): StringVal = {
      if (n.isLiteral) {
        STRING(n.toString())
      } else if (n.isURI) {
        URIVAL(s"<${n.toString()}>")
      } else if (n.isVariable) {
        VARIABLE(n.toString())
      } else if (n.isBlank) {
        BLANK(n.toString())
      } else {
        throw new SparqlParsingError(s"${quad} cannot convert to ADT triple")
      }
    }

    val triple = quad.asTriple()
    Triple(
      toStringVal(triple.getSubject),
      toStringVal(triple.getPredicate),
      toStringVal(triple.getObject)
    )
  }

}
