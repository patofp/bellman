package com.gsk.kg.engine

import cats.Foldable
import cats.implicits._

import org.apache.spark.sql.DataFrame

import com.gsk.kg.engine._
import com.gsk.kg.sparqlparser._
import com.gsk.kg.sparqlparser.Expr.fixedpoint._

import higherkindness.droste._
import org.apache.spark.sql.SQLContext
import com.gsk.kg.sparqlparser.StringVal
import com.gsk.kg.engine.Multiset._
import com.gsk.kg.engine.Predicate.None
import com.gsk.kg.sparqlparser.Query
import com.gsk.kg.sparqlparser.Query.Construct
import com.gsk.kg.sparqlparser.StringFunc._
import com.gsk.kg.sparqlparser.StringVal._
import com.gsk.kg.sparqlparser.Expression

object Engine {

  def evaluateAlgebraM(implicit sc: SQLContext): AlgebraM[M, ExprF, Multiset] =
    AlgebraM[M, ExprF, Multiset] {
      case BGPF(triples) => evaluateBGPF(triples)
      case TripleF(s, p, o) => notImplemented("TripleF")
      case LeftJoinF(l, r) => notImplemented("LeftJoinF")
      case FilteredLeftJoinF(l, r, f) => notImplemented("FilteredLeftJoinF")
      case UnionF(l, r) =>
        l.union(r).pure[M]
      case ExtendF(bindTo, bindFrom, r) =>
        evaluateExtendF(bindTo, bindFrom, r)
      case FilterF(funcs, expr) => notImplemented("FilterF")
      case JoinF(l, r) => notImplemented("JoinF")
      case GraphF(g, e) => notImplemented("GraphF")
      case DistinctF(r) => notImplemented("DistinctF")
      case OffsetLimitF(offset, limit, r) => notImplemented("OffsetLimitF")
      case OpNilF() => notImplemented("OpNilF")
      case ProjectF(vars, r) =>
        r.select(vars: _*).pure[M]
      case TabUnitF() => notImplemented("TabUnitF")
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

  private def evaluateExtendF(
      bindTo: VARIABLE,
      bindFrom: Expression,
      r: Multiset
  ) = {
    val getColumn = ExpressionF.compile(bindFrom)

    M.liftF[Result, DataFrame, Multiset](
      getColumn(r.dataframe).map { col =>
        r.withColumn(bindTo, col)
      }
    )
  }

  private def evaluateBGPF(
      triples: Seq[Expr.Triple]
  )(implicit sc: SQLContext) = {
    import sc.implicits._
    M.get[Result, DataFrame].map { df: DataFrame =>
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

  private def notImplemented(constructor: String): M[Multiset] =
    M.liftF[Result, DataFrame, Multiset](
      EngineError.General(s"$constructor not implemented").asLeft[Multiset]
    )

}
