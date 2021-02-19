package com.gsk.kg.sparqlparser

import org.scalatest.flatspec.AnyFlatSpec
import com.gsk.kg.sparqlparser.Expr._
import com.gsk.kg.sparqlparser.FilterFunction._
import com.gsk.kg.sparqlparser.StringFunc._
import com.gsk.kg.sparqlparser.StringVal._

import scala.collection.mutable.ArrayBuffer

class ShortQueryTestSpec extends AnyFlatSpec {

  "test filtered left join with multiple filters" should "pass" in {

  val q = """
    PREFIX  rdf:  <http://www.w3.org/2000/01/rdf-schema#>
    PREFIX  dm:   <http://gsk-kg.rdip.gsk.com/dm/1.0/>
    PREFIX  litp:  <http://lit-search-api/property/>

    CONSTRUCT
    {
      ?doc litp:containsEntity ?detent .
      ?detent litp:partOfDoc ?doc .
    }
    WHERE
    {
      ?de a dm:DetectedEntity .
      OPTIONAL {
        ?de dm:predictedBy ?model .
        ?model dm:modelName ?modelname .
        BIND(STRAFTER(str(?modelname), "ner:") as ?nermodel) .
        FILTER (STRSTARTS(str(?modelname), "ner:"))
        FILTER (STRSTARTS(str(?modelname), "ner1:"))
      }
    }

    """
    val query = QueryConstruct.parse(q)
    query.r match {
      case FilteredLeftJoin(BGP(_), Extend(to,from, _), funcs) =>
        assert(funcs.size == 2)
      case _ => fail
    }
  }
}
