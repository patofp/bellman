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

}
