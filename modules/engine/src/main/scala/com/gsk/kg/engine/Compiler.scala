package com.gsk.kg.engine

import higherkindness.droste.syntax.all._
import cats.implicits._
import cats.data.Kleisli
import com.gsk.kg.sparqlparser.QueryConstruct
import com.gsk.kg.sparqlparser.Query
import cats.arrow.Arrow
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.DataFrame
import higherkindness.droste.Trans
import com.gsk.kg.sparqlparser.Expr.fixedpoint._
import higherkindness.droste.Basis
import higherkindness.droste.Project

object Compiler {

  def compile(df: DataFrame, query: String)(implicit sc: SQLContext): Result[DataFrame] =
    compiler(df)
      .run(query)
      .runA(df)


  /**
    * Put together all phases of the compiler
    *
    * @param df
    * @param sc
    * @return
    */
  def compiler(df: DataFrame)(implicit sc: SQLContext): Phase[String, DataFrame] =
    parser >>>
      optimizer >>>
      engine(df)


  /**
    * The engine phase receives a query and applies it to the given
    * dataframe
    *
    * @param df
    * @param sc
    * @return
    */
  def engine(df: DataFrame)(implicit sc: SQLContext): Phase[Query, DataFrame] =
    Kleisli { case query =>
      M.liftF(Engine.evaluate(df, query))
    }

  /**
    * parser converts strings to our [[Query]] ADT
    */
  val parser: Phase[String, Query] =
    Arrow[Phase].lift(QueryConstruct.parse)

  val optimizer: Phase[Query, Query] = Arrow[Phase].id

}
