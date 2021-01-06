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
    }
  }
}
