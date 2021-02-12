package com.gsk.kg.engine

import com.gsk.kg.sparqlparser.Expr._
import com.gsk.kg.sparqlparser.FilterFunction._
import com.gsk.kg.sparqlparser.StringFunc._
import com.gsk.kg.sparqlparser.StringVal._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import com.gsk.kg.sparqlparser.QueryConstruct
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.DataFrame
import org.scalatest.BeforeAndAfterAll

class EngineSpec extends AnyFlatSpec with Matchers with BeforeAndAfterAll {

  val spark = SparkSession
    .builder()
    .appName("Spark SQL basic example")
    .config("spark.master", "local")
    .getOrCreate()

  import spark.implicits._

  val df: DataFrame = Seq(
    (
      "test",
      "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
      "http://id.gsk.com/dm/1.0/Document"
    ),
    ("test", "http://id.gsk.com/dm/1.0/docSource", "source")
    ).toDF("s", "p", "o")

  override protected def afterAll(): Unit = {
    spark.stop()
  }

  "An Engine" should "perform query operations in the dataframe" in {
    val expr = QueryConstruct.parseADT("""
      SELECT
        ?s ?p ?o
      WHERE {
        ?s ?p ?o .
      }
      """)

    Engine.evaluate(df, expr).right.get.collect() shouldEqual df.collect()
  }

}
