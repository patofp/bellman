import fastparse.Parsed
import org.scalatest.flatspec.AnyFlatSpec


class StringFuncParserSpec extends AnyFlatSpec {
  "URI function with string" should "return URI type" in {
    val s = "(uri \"http://id.gsk.com/dm/1.0/\")"
    val p = fastparse.parse(s, StringFuncParser.parser(_))
    p.get.value match {
      case URI(STRING("http://id.gsk.com/dm/1.0/")) => succeed
      case _ => fail
    }
  }

  "URI function with variable" should "return URI type" in {
    val s = "(uri ?test)"
    val p = fastparse.parse(s, StringFuncParser.parser(_))
    p.get.value match {
      case URI(VARIABLE("?test")) => succeed
      case _ => fail
    }
  }

  "CONCAT function" should "return CONCAT type" in {
    val s = "(concat \"http://id.gsk.com/dm/1.0/\" ?src)"
    val p = fastparse.parse(s, StringFuncParser.parser(_))
    p.get.value match {
      case CONCAT(STRING("http://id.gsk.com/dm/1.0/"), VARIABLE("?src")) => succeed
      case _ => fail
    }
  }

  "Nested URI and CONCAT" should "return proper nested type" in {
    val s = "(uri (concat \"http://id.gsk.com/dm/1.0/\" ?src))"
    val p = fastparse.parse(s, StringFuncParser.parser(_))
    p.get.value match {
      case URI(CONCAT(STRING("http://id.gsk.com/dm/1.0/"), VARIABLE("?src"))) => succeed
      case _ => fail
    }
  }

  "str function" should "return STR type" in {
    val s = "(str ?d)"
    val p = fastparse.parse(s, StringFuncParser.parser(_))
    p.get.value match {
      case STR(i:StringLike) => succeed
      case _ => fail
    }
  }

  "strafter function" should "return STRAFTER type" in {
    val s = "(strafter ( str ?d) \"#\")"
    val p = fastparse.parse(s, StringFuncParser.parser(_))
    p.get.value match {
      case STRAFTER(STR(VARIABLE(s1:String)), STRING(s2:String)) => succeed
      case _ => fail
    }
  }

  "Deeply nested strafter function" should "return nested STRAFTER type" in {
    val s = "(uri (strafter (concat (str ?d) (str ?src)) \"#\"))"
    val p = fastparse.parse(s, StringFuncParser.parser(_))
    p.get.value match {
      case URI(STRAFTER(CONCAT(STR(VARIABLE(a1: String)), STR(VARIABLE(a2: String))), STRING("#"))) => succeed
      case _ => fail
    }
  }

  "strstarts function" should "return STRSTARTS type" in {
    val s = "(strstarts \"http://google.com/new:helloworld\" \"ner:\")"
    val p = fastparse.parse(s, StringFuncParser.parser(_))
    p.get.value match {
      case STRSTARTS(STRING("http://google.com/new:helloworld"), STRING("ner:")) => succeed
      case _ => fail
    }
  }

  "nested strstarts function" should "return nested STRSTARTS type" in {

    val s = "(uri (strstarts (concat (str ?d) (str ?src)) \"#\"))"
    val p = fastparse.parse(s, StringFuncParser.parser(_))
    p.get.value match {
      case URI(STRSTARTS(CONCAT(STR(VARIABLE(a1: String)), STR(VARIABLE(a2: String))), STRING("#"))) => succeed
      case _ => fail
    }
  }
}
