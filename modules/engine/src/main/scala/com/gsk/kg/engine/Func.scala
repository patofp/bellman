package com.gsk.kg.engine

import cats.syntax.either._

import org.apache.spark.sql.functions._
import org.apache.spark.sql.Column
import com.gsk.kg.sparqlparser.StringFunc
import com.gsk.kg.sparqlparser.StringFunc.URI
import com.gsk.kg.sparqlparser.StringFunc.CONCAT
import com.gsk.kg.sparqlparser.StringFunc.STR
import com.gsk.kg.sparqlparser.StringFunc.STRAFTER
import com.gsk.kg.sparqlparser.StringFunc.ISBLANK
import com.gsk.kg.sparqlparser.StringFunc.REPLACE
import com.gsk.kg.sparqlparser.StringVal.STRING

object Func {


  /**
    * Implementation of SparQL STRAFTER on Spark dataframes.
    *
    * | Function call                  | Result            |
    * |:-------------------------------|:------------------|
    * | strafter("abc","b")            | "c"               |
    * | strafter("abc"@en,"ab")        | "c"@en            |
    * | strafter("abc"@en,"b"@cy)      | error             |
    * | strafter("abc"^^xsd:string,"") | "abc"^^xsd:string |
    * | strafter("abc","xyz")          | ""                |
    * | strafter("abc"@en, "z"@en)     | ""                |
    * | strafter("abc"@en, "z")        | ""                |
    * | strafter("abc"@en, ""@en)      | "abc"@en          |
    * | strafter("abc"@en, "")         | "abc"@en          |
    *
    * TODO (pepegar): Implement argument compatibility checks
    *
    * @see [[https://www.w3.org/TR/sparql11-query/#func-strafter]]
    * @param col
    * @param str
    * @return
    */
  def strafter(col: Column, str: String): Column =
    when(substring_index(col, str, -1) === col, lit(""))
      .otherwise(substring_index(col, str, -1))


  def fromStringFunc(stringFunc: StringFunc): Column => Either[EngineError, Column] = col =>
    stringFunc match {
	    case URI(s) => col.asRight
	    case CONCAT(appendTo, append) => col.asRight
	    case STR(s) => col.asRight
	    case STRAFTER(s, STRING(separator)) => strafter(col, separator).asRight
	    case STRAFTER(s, _) => EngineError.General("not implemented yet").asLeft
	    case ISBLANK(s) => col.asRight
	    case REPLACE(st, pattern, by) => col.asRight
    }

}
