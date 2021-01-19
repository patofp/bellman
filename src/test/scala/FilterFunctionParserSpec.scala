import org.scalatest.flatspec.AnyFlatSpec
import FilterFunction._
import StringVal._
import StringFunc._


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
}
