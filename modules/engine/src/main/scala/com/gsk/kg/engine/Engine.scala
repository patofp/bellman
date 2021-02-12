package com.gsk.kg.engine

import cats.data.StateT
import cats.instances.either._

import org.apache.spark.sql.DataFrame

import com.gsk.kg.sparqlparser.Expr
import com.gsk.kg.sparqlparser.Expr.fixedpoint._

import higherkindness.droste._
import cats.data.IndexedStateT

object Engine {

  sealed trait EngineError

  object EngineError {
    case class General(description: String) extends EngineError
  }

  type Result[A] = Either[EngineError, A]
  type M[A] = StateT[Result, DataFrame, A]

  val evaluateAlgebraM: AlgebraM[M, ExprF, DataFrame] =
    AlgebraM[M, ExprF, DataFrame] {
      case BGPF(triples)                => StateT.get
      case TripleF(s, p, o)             => StateT.get
      case LeftJoinF(l, r)              => StateT.get
      case FilteredLeftJoinF(l, r, f)   => StateT.get
      case UnionF(l, r)                 => StateT.get
      case ExtendF(bindTo, bindFrom, r) => StateT.get
      case FilterF(funcs, expr)         => StateT.get
      case JoinF(l, r)                  => StateT.get
      case GraphF(g, e)                 => StateT.get
      case ConstructF(vars, bgp, r)     => StateT.get
      case SelectF(vars, r)             => StateT.get
    }

  def evaluate(dataframe: DataFrame, query: Expr): Result[DataFrame] = {
    val eval = scheme.cataM[M, ExprF, Expr, DataFrame](evaluateAlgebraM)

    eval(query).runA(dataframe)
  }

}
