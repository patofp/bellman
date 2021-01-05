import fastparse.Parsed
import org.scalatest.flatspec.AnyFlatSpec


class StringFuncParserSpec extends AnyFlatSpec {
  "URI function" should "return URI type" in {
    val s = "(uri \"http://id.gsk.com/dm/1.0/\")"
    val p = fastparse.parse(s, StringFuncParser.parser(_))
    p.get.value match {
      case URI(s: String) => succeed
      case _ => fail
    }
  }

  "CONCAT function" should "return CONCAT type" in {
    val s = "(concat \"http://id.gsk.com/dm/1.0/\" ?src)"
    val p = fastparse.parse(s, StringFuncParser.parser(_))
    p.get.value match {
      case CONCAT("\"http://id.gsk.com/dm/1.0/\"", "?src") => succeed
      case _ => fail
    }
  }
}
