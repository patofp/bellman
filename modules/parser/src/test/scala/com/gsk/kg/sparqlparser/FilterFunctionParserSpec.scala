package com.gsk.kg.sparqlparser

import com.gsk.kg.sparqlparser.FilterFunction._
import com.gsk.kg.sparqlparser.StringFunc._
import com.gsk.kg.sparqlparser.StringVal._
import org.scalatest.flatspec.AnyFlatSpec


class FilterFunctionParserSpec extends AnyFlatSpec{

  "strstarts function" should "return STRSTARTS type" in {
    val s = """(strstarts (str ?modelname) "ner:")"""
    val p = fastparse.parse(s, FilterFunctionParser.parser(_))
    p.get.value match {
      case STRSTARTS(STR(VARIABLE("?modelname")), STRING("ner:")) => succeed
      case _ => fail
    }
  }

  "Equals parser" should "return EQUALS type" in {
    val p = fastparse.parse("""(= ?d "Hello")""", FilterFunctionParser.equalsParen(_))
    p.get.value match {
      case EQUALS(VARIABLE("?d"), STRING("Hello")) => succeed
      case _ => fail
    }
  }

  "Regex parser" should "return REGEX type" in {
    val p = fastparse.parse("""(regex ?d "Hello")""", FilterFunctionParser.regexParen(_))
    p.get.value match {
      case REGEX(VARIABLE("?d"), STRING("Hello")) => succeed
      case _ => fail
    }
  }

  "GT parser" should "return GT type" in {
    val p = fastparse.parse("""(> ?year "2015")""", FilterFunctionParser.gtParen(_))
    p.get.value match {
      case GT(VARIABLE("?year"), STRING("2015")) => succeed
      case _ => fail
    }
  }

  "LT parser" should "return LT type" in {
    val p = fastparse.parse("""(< ?year "2015")""", FilterFunctionParser.ltParen(_))
    p.get.value match {
      case LT(VARIABLE("?year"), STRING("2015")) => succeed
      case _ => fail
    }
  }
}
