package org.spiderplan.solver

import org.aiddl.core.scala.function.{Function, Verbose}
import org.aiddl.core.scala.representation.*
import org.aiddl.common.scala.Common.NIL
import org.aiddl.core.scala.util.logger.Logger
import org.spiderplan.solver.SpiderPlan
import org.spiderplan.solver.Solver.Propagator
import org.spiderplan.solver.Solver.FlawResolver

import org.spiderplan.solver.ResolverInstruction.*

trait SpiderPlanTreeSearch extends Function with Verbose with SpiderPlan {
  var generateAllSolutions = false

  val preprocessors: Vector[Function]
  val propagators: Vector[Propagator]
  val solvers: Vector[FlawResolver]

  logger.name = this.getClass.getSimpleName

  override def logGetVerboseSubComponents: List[Verbose] = {
    (preprocessors.withFilter(_.isInstanceOf[Verbose]).map(_.asInstanceOf[Verbose])
      ++ solvers.withFilter(_.isInstanceOf[Verbose]).map(_.asInstanceOf[Verbose])
      ++ propagators.withFilter(_.isInstanceOf[Verbose]).map(_.asInstanceOf[Verbose])).toList
  }

  override def solve(cdb: CollectionTerm): Option[CollectionTerm] =
    val preCdb = preprocessors.foldLeft(cdb: Term)( (c, f) => f(c)).asCol
    this(preCdb) match {
      case cdb: CollectionTerm => Some(cdb)
      case _ => None
    }

  def apply(args: Term): Term = {
    var isConsistent = true

    var backtrackStack: List[Iterator[Resolver]] = Nil
    var coreStack: List[CollectionTerm] = Nil
    var currentCore = args.asCol
    var skipToNextResolver = false
    var done = false

    while { !done } do {
      var i = 0
      while { !done && i < (propagators.size + solvers.size) } do {
        var needResolver = false
        if ( !skipToNextResolver ) {
          if ( i < propagators.size ) {
            logger.info(s"Propagator $i: ${propagators(i).getClass.getSimpleName}")
            logger.depth += 1
            val result = this.propagators(i).propagate(currentCore)
            result match {
              case Propagator.Result.Consistent =>
                this.logger.info(s"Result  $i: ${propagators(i).getClass.getSimpleName} -> Consistent")
                i += 1
              case Propagator.Result.ConsistentWith(changes) =>
                this.logger.info(s"Result  $i: ${propagators(i).getClass.getSimpleName} -> Consistent (with change)")
                currentCore = SpiderPlan.applyResolver(currentCore, changes)
                i += 1
              case Propagator.Result.Inconsistent =>
                this.logger.info(s"Result  $i: ${propagators(i).getClass.getSimpleName} -> Inconsistent")
                needResolver = true
            }
          } else {
            val solverIdx = i - propagators.size
            logger.info(s"Solver $solverIdx: ${solvers(solverIdx).getClass.getSimpleName}")
            logger.depth += 1
            val result = this.solvers(solverIdx).resolve(currentCore)
            logger.depth -= 1

            result match {
              case FlawResolver.Result.Consistent =>
                this.logger.info(s"Result  $solverIdx: ${solvers(solverIdx).getClass.getSimpleName} -> Consistent")
                i += 1
              case FlawResolver.Result.Inconsistent =>
                this.logger.info(s"Result  $solverIdx: ${solvers(solverIdx).getClass.getSimpleName} -> Inconsistent")
                needResolver = true
              case FlawResolver.Result.Search(resolvers) =>
                this.logger.info(s"Result  $solverIdx: ${solvers(solverIdx).getClass.getSimpleName} -> Resolver required")
                coreStack = currentCore :: coreStack
                backtrackStack = resolvers.iterator :: backtrackStack
                needResolver = true
            }
          }
        } else {
          skipToNextResolver = false
        }

        if ( needResolver ) {
          this.logger.info("Choosing next resolver")
          var r: Option[Resolver] = None
          while {
            !backtrackStack.isEmpty && r == None
          } do {
            logger.info(s"trying stack level ${backtrackStack.size}")
            logger.depth += 1
            if ( backtrackStack.head.hasNext ) {
              r = Some(backtrackStack.head.next)
              currentCore = coreStack.head
            } else {
              backtrackStack = backtrackStack.tail
              coreStack = coreStack.tail
            }
            logger.depth -= 1
          }
          r match {
            case None => {
              isConsistent = false
              done = true
            }
            case Some(resolver: Resolver) => {
              currentCore = SpiderPlan.applyResolver(currentCore, resolver)
              i = 0
            }
          }
        }
      }
      this.logger.info(s"Done with i=${i} and done=$done")
      if ( !generateAllSolutions ) { done = true }
      else if ( !isConsistent ) { done = true }
      else { skipToNextResolver = true }
    }
    if ( isConsistent ) currentCore
    else NIL
  }
}
