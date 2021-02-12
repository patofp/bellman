package com.gsk.kg.engine

import org.scalatest.flatspec.AnyFlatSpec
import org.apache.spark.sql.{SparkSession, DataFrame}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite
import org.apache.spark.sql.Dataset
import org.scalatest.matchers.should.Matchers

class MultisetSpec extends AnyFlatSpec with Matchers with BeforeAndAfterAll {

  val spark = SparkSession
    .builder()
    .appName("Spark SQL basic example")
    .config("spark.master", "local")
    .getOrCreate()

  import spark.implicits._

  override protected def afterAll(): Unit = {
    spark.stop()
  }

  "Multiset.join" should "join two empty multisets together" in {
    val ms1 = Multiset(Set.empty, spark.emptyDataFrame)
    val ms2 = Multiset(Set.empty, spark.emptyDataFrame)

    ms1.join(ms2) shouldEqual Multiset(Set.empty, spark.emptyDataFrame)
  }

}
