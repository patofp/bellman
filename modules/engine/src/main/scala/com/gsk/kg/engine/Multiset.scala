package com.gsk.kg.engine

import com.gsk.kg.sparqlparser.StringVal.VARIABLE
import org.apache.spark.sql.DataFrame

final case class Multiset(
  bindings: Set[VARIABLE],
  dataframe: DataFrame
) {

  def join(other: Multiset) = this

}
