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
import com.gsk.kg.sparqlparser.Query
import org.apache.jena.riot.RDFDataMgr
import org.apache.jena.rdf.model.Model
import org.apache.jena.riot.lang.CollectorStreamTriples
import org.apache.jena.riot.RDFParser

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

  "Engine" should "perform query operations in the dataframe" ignore {
    import sqlContext.implicits._

    val df = dfList.toDF("s", "p", "o")
    val query = sparql"""
      SELECT
        ?s ?p ?o
      WHERE {
        ?s ?p ?o .
      }
      """

    Engine.evaluate(df, query).right.get.collect() shouldEqual df.collect()
  }

  it should "execute a query with two dependent BGPs" ignore {
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

    Engine.evaluate(df, query).right.get.collect() shouldEqual Array(
      Row("test", "source")
    )
  }

  it should "execute a UNION query BGPs with the same bindings" ignore {
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

    Engine.evaluate(df, query).right.get.collect() shouldEqual Array(
      Row("test", "<http://id.gsk.com/dm/1.0/Document>"),
      Row("test", "source")
    )
  }

  it should "execute a UNION query BGPs with different bindings" ignore {
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

    Engine.evaluate(df, query).right.get.collect() shouldEqual Array(
      Row("test", "<http://id.gsk.com/dm/1.0/Document>", null, null),
      Row(null, null, "test", "source")
    )
  }

  it should "execute a CONSTRUCT with a single triple pattern" ignore {
    import sqlContext.implicits._

    val df: DataFrame = dfList.toDF("s", "p", "o")

    val query = sparql"""
      CONSTRUCT {
        ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?o
      } WHERE {
        ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?o
      }
      """

    Engine.evaluate(df, query).right.get.collect() shouldEqual Array(
      Row(
        "test",
        "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>",
        "<http://id.gsk.com/dm/1.0/Document>"
      )
    )
  }

  it should "execute a CONSTRUCT with more than one triple pattern" ignore {
    import sqlContext.implicits._

    val positive = List(
        ("doesmatch", "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>", "<http://id.gsk.com/dm/1.0/Document>"),
        ("doesmatchaswell", "<http://id.gsk.com/dm/1.0/docSource>", "potato")
      )
    val df: DataFrame = (positive ++ dfList).toDF("s", "p", "o")

    val query = sparql"""
      CONSTRUCT {
        ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?o .
        ?s2 <http://id.gsk.com/dm/1.0/docSource> ?o2
      } WHERE {
        ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?o .
        ?s2 <http://id.gsk.com/dm/1.0/docSource> ?o2
      }
      """

    Engine.evaluate(df, query).right.get.collect().toSet shouldEqual Set(
      Row(
        "doesmatch",
        "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>",
        "<http://id.gsk.com/dm/1.0/Document>"
      ),
      Row(
        "doesmatchaswell",
        "<http://id.gsk.com/dm/1.0/docSource>",
        "potato"
      ),
      Row(
        "test",
        "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>",
        "<http://id.gsk.com/dm/1.0/Document>"
      ),
      Row(
        "test",
        "<http://id.gsk.com/dm/1.0/docSource>",
        "source"
      )
    )
  }


  it should "execute a CONSTRUCT with more than one triple pattern with common bindings" ignore {
    import sqlContext.implicits._

    val negative = List(
        ("doesntmatch", "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>", "<http://id.gsk.com/dm/1.0/Document>"),
        ("doesntmatcheither", "<http://id.gsk.com/dm/1.0/docSource>", "potato")
      )

    val df: DataFrame = (negative ++ dfList).toDF("s", "p", "o")

    val query = sparql"""
      CONSTRUCT
      {
        ?d <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://id.gsk.com/dm/1.0/Document> .
        ?d <http://id.gsk.com/dm/1.0/docSource> ?src
      }
      WHERE
      {
        ?d <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://id.gsk.com/dm/1.0/Document> .
        ?d <http://id.gsk.com/dm/1.0/docSource> ?src
      }
      """

    Engine.evaluate(df, query).right.get.collect().toSet shouldEqual Set(
      Row(
        "test",
        "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>",
        "<http://id.gsk.com/dm/1.0/Document>"
      ),
      Row(
        "test",
        "<http://id.gsk.com/dm/1.0/docSource>",
        "source"
      )
    )

  }

  it should "lalalala" in {


    readNTtoDF("fixtures/reference-q1-input.nt").show
    //spark.read.text(path).toDF().show
  }

  private def readNTtoDF(path: String) = {
    import sqlContext.implicits._
    import scala.collection.JavaConverters._

    val filename = s"modules/engine/src/test/resources/$path"
    val inputStream: CollectorStreamTriples = new CollectorStreamTriples();
    RDFParser.source(filename).parse(inputStream);

    inputStream
      .getCollected()
      .asScala
      .toList
      .map(triple =>
        (triple.getSubject().toString(), triple.getPredicate().toString(), triple.getObject().toString())
      ).toDF("s", "p", "o")

  }

}
