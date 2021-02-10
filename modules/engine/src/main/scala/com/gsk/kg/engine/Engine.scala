package com.gsk.kg.engine

import cats.data.State

import org.apache.spark.sql.DataFrame

import com.gsk.kg.sparqlparser.Expr
import com.gsk.kg.sparqlparser.Expr.fixedpoint._

import higherkindness.droste.AlgebraM
import higherkindness.droste.scheme

object Engine {

  val evaluateAlgebraM: AlgebraM[State[DataFrame, *], ExprF, DataFrame] =
    AlgebraM[State[DataFrame, *], ExprF, DataFrame] {
      case BGPF(triples)                => State.get
      case TripleF(s, p, o)             => State.get
      case LeftJoinF(l, r)              => State.get
      case FilteredLeftJoinF(l, r, f)   => State.get
      case UnionF(l, r)                 => State.get
      case ExtendF(bindTo, bindFrom, r) => State.get
      case FilterF(funcs, expr)         => State.get
      case JoinF(l, r)                  => State.get
      case GraphF(g, e)                 => State.get
      case ConstructF(vars, bgp, r)     => State.get
      case SelectF(vars, r)             => State.get
    }

  def evaluate(dataframe: DataFrame, query: Expr): DataFrame = {
    val eval = scheme.cataM[State[DataFrame, *], ExprF, Expr, DataFrame](evaluateAlgebraM)

    eval(query).runA(dataframe).value
  }

}
