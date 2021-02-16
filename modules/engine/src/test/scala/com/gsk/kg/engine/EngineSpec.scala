package com.gsk.kg.engine

import com.gsk.kg.sparql.syntax.all._
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
import com.holdenkarau.spark.testing.DataFrameSuiteBase
import org.apache.spark.sql.Row

class EngineSpec extends AnyFlatSpec with Matchers with DataFrameSuiteBase {

  override implicit def reuseContextIfPossible: Boolean = true

  override implicit def enableHiveSupport: Boolean = false

  val dfList = List(
      (
        "test",
        "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>",
        "<http://id.gsk.com/dm/1.0/Document>"
      ),
      ("test", "<http://id.gsk.com/dm/1.0/docSource>", "source")
    )

  "Engine" should "perform query operations in the dataframe" in {
    import sqlContext.implicits._

    val df = dfList.toDF("s", "p", "o")
    val query = sparql"""
      SELECT
        ?s ?p ?o
      WHERE {
        ?s ?p ?o .
      }
      """

    Engine.evaluate(df, query.r).right.get.collect() shouldEqual df.collect()
  }

  it should "execute a query with two dependent BGPs" in {
    import sqlContext.implicits._

    val df: DataFrame = dfList.toDF("s", "p", "o")

    val query = sparql"""
      SELECT
        ?d ?src
      WHERE {
        ?d a <http://id.gsk.com/dm/1.0/Document> .
        ?d <http://id.gsk.com/dm/1.0/docSource> ?src
      }
      """

    Engine.evaluate(df, query.r).right.get.collect() shouldEqual Array(Row("test", "source"))
  }

  it should "execute a UNION query BGPs with the same bindings" in {
    import sqlContext.implicits._

    val df: DataFrame = (("does", "not", "match") :: dfList).toDF("s", "p", "o")

    val query = sparql"""
      SELECT
        ?s ?o
      WHERE {
        { ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?o }
        UNION
        { ?s <http://id.gsk.com/dm/1.0/docSource> ?o }
      }
      """

    Engine.evaluate(df, query.r).right.get.collect() shouldEqual Array(
      Row("test", "<http://id.gsk.com/dm/1.0/Document>"),
      Row("test", "source"))
  }

  it should "execute a UNION query BGPs with different bindings" in {
    import sqlContext.implicits._

    val df: DataFrame = (("does", "not", "match") :: dfList).toDF("s", "p", "o")

    val query = sparql"""
      SELECT
        ?s ?o ?s2 ?o2
      WHERE {
        { ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?o }
        UNION
        { ?s2 <http://id.gsk.com/dm/1.0/docSource> ?o2 }
      }
      """

    Engine.evaluate(df, query.r).right.get.collect() shouldEqual Array(
      Row("test", "<http://id.gsk.com/dm/1.0/Document>", null, null),
      Row(null, null, "test", "source"))
  }

}
