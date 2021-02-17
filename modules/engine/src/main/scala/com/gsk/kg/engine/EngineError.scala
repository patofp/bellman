package com.gsk.kg.engine

sealed trait EngineError

object EngineError {
  case class General(description: String) extends EngineError
}
