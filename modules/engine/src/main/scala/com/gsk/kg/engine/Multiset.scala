package com.gsk.kg.engine

import com.gsk.kg.sparqlparser.StringVal.VARIABLE
import org.apache.spark.sql.DataFrame
import cats.kernel.Semigroup
import org.apache.spark.sql.SQLContext
import cats.kernel.Monoid


/**
  * A [[Multiset]], as expressed in SparQL terms.
  *
  * @see See [[https://www.w3.org/2001/sw/DataAccess/rq23/rq24-algebra.html]]
  * @param bindings the variables this multiset expose
  * @param dataframe the underlying data that the multiset contains
  */
final case class Multiset(
  bindings: Set[VARIABLE],
  dataframe: DataFrame
) {

  /**
    * Join two multisets following SparQL semantics.  If two multisets
    * share some bindings, it performs an _inner join_ between them.
    * If they don't share any common binding, it performs a cross join
    * instead.
    *
    * @param other
    * @return the join result of both multisets
    */
  def join(other: Multiset): Multiset = (this, other) match {
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

  def select(vars: VARIABLE*): Multiset =
    Multiset(
      bindings.intersect(vars.toSet),
      dataframe.select(vars.map(v => dataframe(v.s)): _*)
    )
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