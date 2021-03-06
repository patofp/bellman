package com.gsk.kg.sparqlparser

import org.scalatest.flatspec.AnyFlatSpec
import com.gsk.kg.sparqlparser.StringVal._

class StringValParserSpec extends AnyFlatSpec{
  "parse the string literal with tag" should "create proper STRING cass class" in {
    val s = "\"abc\"@en"
    val p = fastparse.parse(s, StringValParser.tripleValParser(_))
    p.get.value match {
      case STRING("abc", Some("@en")) => succeed
      case _ => fail
    }
    val s1 = "\"345\"^^<http://www.w3.org/2001/XMLSchema#xsd:integer>"
    val p1 = fastparse.parse(s1, StringValParser.tripleValParser(_))
    p1.get.value match {
      case STRING("345", Some("<http://www.w3.org/2001/XMLSchema#xsd:integer>")) => succeed
      case _ => fail
    }
  }

  "parse variable" should "create proper VARIABLE cass class" in {
    val s = "?ab_cd"
    val p = fastparse.parse(s, StringValParser.tripleValParser(_))
    p.get.value match {
      case VARIABLE("?ab_cd") => succeed
      case _ => fail
    }
  }

  "parse uri" should "create proper URIVAL cass class" in {
    val s = "<http://id.gsk.com/dm/1.0/>"
    val p = fastparse.parse(s, StringValParser.tripleValParser(_))
    p.get.value match {
      case URIVAL("<http://id.gsk.com/dm/1.0/>") => succeed
      case _ => fail
    }
  }

  "parse number" should "create proper NUM cass class" in {
    val s = "-123.456"
    val p = fastparse.parse(s, StringValParser.tripleValParser(_))
    p.get.value match {
      case NUM("-123.456") => succeed
      case _ => fail
    }
  }

  "parse blank node" should "create proper BLANK cass class" in {
    val s = "_:iamblank"
    val p = fastparse.parse(s, StringValParser.tripleValParser(_))
    p.get.value match {
      case BLANK("iamblank") => succeed
      case _ => fail
    }
  }

  "parse boolean" should "create proper BOOL cass class" in {
    val s = "false"
    val p = fastparse.parse(s, StringValParser.tripleValParser(_))
    p.get.value match {
      case BOOL("false") => succeed
      case _ => fail
    }
  }

}
