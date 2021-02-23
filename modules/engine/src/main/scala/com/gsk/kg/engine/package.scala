package com.gsk.kg

import cats.data.StateT
import org.apache.spark.sql.DataFrame

package object engine {

  type Result[A] = Either[EngineError, A]
  val Result = Either
  type M[A] = StateT[Result, DataFrame, A]
  val M = StateT

}
