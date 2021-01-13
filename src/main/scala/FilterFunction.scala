sealed trait FilterFunction

final case class EQUALS() extends FilterFunction
final case class REGEX() extends FilterFunction
