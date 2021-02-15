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

    assertMultisetEquals(
      ms1.join(ms2),
      Multiset(Set.empty, spark.emptyDataFrame)
    )
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

  "Multiset.join" should "join other multiset when they have both the same single binding" in {
    import sqlContext.implicits._
    val variable = VARIABLE("d")
    val ms1 = Multiset(Set(variable), List("test1", "test2").toDF("d"))
    val ms2 = Multiset(Set(variable), List("test1", "test3").toDF("d"))

    assertMultisetEquals(
      ms1.join(ms2),
      Multiset(Set(variable), List("test1").toDF("d"))
    )
  }

  it should "join other multiset when they share one binding" in {
    import sqlContext.implicits._
    val d = VARIABLE("d")
    val e = VARIABLE("e")
    val f = VARIABLE("f")
    val ms1 = Multiset(
      Set(d, e),
      List(("test1", "234"), ("test2", "123")).toDF(d.s, e.s)
    )
    val ms2 = Multiset(
      Set(d, f),
      List(("test1", "hello"), ("test3", "goodbye")).toDF(d.s, f.s)
    )

    assertMultisetEquals(
      ms1.join(ms2),
      Multiset(
        Set(d, e, f),
        List(("test1", "234", "hello")).toDF("d", "e", "f")
      )
    )
  }

  it should "join other multiset when they share more than one binding" in {
    import sqlContext.implicits._
    val d = VARIABLE("d")
    val e = VARIABLE("e")
    val f = VARIABLE("f")
    val g = VARIABLE("g")
    val h = VARIABLE("h")
    val ms1 = Multiset(
      Set(d, e, g, h),
      List(("test1", "234", "g1", "h1"), ("test2", "123", "g2", "h2"))
        .toDF(d.s, e.s, g.s, h.s)
    )
    val ms2 = Multiset(
      Set(d, e, f),
      List(("test1", "234", "hello"), ("test3", "e2", "goodbye"))
        .toDF(d.s, e.s, f.s)
    )

    assertMultisetEquals(
      ms1.join(ms2),
      Multiset(
        Set(d, e, f, g, h),
        List(("test1", "234", "g1", "h1", "hello"))
          .toDF("d", "e", "g", "h", "f")
      )
    )
  }

  it should "perform a union when there's no shared bindings between multisets" in {
    import sqlContext.implicits._
    val d = VARIABLE("d")
    val e = VARIABLE("e")
    val f = VARIABLE("f")
    val g = VARIABLE("g")
    val h = VARIABLE("h")
    val i = VARIABLE("i")

    val ms1 = Multiset(
      Set(d, e, f),
      List(("test1", "234", "g1"), ("test2", "123", "g2")).toDF(d.s, e.s, f.s)
    )
    val ms2 = Multiset(
      Set(g, h, i),
      List(("test1", "234", "hello"), ("test3", "e2", "goodbye"))
        .toDF(g.s, h.s, i.s)
    )

    assertMultisetEquals(
      ms1.join(ms2),
      Multiset(
        Set(d, e, f, g, h, i),
        List(
          ("test1","234","g1","test1","234","hello"),
          ("test1","234","g1","test3","e2","goodbye"),
          ("test2","123","g2","test1","234","hello"),
          ("test2","123","g2","test3","e2","goodbye")
        ).toDF("d", "e", "g", "h", "f", "i")
      )
    )
  }

  "Multiset.union" should "work both sides are empty" in {
    val ms1 = Multiset(Set.empty, spark.emptyDataFrame)
    val ms2 = Multiset(Set.empty, spark.emptyDataFrame)

    assertMultisetEquals(
      ms1.union(ms2),
      Multiset(Set.empty, spark.emptyDataFrame)
    )
  }

  it should "work when left side is empty" in {
    import sqlContext.implicits._

    val ms1 = Multiset(Set.empty, spark.emptyDataFrame)
    val ms2 = Multiset(Set(VARIABLE("a")), List("A", "B", "C").toDF("a"))

    assertMultisetEquals(
      ms1.union(ms2),
      ms2
    )
  }

  it should "work when right side is empty" in {
    import sqlContext.implicits._

    val ms1 = Multiset(Set(VARIABLE("a")), List("A", "B", "C").toDF("a"))
    val ms2 = Multiset(Set.empty, spark.emptyDataFrame)

    assertMultisetEquals(
      ms1.union(ms2),
      ms1
    )
  }

  def assertMultisetEquals(ms1: Multiset, ms2: Multiset): Unit = {
    assert(ms1.bindings === ms2.bindings, "bindings are different")
    assert(
      ms1.dataframe.collect === ms2.dataframe.collect,
      "dataframes are different"
    )
  }

}
