sealed trait FilterFunction

final case class EQUALS(l:StringLike, r:StringLike) extends FilterFunction
final case class REGEX(l:StringLike, r:StringLike) extends FilterFunction
final case class STRSTARTS(l:StringLike, r:StringLike) extends FilterFunction
