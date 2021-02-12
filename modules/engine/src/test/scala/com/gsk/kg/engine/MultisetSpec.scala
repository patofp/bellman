package com.gsk.kg.engine

import org.scalatest.flatspec.AnyFlatSpec
import org.apache.spark.sql.{SparkSession, DataFrame}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite
import org.apache.spark.sql.Dataset
import org.scalatest.matchers.should.Matchers
import com.gsk.kg.sparqlparser.StringVal.VARIABLE
import com.holdenkarau.spark.testing.DataFrameSuiteBase

class MultisetSpec extends AnyFlatSpec with Matchers with DataFrameSuiteBase {

  override implicit def reuseContextIfPossible: Boolean = true

  override implicit def enableHiveSupport: Boolean = false

  "Multiset.join empty" should "join two empty multisets together" in {
    val ms1 = Multiset(Set.empty, spark.emptyDataFrame)
    val ms2 = Multiset(Set.empty, spark.emptyDataFrame)

    assertMultisetEquals(ms1.join(ms2), Multiset(Set.empty, spark.emptyDataFrame))
  }

  it should "join other nonempty multiset on the right" in {
    import sqlContext.implicits._
    val empty = Multiset(Set.empty, spark.emptyDataFrame)
    val nonEmpty = Multiset(Set(VARIABLE("d")), Seq("test1", "test2").toDF("d"))

    assertMultisetEquals(empty.join(nonEmpty), nonEmpty)
  }

  it should "join other nonempty multiset on the left" in {
    import sqlContext.implicits._
    val empty = Multiset(Set.empty, spark.emptyDataFrame)
    val nonEmpty = Multiset(Set(VARIABLE("d")), Seq("test1", "test2").toDF("d"))

    assertMultisetEquals(nonEmpty.join(empty), nonEmpty)
  }

  "Multiset.join" should "join other multiset when they share one binding" in {
    import sqlContext.implicits._
    val variable = VARIABLE("d")
    val ms1 = Multiset(Set(variable), List("test1", "test2").toDF("d"))
    val ms2 = Multiset(Set(variable), List("test1", "test3").toDF("d"))

    assertMultisetEquals(ms1.join(ms2), Multiset(Set(variable), List("test1").toDF("d")))
  }

  def assertMultisetEquals(ms1: Multiset, ms2: Multiset): Unit = {
    assert(ms1.bindings === ms2.bindings, "bindings are different")
    assert(ms1.dataframe.collect === ms2.dataframe.collect, "dataframes are different")
  }

}
