package com.gsk.kg.engine

import com.gsk.kg.sparqlparser.StringVal.VARIABLE
import org.apache.spark.sql.DataFrame
import cats.kernel.Semigroup
import org.apache.spark.sql.SQLContext
import cats.kernel.Monoid

final case class Multiset(
  bindings: Set[VARIABLE],
  dataframe: DataFrame
) {

  def join(other: Multiset) = (this, other) match {
    case (a, b) if a.isEmpty => b
    case (a, b) if b.isEmpty => a
    case (Multiset(aBindings, aDF), Multiset(bBindings, bDF)) if aBindings.intersect(bBindings).isEmpty =>
      val df = aDF.as("a")
        .crossJoin(bDF.as("b"))

      Multiset(aBindings.union(bBindings), df)

    case (a, b) =>
      val common = a.bindings.intersect(b.bindings)
      val df = a.dataframe.as("a")
        .join(b.dataframe.as("b"), common.map(_.s).toSeq)

      Multiset(a.bindings.union(b.bindings), df)
  }

  def isEmpty: Boolean = bindings.isEmpty && dataframe.isEmpty

}

object Multiset {

  implicit val semigroup: Semigroup[Multiset] = new Semigroup[Multiset] {
    def combine(x: Multiset, y: Multiset): Multiset = x.join(y)
  }

  implicit def monoid(implicit sc: SQLContext): Monoid[Multiset] = new Monoid[Multiset] {
    def combine(x: Multiset, y: Multiset): Multiset = x.join(y)
    def empty: Multiset = Multiset(Set.empty, sc.emptyDataFrame)
  }

}
