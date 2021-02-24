package com.gsk.kg.engine

import higherkindness.droste.syntax.embed._
import higherkindness.droste._
import higherkindness.droste.macros.deriveTraverse
import higherkindness.droste.util.DefaultTraverse

import com.gsk.kg.sparqlparser.StringVal.VARIABLE
import com.gsk.kg.sparqlparser.StringVal

import cats.Traverse
import cats.Applicative
import cats.implicits._

sealed trait DAG[A] {

  def rewrite(
    pf: PartialFunction[DAG[A], DAG[A]]
  )(implicit A: Basis[DAG, A]): A =
    scheme
      .cata(Trans(pf.orElse(PartialFunction[DAG[A], DAG[A]] { a => a })).algebra)
      .apply(this.embed)

}

object DAG {
  final case class Describe[A](vars: List[VARIABLE], r: A) extends DAG[A]
  final case class Ask[A](r: A) extends DAG[A]
  final case class Construct[A](bgp: A, r: A) extends DAG[A]
  final case class Scan[A](graph: String, expr: A) extends DAG[A]
  final case class Project[A](variables: List[VARIABLE], r: A) extends DAG[A]
  final case class Bind[A](variable: VARIABLE, expression: ExpressionF[A], r: A)
      extends DAG[A]
  final case class Triple[A](s: StringVal, p: StringVal, o: StringVal)
      extends DAG[A]
  final case class BGP[A](triples: List[A]) extends DAG[A]
  final case class LeftJoin[A](l: A, r: A, filters: List[ExpressionF[A]])
      extends DAG[A]
  final case class Union[A](l: A, r: A) extends DAG[A]
  final case class Filter[A](funcs: List[ExpressionF[A]], expr: A)
      extends DAG[A]
  final case class Join[A](l: A, r: A) extends DAG[A]
  final case class OffsetLimit[A](
      offset: Option[Long],
      limit: Option[Long],
      r: A
  ) extends DAG[A]
  final case class Distinct[A](r: A) extends DAG[A]

  implicit val traverse: Traverse[DAG] = new DefaultTraverse[DAG] {
    def traverse[G[_]: Applicative, A, B](fa: DAG[A])(f: A => G[B]): G[DAG[B]] =
      fa match {
        case DAG.Describe(vars, r)                     => f(r).map(describe(vars, _))
        case DAG.Ask(r)                                => f(r).map(ask)
        case DAG.Construct(bgp, r)                     => (f(bgp), f(r)).mapN(construct)
        case DAG.Scan(graph, expr)                     => f(expr).map(scan(graph,_))
        case DAG.Project(variables, r)                 => f(r).map(project(variables, _))
        case DAG.Bind(variable, expression, r)         => (expression.traverse(f), f(r)).mapN(bind(variable, _, _))
        case DAG.Triple(s, p, o)                       => triple(s, p, o).pure[G]
        case DAG.BGP(triples)                          => triples.traverse(f).map(bgp)
        case DAG.LeftJoin(l, r, filters)               =>
          (
            f(l),
            f(r),
            Traverse[List].compose[ExpressionF].traverse(filters)(f)
          ).mapN(leftJoin)
        case DAG.Union(l, r)                           => (f(l), f(r)).mapN(union)
        case DAG.Filter(funcs, expr)                   =>
          (
            Traverse[List].compose[ExpressionF].traverse(funcs)(f),
            f(expr)
          ).mapN(filter)
        case DAG.Join(l, r)                            => (f(l), f(r)).mapN(join)
        case DAG.OffsetLimit(offset, limit, r)         => f(r).map(offsetLimit(offset, limit, _))
        case DAG.Distinct(r)                           => f(r).map(distinct)
      }
  }

  // Smart constructors for better type inference (they return DAG[A] instead of the case class itself)
  def describe[A](vars: List[VARIABLE], r: A): DAG[A] = Describe[A](vars, r)
  def ask[A](r: A): DAG[A] = Ask[A](r)
  def construct[A](bgp: A, r: A): DAG[A] = Construct[A](bgp, r)
  def scan[A](graph: String, expr: A): DAG[A] = Scan[A](graph, expr)
  def project[A](variables: List[VARIABLE], r: A): DAG[A] =
    Project[A](variables, r)
  def bind[A](variable: VARIABLE, expression: ExpressionF[A], r: A): DAG[A] =
    Bind[A](variable, expression, r)
  def triple[A](s: StringVal, p: StringVal, o: StringVal): DAG[A] =
    Triple[A](s, p, o)
  def bgp[A](triples: List[A]): DAG[A] = BGP[A](triples)
  def leftJoin[A](l: A, r: A, filters: List[ExpressionF[A]]): DAG[A] =
    LeftJoin[A](l, r, filters)
  def union[A](l: A, r: A): DAG[A] = Union[A](l, r)
  def filter[A](funcs: List[ExpressionF[A]], expr: A): DAG[A] =
    Filter[A](funcs, expr)
  def join[A](l: A, r: A): DAG[A] = Join[A](l, r)
  def offsetLimit[A](offset: Option[Long], limit: Option[Long], r: A): DAG[A] =
    OffsetLimit[A](offset, limit, r)
  def distinct[A](r: A): DAG[A] = Distinct[A](r)

  // Smart constructors for building the recursive version directly
  def describeR[T: Embed[DAG, *]](vars: List[VARIABLE], r: T): T =
    describe[T](vars, r).embed
  def askR[T: Embed[DAG, *]](r: T): T = ask[T](r).embed
  def constructR[T: Embed[DAG, *]](bgp: T, r: T): T = construct[T](bgp, r).embed
  def scanR[T: Embed[DAG, *]](graph: String, expr: T): T =
    scan[T](graph, expr).embed
  def projectR[T: Embed[DAG, *]](variables: List[VARIABLE], r: T): T =
    project[T](variables, r).embed
  def bindR[T: Embed[DAG, *]](
      variable: VARIABLE,
      expression: ExpressionF[T],
      r: T
  ): T = bind[T](variable, expression, r).embed
  def tripleR[T: Embed[DAG, *]](s: StringVal, p: StringVal, o: StringVal): T =
    triple[T](s, p, o).embed
  def bgpR[T: Embed[DAG, *]](triples: List[T]): T = bgp[T](triples).embed
  def leftJoinR[T: Embed[DAG, *]](
      l: T,
      r: T,
      filters: List[ExpressionF[T]]
  ): T = leftJoin[T](l, r, filters).embed
  def unionR[T: Embed[DAG, *]](l: T, r: T): T = union[T](l, r).embed
  def filterR[T: Embed[DAG, *]](funcs: List[ExpressionF[T]], expr: T): T =
    filter[T](funcs, expr).embed
  def joinR[T: Embed[DAG, *]](l: T, r: T): T = join[T](l, r).embed
  def offsetLimitR[T: Embed[DAG, *]](
      offset: Option[Long],
      limit: Option[Long],
      r: T
  ): T = offsetLimit[T](offset, limit, r).embed
  def distinctR[T: Embed[DAG, *]](r: T): T = distinct[T](r).embed


}
