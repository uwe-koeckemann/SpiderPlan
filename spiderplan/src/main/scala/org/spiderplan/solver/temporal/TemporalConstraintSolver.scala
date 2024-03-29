package org.spiderplan.solver.temporal

import org.aiddl.common.scala.Common
import org.aiddl.common.scala.Common.NIL
import org.aiddl.common.scala.reasoning.resource.{FlexibilityOrdering, LinearMcsSampler}
import org.aiddl.common.scala.reasoning.temporal.Timepoint.{ET, ST}
import org.aiddl.common.scala.reasoning.temporal.{AllenInterval2Stp, MinimalInconsistentStpFinder, StpSolver}
import org.aiddl.core.scala.util.StopWatch
import org.aiddl.core.scala.function.{Function, Verbose}
import org.aiddl.core.scala.representation.*
import org.spiderplan.solver.ResolverInstruction.*
import org.spiderplan.solver.Solver.Propagator
import org.spiderplan.solver.Solver.Propagator.Result
import org.spiderplan.solver.SpiderPlan.Type.{PropagatedValue, Temporal}
import org.spiderplan.solver.{Explainable, Resolver, SpiderPlan}

import scala.collection.{immutable, mutable}

class TemporalConstraintSolver extends Propagator with Function with Explainable {
  val ac2stp = new AllenInterval2Stp
  val stpSolver = new StpSolver

  override def apply(cdb: Term): Term =
    this.propagate(cdb.asCol) match {
      case Result.Consistent => cdb
      case Result.Inconsistent => Common.NIL //cdb
      case Result.ConsistentWith(r) => SpiderPlan.applyResolver(cdb.asCol, r)
    }

  override def propagate(cdb: CollectionTerm): Propagator.Result =
    val acs = cdb.getOrElse(Temporal, SetTerm.empty)
    val misc = cdb.getOrElse(Sym("temporal.misc"), SetTerm.empty).asCol

    val stp = ac2stp(acs)
    val r = stpSolver(stp)

    r match {
      case NIL => Propagator.Result.Inconsistent
      case propVals => {
        val miscCon = misc.forall( c => {
          c match {
            case Tuple(Sym("intersection-possible"), intervals: CollectionTerm) => {
              val maxEst = intervals.map( i => propVals(Tuple(ST, i))(0).asNum ).max
              val minEet = intervals.map( i => propVals(Tuple(ET, i))(0).asNum ).min
              maxEst < minEet
            }
            case _ => throw new IllegalArgumentException(s"Unsupported constraint for temporal.mist: ${c}")
          }
        })
        if ( miscCon )
          Propagator.Result.ConsistentWith(Resolver(List(PutAll(PropagatedValue, propVals.asCol)), Some(() => "Temporal propagation")))
        else {
          Propagator.Result.Inconsistent
        }
      }
    }

  override def explain(cdb: CollectionTerm): String = {
    val acs = cdb.getOrElse(Temporal, SetTerm.empty)
    val stp = ac2stp(acs)
    val minInconsistentFinder = new MinimalInconsistentStpFinder
    val minStpCs = minInconsistentFinder(stp)
    minStpCs.toString
  }
}