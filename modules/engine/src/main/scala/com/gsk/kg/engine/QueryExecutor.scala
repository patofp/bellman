package com.gsk.kg.engine

import com.gsk.kg.sparqlparser.Query
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.SQLContext
import com.gsk.kg.sparqlparser.Query.Construct

object QueryExecutor {

  def execute(
      query: Query
  )(df: DataFrame)(implicit sc: SQLContext): DataFrame =
    query match {
      case Construct(vars, bgp, r) =>
        import sc.implicits._
        val acc = List.empty[(String, String, String)].toDF("s", "p", "o")

        bgp.triples
          .map({ triple =>
            import org.apache.spark.sql.functions._

            val cols = (triple.getVariables ++ triple.getPredicates)

            cols
              .foldLeft(df)({
                case (df, (sv, pos)) =>
                  if (df.columns.contains(sv.s)) {
                    df.withColumnRenamed(sv.s, pos)
                  } else {
                    df.withColumn(pos, lit(sv.s))
                  }
              })
              .select("s", "p", "o")
          })
          .foldLeft(acc) { (acc, other) =>
            acc.union(other)
          }.dropDuplicates()

      case _ => df
    }


}
