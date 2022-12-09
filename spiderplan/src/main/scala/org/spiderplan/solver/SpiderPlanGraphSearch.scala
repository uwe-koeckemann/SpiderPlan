package org.spiderplan.solver

import org.aiddl.common.scala.Common.NIL
import org.aiddl.common.scala.search.GenericGraphSearch
import org.aiddl.core.scala.representation.CollectionTerm
import org.aiddl.core.java.tools.StopWatch
import org.aiddl.core.scala.function.{Function, Verbose}
import org.aiddl.core.scala.representation.*
import org.aiddl.core.scala.util.logger.Logger
import org.spiderplan.solver.Solver.Propagator
import org.spiderplan.solver.Solver.FlawResolver
import org.spiderplan.solver.ResolverInstruction.*

import org.aiddl.core.scala.representation.conversion.given_Conversion_Term_Num

import scala.language.implicitConversions

trait SpiderPlanGraphSearch(heuristics: Vector[(Heuristic, Num)]) extends GenericGraphSearch[Resolver, CollectionTerm] with SpiderPlan {
  val preprocessors: Vector[Function]
  val solvers: Vector[FlawResolver]
  val propagators: Vector[Propagator]
  //val heuristics: Vector[(Heuristic, Num)]

  this.heuristics.foreach((_, o) => super.addHeuristic(o))

  override def logGetVerboseSubComponents: List[Verbose] = {
    (preprocessors.withFilter(_.isInstanceOf[Verbose]).map(_.asInstanceOf[Verbose])
      ++ solvers.withFilter(_.isInstanceOf[Verbose]).map(_.asInstanceOf[Verbose])
      ++ propagators.withFilter(_.isInstanceOf[Verbose]).map(_.asInstanceOf[Verbose])
      ++ heuristics.withFilter(_.isInstanceOf[Verbose]).map(_.asInstanceOf[Verbose])).toList
  }

  override def solve( cdb: CollectionTerm): Option[CollectionTerm] =
    val preCdb = preprocessors.foldLeft(cdb: Term)( (c, f) => {
      StopWatch.start(s"[Preprocessor] ${this.nameOf(f)}")
      val r = f(c)
      StopWatch.stop(s"[Preprocessor] ${this.nameOf(f)}")
      r
    }).asCol
    this.init(List(preCdb))
    search.flatMap( rs => Some(SpiderPlan.applyResolverSeq(preCdb, rs)))

  override def propagate(n: CollectionTerm): Option[(CollectionTerm, Option[Resolver])] = {
    assert(currentNode == n)
    propagationEdge match {
      case Some((edge, cdb)) =>
        if ( edge.isEmpty ) {
          Some((n, None))
        } else {
          if ( n == cdb ) Some(n, None)
          else Some((cdb, Some(edge)))
        }
      case None => None
    }
  }

  override def expand(n: CollectionTerm): Seq[(Resolver, CollectionTerm)] = {
    flawResolvers match {
      case None => Nil
      case Some(resolvers) =>
        resolvers.map( r => (r, SpiderPlan.applyResolver(n, r)) )
    }
  }

  var currentNode: CollectionTerm = _
  var propagationEdge: Option[(Resolver, CollectionTerm)] = None
  var flawResolvers: Option[Seq[Resolver]] = None
  logger.name = this.getClass.getSimpleName


  private def nameOf( p: Any ): String = {
    if (p.isInstanceOf[Verbose]) p.asInstanceOf[Verbose].logName else p.getClass.getSimpleName
  }

  override def h(i: Int, n: CollectionTerm): Num = this.heuristics(i)._1(n)

  override def isGoal(n: CollectionTerm): Boolean = {
    // isGoal is called before expand and propagate and requires their results
    // so we do everything here and just look-up results later
    // this should work because we never ask for expansion or propagation of a node that
    // has not been checked to be a goal first
    currentNode = n
    propagationEdge = None
    flawResolvers = None

    var propChanges: Resolver = Resolver(Nil, Some(() => ""))
    var cdb = n
    var propList: List[Propagator.Result] = Nil

    val pCon = this.propagators.forall( p => {
      StopWatch.start(s"[Propagator] ${this.nameOf(p)}")
      val r = p.propagate(cdb)
      StopWatch.stop(s"[Propagator] ${this.nameOf(p)}")
      propList = r :: propList
      r match {
        case Propagator.Result.Consistent => true
        case Propagator.Result.Inconsistent => {
          logger.info(s"Rejected by propagator: ${this.nameOf(p)}")
          false
        }
        case Propagator.Result.ConsistentWith(changes) =>
          cdb = SpiderPlan.applyResolver(cdb, changes)
          propChanges = Resolver(propChanges ++ changes, Some(() => "Propagation"))
          true
      }
    })

    if ( pCon ) {
      var isGoal = true
      var resolverList: List[Resolver] = Nil
      var fCon = true

      this.solvers.forall( s => {
        StopWatch.start(s"[Solver] ${this.nameOf(s)}")
        val r = s.resolve(cdb)
        StopWatch.stop(s"[Solver] ${this.nameOf(s)}")
        r match {
          case FlawResolver.Result.Consistent => {
            logger.info(s"Accepted by resolver: ${this.nameOf(s)}")
            fCon = true
            true
          }
          case FlawResolver.Result.Inconsistent => {
            logger.info(s"Rejected by resolver: ${this.nameOf(s)}")
            isGoal = false
            fCon = false
            false
          }
          case FlawResolver.Result.Search(resolvers) => {
            logger.info(s"Expanded by resolver: ${this.nameOf(s)}")
            resolverList = resolverList ++ resolvers
            isGoal = false
            fCon = true
            false
          }
        }
      })
      if ( fCon ) {
        propagationEdge = Some((propChanges, cdb))
        flawResolvers = Some(resolverList)
      }
      isGoal
    } else {
      false
    }
  }
}
