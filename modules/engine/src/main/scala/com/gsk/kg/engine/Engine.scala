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
import com.gsk.kg.engine.Predicate.None
import com.gsk.kg.sparqlparser.Query
import com.gsk.kg.sparqlparser.Query.Construct

object Engine {

  type Result[A] = Either[EngineError, A]
  val Result = Either
  type M[A] = StateT[Result, DataFrame, A]

  def evaluateAlgebraM(implicit sc: SQLContext): AlgebraM[M, ExprF, Multiset] =
    AlgebraM[M, ExprF, Multiset] {
      case BGPF(triples) => evaluateBGPF(triples)
      case TripleF(s, p, o) =>
        StateT.get[Result, DataFrame].map(df => Multiset(Set.empty, df))
      case LeftJoinF(l, r) =>
        StateT.get[Result, DataFrame].map(df => Multiset(Set.empty, df))
      case FilteredLeftJoinF(l, r, f) =>
        StateT.get[Result, DataFrame].map(df => Multiset(Set.empty, df))
      case UnionF(l, r) =>
        l.union(r).pure[M]
      case ExtendF(bindTo, bindFrom, r) =>
        StateT.get[Result, DataFrame].map(df => Multiset(Set.empty, df))
      case FilterF(funcs, expr) =>
        StateT.get[Result, DataFrame].map(df => Multiset(Set.empty, df))
      case JoinF(l, r) =>
        StateT.get[Result, DataFrame].map(df => Multiset(Set.empty, df))
      case GraphF(g, e) =>
        StateT.get[Result, DataFrame].map(df => Multiset(Set.empty, df))
      case DistinctF(r) =>
        StateT.get[Result, DataFrame].map(df => Multiset(Set.empty, df))
      case OffsetLimitF(offset, limit, r) =>
        StateT.get[Result, DataFrame].map(df => Multiset(Set.empty, df))
      case OpNilF() =>
        StateT.get[Result, DataFrame].map(df => Multiset(Set.empty, df))
      case TabUnitF() =>
        StateT.get[Result, DataFrame].map(df => Multiset(Set.empty, df))
      case ProjectF(vars, r) =>
        r.select(vars: _*).pure[M]
    }

  def evaluate(
      dataframe: DataFrame,
      query: Query
  )(implicit
      sc: SQLContext
  ): Result[DataFrame] = {
    val eval =
      scheme.cataM[M, ExprF, Expr, Multiset](evaluateAlgebraM)

    eval(query.r)
      .runA(dataframe)
      .map(_.dataframe)
      .map(QueryExecutor.execute(query))
  }

  private def evaluateBGPF(
      triples: Seq[Expr.Triple]
  )(implicit sc: SQLContext) = {
    import sc.implicits._
    StateT.get[Result, DataFrame].map { df: DataFrame =>
      Foldable[List].fold(
        triples.toList.map({ triple =>
          val predicate = Predicate.fromTriple(triple)
          val current = applyPredicateToDataFrame(predicate, df)
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

  }

  private def applyPredicateToDataFrame(
      predicate: Predicate,
      df: DataFrame
  ): DataFrame =
    predicate match {
      case Predicate.SPO(s, p, o) =>
        df.filter(df("s") === s && df("p") === p && df("o") === o)
      case Predicate.SP(s, p) =>
        df.filter(df("s") === s && df("p") === p)
      case Predicate.PO(p, o) =>
        df.filter(df("p") === p && df("o") === o)
      case Predicate.SO(s, o) =>
        df.filter(df("s") === s && df("o") === o)
      case Predicate.S(s) =>
        df.filter(df("s") === s)
      case Predicate.P(p) =>
        df.filter(df("p") === p)
      case Predicate.O(o) =>
        df.filter(df("o") === o)
      case Predicate.None =>
        df
    }

}
