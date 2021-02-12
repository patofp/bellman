package com.gsk.kg.engine

import com.gsk.kg.sparqlparser.StringVal.VARIABLE
import org.apache.spark.sql.DataFrame

final case class Multiset(
  bindings: Set[VARIABLE],
  dataframe: DataFrame
) {

  def join(other: Multiset) = (this, other) match {
    case (a, b) if a.isEmpty => b
    case (a, b) if b.isEmpty => a
    case (a, b) =>
      val common = a.bindings.union(b.bindings)
      val df = a.dataframe.as("a")
        .join(b.dataframe.as("b"), common.map(_.s).toSeq)

      Multiset(common, df)
  }

  def isEmpty: Boolean = bindings.isEmpty && dataframe.isEmpty

}
