package com.gsk.kg.sparqlparser

import com.gsk.kg.sparqlparser.Expr._
import com.gsk.kg.sparqlparser.StringVal._
import org.scalatest.flatspec.AnyFlatSpec

class QueryConstructSpec extends AnyFlatSpec {

  "Simple Query" should "parse Construct statement with correct number of Triples" in {
    TestUtils.queryConstruct("/queries/q0-simple-basic-graph-pattern.sparql") match {
      case Construct(vars, bgp, expr) =>
        assert(vars.size == 2 && bgp.triples.size == 2)
      case _ => fail
    }
  }


  "Construct" should "result in proper variables, a basic graph pattern, and algebra expression" in {
    TestUtils.queryConstruct("/queries/q3-union.sparql") match {
      case Construct(vars, bgp, Union(BGP(triplesL: Seq[Triple]), BGP(triplesR: Seq[Triple]))) =>
        val temp = QueryConstruct.getAllVariableNames(bgp)
        val all = vars.map(_.v).toSet
        assert((all -- temp) == Set("?lnk"))
      case _ => fail
    }
  }


  "Construct with Bind" should "contains bind variable" in {
    TestUtils.queryConstruct("/queries/q4-simple-bind.sparql") match {
      case Construct(vars, bgp, Extend(l: StringVal, r: StringVal, BGP(triples: Seq[Triple]))) =>
        vars.exists(_.v == "?dbind")
      case _ => fail
    }
  }

  "Complex named graph query" should "be captured properly in Construct" in {
    TestUtils.queryConstruct("/queries/q13-complex-named-graph.sparql") match {
      case Construct(vars, bgp, expr) =>
        assert(vars.size == 13)
        assert(vars.exists(va => va.v == "?ogihw"))
      case _ => fail
    }
  }

  "Complex lit-search query" should "return proper Construct type" in {
    TestUtils.queryConstruct("/queries/lit-search-3.sparql") match {
      case Construct(vars, bgp, expr) =>
        assert(bgp.triples.size == 11)
        assert(bgp.triples.head.o.asInstanceOf[BLANK].ref == bgp.triples(1).s.asInstanceOf[BLANK].ref)
        assert(vars.exists(v => v.v == "?secid"))
      case _ => fail
    }

  }

}
