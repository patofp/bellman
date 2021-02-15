package com.gsk.kg.sparqlparser
import com.gsk.kg.sparqlparser.Expr._
import com.gsk.kg.sparqlparser.FilterFunction._
import com.gsk.kg.sparqlparser.Query.{Construct, Describe, Select}
import com.gsk.kg.sparqlparser.StringFunc._
import com.gsk.kg.sparqlparser.StringVal._
import org.apache.jena.query.QueryFactory
import org.apache.jena.sparql.algebra.{Algebra, Op}
import org.scalatest.flatspec.AnyFlatSpec

class QuerySamplesTestSpec extends AnyFlatSpec {

  def showAlgebra(q: String): Op = {
    val query = QueryFactory.create(q)
    Algebra.compile(query)
  }

  "Get a small sample" should "parse" in {
    val query = QuerySamples.q1
    val expr = QueryConstruct.parseADT(query)
    expr match {
      case OffsetLimit(None,Some(20), _) => succeed
      case _ =>
        fail
    }
  }

  "Find label" should "parse" in {
    val query = QuerySamples.q2
    val expr = QueryConstruct.parseADT(query)
    expr match {
      case Project(vs, BGP(_)) =>
        assert(vs.nonEmpty && vs.head == VARIABLE("?label"))
      case _ =>
        fail
    }
  }

  "Find distinct label" should "parse" in {
    val query = QuerySamples.q3
    val expr = QueryConstruct.parseADT(query)
    expr match {
      case Distinct(Project(vs, BGP(_))) =>
        assert(vs.nonEmpty && vs.head == VARIABLE("?label"))
      case _ =>
        fail
    }
  }

  "Get all relations" should "parse" in {
    val query = QuerySamples.q4
    val expr = QueryConstruct.parseADT(query)
    expr match {
      case Project(vs, BGP(_)) =>
        assert(vs.nonEmpty && vs.size == 2)
      case _ =>
        fail
    }
  }

  "Get parent class" should "parse" in {
    val query = QuerySamples.q5
    val expr = QueryConstruct.parseADT(query)
    expr match {
      case Project(vs, BGP(_)) =>
        assert(vs.nonEmpty && vs.head == VARIABLE("?parent"))
      case _ =>
        fail
    }
  }

  "Get parent class with filter" should "parse" in {
    val query = QuerySamples.q6
    val expr = QueryConstruct.parseADT(query)
    expr match {
      case Project(vs, Filter(funcs,r)) =>
        assert(vs.nonEmpty && vs.head == VARIABLE("?parent"))
      case _ =>
        fail
    }
  }

  "Test multiple hops" should "parse" in {
    val query = QuerySamples.q7
    val expr = QueryConstruct.parseADT(query)
    expr match {
      case Project(vs, BGP(triples)) =>
        assert(vs.nonEmpty && vs.head == VARIABLE("?species"))
        assert(triples.size==7)
      case _ =>
        fail
    }
  }

  "Test multiple hops and prefixes" should "parse" in {
    val query = QuerySamples.q8
    val expr = QueryConstruct.parseADT(query)
    expr match {
      case Project(vs, BGP(triples)) =>
        assert(vs.nonEmpty && vs.head == VARIABLE("?species"))
        assert(triples.size==7)
      case _ =>
        fail
    }
  }

  "Test find label" should "parse" in {
    val query = QuerySamples.q9
    val expr = QueryConstruct.parseADT(query)
    expr match {
      case Project(vs, BGP(triples)) =>
        assert(vs.nonEmpty && vs.head == VARIABLE("?label"))
      case _ =>
        fail
    }
  }

  "Test find parent class" should "parse" in {
    val query = QuerySamples.q10
    val expr = QueryConstruct.parseADT(query)
    expr match {
      case Project(vs, BGP(triples)) =>
        assert(vs.nonEmpty && vs.head == VARIABLE("?parent"))
        assert(triples.size == 1)
      case _ =>
        fail
    }
  }


  "Tests hops and distinct" should "parse" in {
    val query = QuerySamples.q11
    val expr = QueryConstruct.parseADT(query)
    expr match {
      case Distinct(Project(vs, BGP(triples))) =>
        assert(vs.nonEmpty && vs.head == VARIABLE("?parent_name"))
        assert(triples.size == 2)
      case _ =>
        fail
    }
  }

  "Tests filter and bind" should "parse" in {
    val query = QuerySamples.q12
    val expr = QueryConstruct.parseADT(query)
    expr match {
      case Project(vs, Filter(funcs,Extend(to,from,BGP(_)))) =>
        assert(vs.nonEmpty && vs.size == 3)
        assert(funcs.size == 1)
        assert(to == VARIABLE("?o"))
      case _ =>
        fail
    }
  }

  // ignore for now since for diff position, Jena generates different representations
  "Test BIND in another position in the query" should "parse to same as q12" ignore {
    val query = QuerySamples.q13
    val expr = QueryConstruct.parseADT(query)
    println(expr)
    expr match {
      case Project(vs, Filter(funcs,Extend(to,from,BGP(_)))) =>
        assert(vs.nonEmpty && vs.size == 3)
        assert(funcs.size == 1)
        assert(to == VARIABLE("?o"))
      case _ =>
        fail
    }
  }

  "Test union" should "parse" in {
    val query = QuerySamples.q14
    val expr = QueryConstruct.parseADT(query)
    expr match {
      case Project(vs, Union(l,r)) =>
        assert(vs.nonEmpty && vs.size == 3)
        r match {
          case Filter(funcs,e) => succeed
          case _ => fail
        }
      case _ =>
        fail
    }
  }

  "Test simple describe query" should "parse" in {
    val query = QuerySamples.q15
    val q = QueryConstruct.parse(query)
    q match {
      case Describe(_, OpNil()) =>
        succeed
      case _ =>
        fail
    }
  }

  "Test describe query" should "parse" in {
    val query = QuerySamples.q16
    val q = QueryConstruct.parse(query)
    q match {
      case Describe(vars, Project(vs, Filter(_,_))) =>
        assert(vars == vs)
        succeed
      case _ =>
        fail
    }
  }

  "Tests str conversion and logical operators" should "parse" in {
    val query = QuerySamples.q17
    val q = QueryConstruct.parse(query)
    q match {
      case Select(vars, Project(vs, Filter(_,_)))=>
        succeed
      case _ =>
        fail
    }
  }

  "Tests FILTER positioning with graph sub-patterns" should "parse" in {
    val query = QuerySamples.q18
    val q = QueryConstruct.parse(query)
    q match {
      case Select(vars, Project(vs, Filter(_,_)))=>
        succeed
      case _ =>
        fail
    }
  }

  "Test for FILTER in different positions" should "parse" ignore {
    val query = QuerySamples.q19
    val q = QueryConstruct.parse(query)
    q match {
      case Select(vars, Distinct(Project(vs, Filter(_,_))))=>
        succeed
      case _ =>
        fail
    }
  }

  "Test CONSTRUCT and string replacement" should "parse" in {
    val query = QuerySamples.q20
    val q = QueryConstruct.parse(query)
    q match {
      case Construct(vars, _, Extend(to,REPLACE(_,_,_),r))=>
        succeed
      case _ =>
        fail
    }
  }

  "Test document query" should "parse" in {
    val query = QuerySamples.q21
    val q = QueryConstruct.parse(query)
    q match {
      case Select(vars, Project(vs, BGP(ts)))=>
        assert(ts.size == 21)
      case _ =>
        fail
    }
  }

}
