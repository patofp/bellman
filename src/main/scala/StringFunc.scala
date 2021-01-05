sealed trait StringFunc

final case class URI(s:String) extends StringFunc
final case class CONCAT(appendTo:String, append:String) extends StringFunc
