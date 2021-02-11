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
      case BGPF(triples)                => StateT.get[Result, DataFrame]
      case TripleF(s, p, o)             => StateT.get[Result, DataFrame]
      case LeftJoinF(l, r)              => StateT.get[Result, DataFrame]
      case FilteredLeftJoinF(l, r, f)   => StateT.get[Result, DataFrame]
      case UnionF(l, r)                 => StateT.get[Result, DataFrame]
      case ExtendF(bindTo, bindFrom, r) => StateT.get[Result, DataFrame]
      case FilterF(funcs, expr)         => StateT.get[Result, DataFrame]
      case JoinF(l, r)                  => StateT.get[Result, DataFrame]
      case GraphF(g, e)                 => StateT.get[Result, DataFrame]
      case ConstructF(vars, bgp, r)     => StateT.get[Result, DataFrame]
      case SelectF(vars, r)             => StateT.get[Result, DataFrame]
    }

  def evaluate(dataframe: DataFrame, query: Expr): Result[DataFrame] = {
    val eval = scheme.cataM[M, ExprF, Expr, DataFrame](evaluateAlgebraM)

    eval(query).runA(dataframe)
  }

}
