package com.gsk.kg.engine

import cats.data.StateT
import cats.instances.all._
import cats.syntax.either._
import cats.syntax.applicative._

import org.apache.spark.sql.DataFrame

import com.gsk.kg.sparqlparser.Expr
import com.gsk.kg.sparqlparser.Expr.fixedpoint._

import higherkindness.droste._
import cats.data.IndexedStateT
import org.apache.spark.sql.SQLContext
import com.gsk.kg.sparqlparser.StringVal
import com.gsk.kg.engine.Multiset._
import cats.Foldable

object Engine {

  sealed trait EngineError

  object EngineError {
    case class General(description: String) extends EngineError
  }

  type Result[A] = Either[EngineError, A]
  val Result = Either
  type M[A] = StateT[Result, DataFrame, A]

  def evaluateAlgebraM(implicit sc: SQLContext): AlgebraM[M, ExprF, Multiset] =
    AlgebraM[M, ExprF, Multiset] {
      case BGPF(triples) =>
        import sc.implicits._
        StateT.get[Result, DataFrame].map { df: DataFrame =>
          Foldable[List].fold(
            triples.toList.map({ triple =>
              val current = df
              triple.getPredicates.foreach {
                case (value, "s") =>
                  current.filter(r => r(0) == value.s).drop("s")
                case (value, "p") =>
                  current.filter(r => r(1) == value.s).drop("p")
                case (value, "o") =>
                  current.filter(r => r(2) == value.s).drop("o")
                case _ => current
              }
              val variables = triple.getVariables
              val selected =
                current.select(variables.map(v => $"${v._2}".as(v._1.s)): _*)

              Multiset(
                variables.map(_._1.asInstanceOf[StringVal.VARIABLE]).toSet,
                selected
              )
            })
          )

        }
      case TripleF(s, p, o) =>
        StateT.get[Result, DataFrame].map(df => Multiset(Set.empty, df))
      case LeftJoinF(l, r) =>
        StateT.get[Result, DataFrame].map(df => Multiset(Set.empty, df))
      case FilteredLeftJoinF(l, r, f) =>
        StateT.get[Result, DataFrame].map(df => Multiset(Set.empty, df))
      case UnionF(l, r) =>
        StateT.get[Result, DataFrame].map(df => Multiset(Set.empty, df))
      case ExtendF(bindTo, bindFrom, r) =>
        StateT.get[Result, DataFrame].map(df => Multiset(Set.empty, df))
      case FilterF(funcs, expr) =>
        StateT.get[Result, DataFrame].map(df => Multiset(Set.empty, df))
      case JoinF(l, r) =>
        StateT.get[Result, DataFrame].map(df => Multiset(Set.empty, df))
      case GraphF(g, e) =>
        StateT.get[Result, DataFrame].map(df => Multiset(Set.empty, df))
      case ConstructF(vars, bgp, r) =>
        StateT.get[Result, DataFrame].map(df => Multiset(Set.empty, df))
      case SelectF(vars, r) =>
        StateT.get[Result, DataFrame].map(df => Multiset(Set.empty, df))
    }

  def evaluate(
      dataframe: DataFrame,
      query: Expr
  )(
    implicit sc: SQLContext
  ): Result[DataFrame] = {
    val eval =
      scheme.cataM[M, ExprF, Expr, Multiset](evaluateAlgebraM)

    eval(query).runA(dataframe).map(_.dataframe)
  }

}
