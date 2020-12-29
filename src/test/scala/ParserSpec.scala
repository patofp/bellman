import org.apache.jena.query.QueryFactory
import org.apache.jena.sparql.algebra.Algebra
import scala.io.Source
import org.scalatest.flatspec.AnyFlatSpec

class ParserSpec extends AnyFlatSpec {

  def sparql2Algebra(fileLoc:String):String = {
    val path = getClass.getResource(fileLoc).getPath
    val sparql = Source.fromFile(path).mkString

    val query = QueryFactory.create(sparql)
    Algebra.compile(query).toString
  }

  "Basic Graph Pattern" should "parse correct number of Triples" in {
    val p = fastparse.parse(sparql2Algebra("/queries/q0-simple-basic-graph-pattern.sparql"), Parser.parser(_))
    p.get.value match {
      case BGP(triples) => assert(triples.length == 2)
      case _ => fail
    }
  }

  "Single optional" should "result in single leftjoin" in {
    val p = fastparse.parse(sparql2Algebra("/queries/q1-single-leftjoin.sparql"), Parser.parser(_))
    p.get.value match {
      case LeftJoin(l:BGP, r:BGP) => succeed
      case LeftJoin(l:LeftJoin, r:BGP) => fail
    }
  }

  "Double optional" should "result in nested leftjoin" in {
    val p = fastparse.parse(sparql2Algebra("/queries/q2-nested-leftjoins.sparql"), Parser.parser(_))
    p.get.value match {
      case LeftJoin(l: LeftJoin, r: BGP) => succeed
      case LeftJoin(l: BGP, r: BGP) => fail
    }
  }

  "Single Union" should "result in a single nested union" in {
    val p = fastparse.parse(sparql2Algebra("/queries/q3-union.sparql"), Parser.parser(_))
    p.get.value match {
      case Union(BGP(triplesL:Seq[Triple]), BGP(triplesR:Seq[Triple])) => succeed
      case _ => fail
    }
  }
  "Single Bind" should "result in a successful extend instruction" in {
    val p = fastparse.parse(sparql2Algebra("/queries/q4-simple-bind.sparql"), Parser.parser(_))
    p.get.value match {
      case Extend(l:String, r:String, BGP(triples:Seq[Triple])) => succeed
      case _ => fail
    }
  }

  "Single union plus bind" should "result in a successful extend and union instruction" in {
    val p = fastparse.parse(sparql2Algebra("/queries/q5-union-plus-bind.sparql"), Parser.parser(_))
    p.get.value match {
      case Union(Extend(l:String, r:String, BGP(triples1:Seq[Triple])), BGP(triples2:Seq[Triple])) => succeed
      case _ => fail
    }
  }

  "Nested leftjoin, nested union, multiple binds" should "result in successful nestings" in {
    val p = fastparse.parse(sparql2Algebra("/queries/q6-nested-leftjoin-union-bind.sparql"),
      Parser.parser(_))

    p.get.value match {
      case
        Union(
          Union(
            Extend(s1:String,s2:String,
              LeftJoin(
                LeftJoin(
                  BGP(l1:Seq[Triple]),
                  BGP(l2:Seq[Triple])),
                BGP(l3:Seq[Triple]))),
              BGP(l4:Seq[Triple])),
            Extend(s3:String,s4:String,
              LeftJoin(
                BGP(l5:Seq[Triple]),
                BGP(l6:Seq[Triple]))))
       => succeed
      case _ => fail
    }
  }

  "Nested bind" should "Result in correct nesting of bind" in {
    val p = fastparse.parse(sparql2Algebra("/queries/q7-nested-bind.sparql"), Parser.parser(_))
    p.get.value match {
      case
        Extend(s1:String, s2:String,
          Extend(s3:String, s4:String,
            BGP(l1:Seq[Triple]))) => succeed
      case _ => fail
    }
  }

  "Filter over simple BGP" should "Result in correct nesting of filter and BGP" in {
    val p = fastparse.parse(sparql2Algebra("/queries/q8-filter-simple-basic-graph.sparql"), Parser.parser(_))
    p.get.value match {
      case Filter(s1:Seq[FilterFunction], b:BGP) => succeed
      case _ => fail
    }
  }

  "Multiple filters over simple BGP" should "Result in correct nesting of filters and BGP" in {
    val p = fastparse.parse(sparql2Algebra("/queries/q9-double-filter-simple-basic-graph.sparql"), Parser.parser(_))
    p.get.value match {
      case Filter(s1:Seq[FilterFunction], b:BGP) => succeed
      case _ => fail
    }
  }

  "Complex filters" should "Result in the correct nesting" in {
    val p  = fastparse.parse(sparql2Algebra("/queries/q10-complex-filter.sparql"), Parser.parser(_))
    p.get.value match {
      case Filter(
            seq1:Seq[FilterFunction],
              Union(
                Union(
                  Filter(
                    seq2:Seq[FilterFunction],
                    Extend(s1:String, s2:String,
                      LeftJoin(
                        LeftJoin(
                          BGP(seq3:Seq[Triple]),
                          BGP(seq4:Seq[Triple])),
                      BGP(seq5:Seq[Triple])))),
                    BGP(seq6:Seq[Triple])),
                Extend(s3:String, s4:String,
                  LeftJoin(
                    BGP(seq7:Seq[Triple]),
                    BGP(seq8:Seq[Triple]))))) => succeed
      case _ => fail
    }
  }

  "Simple named graph query" should "Return correct named graph algebra" in {
    val p = fastparse.parse(sparql2Algebra("/queries/q11-simple-named-graph.sparql"), Parser.parser(_))
    p.get.value match {
      case Join(Graph(ng1:String, BGP(s1:Seq[Triple])), BGP(s2:Seq[Triple])) => succeed
      case _ => fail
    }
  }

  "Double named graph query" should "Return correct named graph algebra" in {
    val p = fastparse.parse(sparql2Algebra("/queries/q12-double-named-graph.sparql"), Parser.parser(_))
    p.get.value match {
      case Join(Graph(ng1:String, BGP(s1:Seq[Triple])), Graph(ng2:String, BGP(s2:Seq[Triple]))) => succeed
      case _ => fail
    }
  }

  "Complex named graph query" should "Return correct named graph algebra" in {
    val p = fastparse.parse(sparql2Algebra("/queries/q13-complex-named-graph.sparql"), Parser.parser(_))

    p.get.value match {
      case Filter(
        seq1:Seq[FilterFunction],
        Union(
          Union(
            Graph(g1:String,
            Filter(
              seq2:Seq[FilterFunction],
              Extend(s1:String, s2:String,
                LeftJoin(
                  LeftJoin(
                    BGP(seq3:Seq[Triple]),
                    BGP(seq4:Seq[Triple])),
                BGP(seq5:Seq[Triple]))))),
              BGP(seq6:Seq[Triple])),
          Extend(s3:String, s4:String,
            LeftJoin(
              BGP(seq7:Seq[Triple]),
              BGP(seq8:Seq[Triple]))))) => succeed
      case _ => fail
    }
  }
}