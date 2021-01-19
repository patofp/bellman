sealed trait StringLike

sealed trait StringFunc extends StringLike
sealed trait StringVal extends StringLike

object StringFunc {
  final case class URI(s:StringLike) extends StringFunc
  final case class CONCAT(appendTo:StringLike, append:StringLike) extends StringFunc
  final case class STR(s:StringLike) extends StringFunc
  final case class STRAFTER(s:StringLike, f:StringLike) extends StringFunc
}

object StringVal {
  final case class STRING(s:String) extends StringVal
  final case class VARIABLE(v:String) extends StringVal
  final case class URIVAL(u:String) extends StringVal
}

