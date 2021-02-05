package com.gsk.kg.sparql.impl

import org.scalatest.flatspec.AnyFlatSpec
import com.gsk.kg.sparql.Visitors
import com.gsk.kg.sparqlparser.{QueryConstruct, TestUtils}

class SimpleVisitorSpec extends AnyFlatSpec {

  val visitor = SimpleVisitor()

  "Select Query" should "parse and run through the visitor to generate same query expression" in {
    val query =
      """
        PREFIX foaf: <http://xmlns.com/foaf/0.1/>
        PREFIX  dm:  <http://gsk-kg.rdip.gsk.com/dm/1.0/>

        SELECT ?name ?person WHERE {
          GRAPH <http://id.gsk.com/dm/1.0/graph1> {
            ?d a dm:Document .
          }
          {
            ?person foaf:mbox <mailto:alice@example.org> .
            ?person foaf:name ?name .
            OPTIONAL { ?d dm:pubDateYear ?year }
            FILTER (?year > 2015)
            FILTER (?d = "test")
            FILTER (STRSTARTS(str(?name), "al"))
          }
          UNION {
            ?a foaf:mbox <mailto:kg@example.org> .
            OPTIONAL {
              ?person foaf:name ?name .
              FILTER (?name = "def")
            }
            ?le dm:mappedTo ?concept .
            BIND(?d as ?dbind) .
            BIND(?name as ?nm) .
          }
        }
      """
    val expr = QueryConstruct.parseADT(query)
    val result = Visitors.dispatch(expr,visitor)
    assert(expr == QueryConstruct.parseADT(result))
  }

  "Construct Query" should "parse and run through the visitor and generate same query expression" in {
    val query =
      """
        PREFIX foaf: <http://xmlns.com/foaf/0.1/>
        PREFIX  dm:  <http://gsk-kg.rdip.gsk.com/dm/1.0/>

        CONSTRUCT {
          ?d a dm:Document .
          ?d dm:docSource ?src .
        } WHERE {
          {
            GRAPH <http://id.gsk.com/dm/1.0/graph1> {
              ?person foaf:mbox <mailto:alice@example.org> .
              ?person foaf:name ?name .
              OPTIONAL { ?d dm:pubDateYear ?year }
              FILTER (?year > 2015)
            }
            FILTER (?d = "test")
            FILTER (STRSTARTS(str(?name), "al"))
          }
          UNION {
            ?a foaf:mbox <mailto:kg@example.org> .
            OPTIONAL {
              ?person foaf:name ?name .
              FILTER (?name = "def")
            }
            ?le dm:mappedTo ?concept .
            BIND(?d as ?dbind) .
          }
        }
      """
    val expr = QueryConstruct.parseADT(query)
    val result = Visitors.dispatch(expr,visitor)
    assert(expr == QueryConstruct.parseADT(result))
  }

  "Lit search query" should "work" in {
    val expr = TestUtils.queryConstruct("/queries/lit-search-2.sparql")
    val result = Visitors.dispatch(expr,visitor)
    assert(expr == QueryConstruct.parseADT(result))
  }
}
