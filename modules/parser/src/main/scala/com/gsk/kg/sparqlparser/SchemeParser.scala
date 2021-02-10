package com.gsk.kg.sparqlparser

import cats.implicits._
import Expr.fixedpoint._
import higherkindness.droste.Coalgebra
import org.apache.jena.graph.Node
import org.apache.jena.sparql.algebra.Op
import org.apache.jena.sparql.algebra.op._
import scala.collection.JavaConverters._
import com.gsk.kg.sparqlparser.QueryConstruct.SparqlParsingError
import com.gsk.kg.sparqlparser.StringVal._
import fastparse.Parsed
import higherkindness.droste.CoalgebraM
import cats.data.Reader
import org.apache.jena.query.Query
import org.apache.jena.query.QueryFactory
import higherkindness.droste.scheme
import org.apache.jena.sparql.algebra.Algebra
import higherkindness.droste.Basis
import org.apache.jena.sparql.core.VarExprList

object SchemeParser {

  val parseCoalgebra: Coalgebra[ExprF, Op] =
    Coalgebra[ExprF, Op] {
      case op: OpAssign => throw new RuntimeException("OpAssign not implemented")
      case op: OpBGP =>
        BGPF(op.getPattern().getList().asScala.map(toTriple))
      case op: OpDiff            => throw new RuntimeException("OpDiff not implemented")
      case op: OpDisjunction     => throw new RuntimeException("OpDisjunction not implemented")
      case op: OpDistinct        => throw new RuntimeException("OpDistinct not implemented")
      case op: OpDistinctReduced => throw new RuntimeException("OpDistinctReduced not implemented")
      case op: OpExt             => throw new RuntimeException("OpExt not implemented")
      case op: OpExtend          =>
        val (to, from) = op.getVarExprList().getExprs().asScala.toList.get(0).get
        ExtendF(
          toStringVal(to),
          toStringVal(from.asVar()),
          op.getSubOp())
      case op: OpExtendAssign    => throw new RuntimeException("OpExtendAssign not implemented")
      case op: OpFilter =>
        FilterF(
          op.getExprs().getList().asScala.map(toFilterFunction),
          op.getSubOp()
        )
      case op: OpFind => throw new RuntimeException("OpFind not implemented")
      case op: OpGraph =>
        GraphF(toStringVal(op.getNode()), op.getSubOp())
      case op: OpGroup => throw new RuntimeException("OpGroup not implemented")
      case op: OpJoin =>
        JoinF(op.getLeft(), op.getRight())
      case op: OpLabel => throw new RuntimeException("OpLabel not implemented")
      case op: OpLeftJoin =>
        LeftJoinF(op.getLeft(), op.getRight())
      case op: OpList        => throw new RuntimeException("OpList not implemented")
      case op: OpMinus       => throw new RuntimeException("OpMinus not implemented")
      case op: OpN           => throw new RuntimeException("OpN not implemented")
      case op: OpNull        => throw new RuntimeException("OpNull not implemented")
      case op: OpOrder       => throw new RuntimeException("OpOrder not implemented")
      case op: OpPath        => throw new RuntimeException("OpPath not implemented")
      case op: OpProcedure   => throw new RuntimeException("OpProcedure not implemented")
      case op: OpProject     =>
        SelectF(
          op.getVars().asScala.map(v => VARIABLE(v.toString())),
          op.getSubOp()
        )
      case op: OpPropFunc    => throw new RuntimeException("OpPropFunc not implemented")
      case op: OpQuad        => throw new RuntimeException("OpQuad not implemented")
      case op: OpQuadBlock   => throw new RuntimeException("OpQuadBlock not implemented")
      case op: OpQuadPattern => throw new RuntimeException("OpQuadPattern not implemented")
      case op: OpReduced     => throw new RuntimeException("OpReduced not implemented")
      case op: OpSequence    => throw new RuntimeException("OpSequence not implemented")
      case op: OpService     => throw new RuntimeException("OpService not implemented")
      case op: OpSlice       => throw new RuntimeException("OpSlice not implemented")
      case op: OpTable       => throw new RuntimeException("OpTable not implemented")
      case op: OpTopN        => throw new RuntimeException("OpTopN not implemented")
      case op: OpTriple =>
        toTripleF(op.getTriple())
      case op: OpUnion =>
        UnionF(op.getLeft(), op.getRight())
    }

  def parse: String => Expr = { str =>
    val query = QueryFactory.create(str)
    val op = Algebra.compile(query)
    val go = scheme.ana(parseCoalgebra)

    go(op)
  }

  private def toFilterFunction(
      expr: org.apache.jena.sparql.expr.Expr
  ): FilterFunction = {
    fastparse.parse(expr.toString(), FilterFunctionParser.parser(_)) match {
      case Parsed.Success(value, index) => value
      case Parsed.Failure(str, i, extra) =>
        throw SparqlParsingError(s"$str at position $i, ${extra.input}")
      case _ => //Failure()
        throw SparqlParsingError(s"parsing failure.")
    }
  }

  private def toStringVal(n: Node): StringVal = {
    if (n.isLiteral) {
      STRING(n.toString())
    } else if (n.isURI) {
      URIVAL(s"<${n.toString()}>")
    } else if (n.isVariable) {
      VARIABLE(n.toString())
    } else if (n.isBlank) {
      BLANK(n.toString())
    } else {
      throw new SparqlParsingError(s"$n cannot convert to ADT triple")
    }
  }

  private def toTriple(triple: org.apache.jena.graph.Triple): Expr.Triple =
    Expr.Triple(
      toStringVal(triple.getSubject()),
      toStringVal(triple.getPredicate()),
      toStringVal(triple.getObject())
    )

  private def toTripleF(triple: org.apache.jena.graph.Triple): ExprF[Op] =
    TripleF(
      toStringVal(triple.getSubject()),
      toStringVal(triple.getPredicate()),
      toStringVal(triple.getObject())
    )

}
