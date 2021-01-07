sealed trait StringLike

sealed trait StringFunc extends StringLike

final case class URI(s:StringLike) extends StringFunc
final case class CONCAT(appendTo:StringLike, append:StringLike) extends StringFunc

sealed trait StringVal extends StringLike

final case class STRING(s:String) extends StringVal
final case class VARIABLE(v:String) extends StringVal
final case class URIVAL(u:String) extends StringVal



