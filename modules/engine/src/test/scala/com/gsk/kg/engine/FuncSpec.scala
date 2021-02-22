package com.gsk.kg.engine

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import com.holdenkarau.spark.testing.DataFrameSuiteBase
import org.apache.spark.sql.Row

class FuncSpec extends AnyFlatSpec with Matchers with DataFrameSuiteBase {

  override implicit def reuseContextIfPossible: Boolean = true

  override implicit def enableHiveSupport: Boolean = false

  "Func.strafter" should "find the correct string if it exists" in {
    import sqlContext.implicits._

    val df = List(
      "hello#potato",
      "goodbye#tomato"
    ).toDF("text")

    df.select(Func.strafter(df("text"), "#").as("result"))
      .collect shouldEqual Array(
      Row("potato"),
      Row("tomato")
    )
  }

  it should "return empty strings otherwise" in {
    import sqlContext.implicits._

    val df = List(
      "hello potato",
      "goodbye tomato"
    ).toDF("text")

    df.select(Func.strafter(df("text"), "#").as("result"))
      .collect shouldEqual Array(
      Row(""),
      Row("")
    )
  }

  "Func.iri" should "do nothing for IRIs" in {
    import sqlContext.implicits._

    val df = List(
      "http://google.com",
      "http://other.com"
    ).toDF("text")

    df.select(Func.iri(df("text")).as("result")).collect shouldEqual Array(
      Row("http://google.com"),
      Row("http://other.com")
    )
  }

  "Func.concat" should "concatenate two string columns" in {
    import sqlContext.implicits._

    val df = List(
      ("Hello", " Dolly"),
      ("Here's a song", " Dolly")
    ).toDF("a", "b")

    df.select(Func.concat(df("a"), df("b")).as("verses"))
      .collect shouldEqual Array(
      Row("Hello Dolly"),
      Row("Here's a song Dolly")
    )
  }

  it should "concatenate a column with a literal string" in {
    import sqlContext.implicits._

    val df = List(
      ("Hello", " Dolly"),
      ("Here's a song", " Dolly")
    ).toDF("a", "b")

    df.select(Func.concat(df("a"), " world!").as("sentences"))
      .collect shouldEqual Array(
      Row("Hello world!"),
      Row("Here's a song world!")
    )
  }

  it should "concatenate a literal string with a column" in {
    import sqlContext.implicits._

    val df = List(
      ("Hello", " Dolly"),
      ("Here's a song", " Dolly")
    ).toDF("a", "b")

    df.select(Func.concat("Ciao", df("b")).as("verses"))
      .collect shouldEqual Array(
      Row("Ciao Dolly"),
      Row("Ciao Dolly")
    )
  }
}
