package com.gsk.kg.engine

import cats.data.State
import cats.implicits._

import higherkindness.droste._
import higherkindness.droste.syntax.all._
import higherkindness.droste.macros.deriveTraverse

import org.apache.spark.sql.functions._

import com.gsk.kg.sparqlparser.Expression
import com.gsk.kg.sparqlparser.FilterFunction
import com.gsk.kg.sparqlparser.StringFunc
import com.gsk.kg.sparqlparser.StringVal
import com.gsk.kg.sparqlparser.StringLike
import org.apache.spark.sql.Column
import com.gsk.kg.engine.ExpressionF._

/**
  * [[ExpressionF]] is a pattern functor for the recursive
  * [[Expression]].
  *
  * Using Droste's syntax, we get tree traversals for free such as the
  * ones seen in [[getVariable]] or [[getString]]
  */
@deriveTraverse sealed trait ExpressionF[+A]

object ExpressionF {

  final case class EQUALS[A](l: A, r: A) extends ExpressionF[A]
  final case class REGEX[A](l: A, r: A) extends ExpressionF[A]
  final case class STRSTARTS[A](l: A, r: A) extends ExpressionF[A]
  final case class GT[A](l: A, r: A) extends ExpressionF[A]
  final case class LT[A](l: A, r: A) extends ExpressionF[A]
  final case class OR[A](l: A, r: A) extends ExpressionF[A]
  final case class AND[A](l: A, r: A) extends ExpressionF[A]
  final case class NEGATE[A](s: A) extends ExpressionF[A]
  final case class URI[A](s: A) extends ExpressionF[A]
  final case class CONCAT[A](appendTo: A, append: A) extends ExpressionF[A]
  final case class STR[A](s: A) extends ExpressionF[A]
  final case class STRAFTER[A](s: A, f: String) extends ExpressionF[A]
  final case class ISBLANK[A](s: A) extends ExpressionF[A]
  final case class REPLACE[A](st: A, pattern: A, by: A) extends ExpressionF[A]
  final case class STRING[A](s: String) extends ExpressionF[A]
  final case class NUM[A](s: String) extends ExpressionF[A]
  final case class VARIABLE[A](s: String) extends ExpressionF[A]
  final case class URIVAL[A](s: String) extends ExpressionF[A]
  final case class BLANK[A](s: String) extends ExpressionF[A]

  val fromExpressionCoalg: Coalgebra[ExpressionF, Expression] =
    Coalgebra {
      case FilterFunction.EQUALS(l, r)         => EQUALS(l, r)
      case FilterFunction.REGEX(l, r)          => REGEX(l, r)
      case FilterFunction.STRSTARTS(l, r)      => STRSTARTS(l, r)
      case FilterFunction.GT(l, r)             => GT(l, r)
      case FilterFunction.LT(l, r)             => LT(l, r)
      case FilterFunction.OR(l, r)             => OR(l, r)
      case FilterFunction.AND(l, r)            => AND(l, r)
      case FilterFunction.NEGATE(s)            => NEGATE(s)
      case StringFunc.URI(s)                   => URI(s)
      case StringFunc.CONCAT(appendTo, append) => CONCAT(appendTo, append)
      case StringFunc.STR(s)                   => STR(s)
      case StringFunc.STRAFTER(s, StringVal.STRING(f))           => STRAFTER(s, f)
      case StringFunc.ISBLANK(s)               => ISBLANK(s)
      case StringFunc.REPLACE(st, pattern, by) => REPLACE(st, pattern, by)
      case StringVal.STRING(s)                 => STRING(s)
      case StringVal.NUM(s)                    => NUM(s)
      case StringVal.VARIABLE(s)               => VARIABLE(s)
      case StringVal.URIVAL(s)                 => URIVAL(s)
      case StringVal.BLANK(s)                  => BLANK(s)
    }

  val toExpressionAlgebra: Algebra[ExpressionF, Expression] =
    Algebra {
      case EQUALS(l, r)    => FilterFunction.EQUALS(l, r)
      case REGEX(l, r)     => FilterFunction.REGEX(l, r)
      case STRSTARTS(l, r) => FilterFunction.STRSTARTS(l, r)
      case GT(l, r)        => FilterFunction.GT(l, r)
      case LT(l, r)        => FilterFunction.LT(l, r)
      case OR(l, r)        => FilterFunction.OR(l, r)
      case AND(l, r)       => FilterFunction.AND(l, r)
      case NEGATE(s)       => FilterFunction.NEGATE(s)
      case URI(s)          => StringFunc.URI(s.asInstanceOf[StringLike])
      case CONCAT(appendTo, append) =>
        StringFunc.CONCAT(
          appendTo.asInstanceOf[StringLike],
          append.asInstanceOf[StringLike]
        )
      case STR(s) => StringFunc.STR(s.asInstanceOf[StringLike])
      case STRAFTER(s, f) =>
        StringFunc.STRAFTER(
          s.asInstanceOf[StringLike],
          f.asInstanceOf[StringLike]
        )
      case ISBLANK(s) => StringFunc.ISBLANK(s.asInstanceOf[StringLike])
      case REPLACE(st, pattern, by) =>
        StringFunc.REPLACE(
          st.asInstanceOf[StringLike],
          pattern.asInstanceOf[StringLike],
          by.asInstanceOf[StringLike]
        )
      case STRING(s)   => StringVal.STRING(s)
      case NUM(s)      => StringVal.NUM(s)
      case VARIABLE(s) => StringVal.VARIABLE(s)
      case URIVAL(s)   => StringVal.URIVAL(s)
      case BLANK(s)    => StringVal.BLANK(s)
    }

  implicit val basis: Basis[ExpressionF, Expression] =
    Basis.Default[ExpressionF, Expression](
      algebra = toExpressionAlgebra,
      coalgebra = fromExpressionCoalg
    )

  def getVariable[T](t: T)(implicit T: Basis[ExpressionF, T]): Option[String] =
    t.collect[List[String], String] {
      case StringVal.VARIABLE(v) => v
    }.headOption

  def getString[T](t: T)(implicit T: Basis[ExpressionF, T]): Option[String] =
    t.collect[List[String], String] {
      case StringVal.STRING(s) => s
    }.headOption

  def compile[T](t: T)(implicit T: Basis[ExpressionF, T]): Multiset => Column = ms => {
    val algebraM: AlgebraM[State[Multiset, *], ExpressionF, Column] = AlgebraM.apply[State[Multiset, *], ExpressionF, Column] {
      case EQUALS(l, r)    => ???
      case REGEX(l, r)     => ???
      case STRSTARTS(l, r) => ???
      case GT(l, r)        => ???
      case LT(l, r)        => ???
      case OR(l, r)        => ???
      case AND(l, r)       => ???
      case NEGATE(s)       => ???

      case URI(s)                   => Func.iri(s).pure[State[Multiset, *]]
      case CONCAT(appendTo, append) => Func.concat(appendTo, append).pure[State[Multiset, *]]
      case STR(s)                   => s.pure[State[Multiset, *]]
      case STRAFTER(s, f)           => Func.strafter(s, f).pure[State[Multiset, *]]
      case ISBLANK(s)               => ???
      case REPLACE(st, pattern, by) => ???

      case STRING(s)   => lit(s).pure[State[Multiset, *]]
      case NUM(s)      => lit(s).pure[State[Multiset, *]]
      case VARIABLE(s) => State.inspect[Multiset, Column](multiset => multiset.dataframe(s))
      case URIVAL(s)   => lit(s).pure[State[Multiset, *]]
      case BLANK(s)    => lit(s).pure[State[Multiset, *]]
    }

    val eval = scheme.cataM[State[Multiset, *], ExpressionF, T, Column](algebraM)

    eval(t).runA(ms).value
  }

}
