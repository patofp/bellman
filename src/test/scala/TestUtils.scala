import org.apache.jena.query.QueryFactory
import org.apache.jena.sparql.algebra.Algebra

import scala.io.Source

object TestUtils {

  def sparql2Algebra(fileLoc:String):String = {
    val path = getClass.getResource(fileLoc).getPath
    val sparql = Source.fromFile(path).mkString

    val query = QueryFactory.create(sparql)
    Algebra.compile(query).toString
  }

}
