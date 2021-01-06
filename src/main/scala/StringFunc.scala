sealed trait StringFunc

final case class URI(s:StringFunc) extends StringFunc
final case class CONCAT(appendTo:StringFunc, append:StringFunc) extends StringFunc
final case class STRING(s:String) extends StringFunc
final case class VARIABLE(v:String) extends StringFunc

