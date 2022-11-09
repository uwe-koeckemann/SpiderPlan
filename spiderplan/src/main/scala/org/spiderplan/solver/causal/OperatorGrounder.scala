package org.spiderplan.solver.causal

import org.aiddl.core.scala.function.Function
import org.aiddl.core.scala.representation.*
import org.aiddl.common.scala.planning.PlanningTerm.*
import org.aiddl.common.scala.planning.state_variable.ReachableOperatorEnumerator
import org.aiddl.common.scala.reasoning.temporal.Timepoint
import org.spiderplan.solver.SpiderPlan
import org.spiderplan.solver.Solver.Propagator.Result
import org.spiderplan.solver.causal.state_space.{GoalCdb2Svp, OperatorCdb2Svp, StateCdb2Svp}
import org.spiderplan.solver.temporal.TemporalConstraintSolver

/**
 * Ground operators in the CDB by finding all reachable operators in a state-variable planning problem.
 *
 * TODO: Will only use a single initial state.
 */
class OperatorGrounder extends Function {
  override def apply(x: Term): Term = {
    var cdb = x.asCol
    val temporal = new TemporalConstraintSolver
    temporal.propagate(cdb) match {
      case Result.Consistent => ???
      case Result.Inconsistent => ???
      case Result.ConsistentWith(r) => cdb = SpiderPlan.applyResolver(cdb, r)
    }

    val conv = new OperatorCdb2Svp
    val svpOps = conv(cdb)

    val grounder = new ReachableOperatorEnumerator

    val stateConv = new StateCdb2Svp
    val svpState = stateConv(cdb)

    var svpProblem = SetTerm(
      KeyVal(InitialState, svpState),
      KeyVal(Operators, svpOps)
    )

    val groundOps = grounder(svpProblem)
    svpProblem = svpProblem.put(KeyVal(Operators, groundOps)).asSet
    val cdbOps = cdb(Sym("operator")).asCol

    val opsCdbGround = groundOps.asCol.map( o => {
      cdbOps.find(_ (Name) unifiable o(Name)) match
        case Some(oCdb) => {
          val sub = (oCdb(Name) unify o(Name)).get // Okay because we already confirmed this works with find
          oCdb \ sub
        }
        case None => throw IllegalStateException(s"No matching CDB operator found for ${o(Name)}")
    }).toSet

    x.asCol.put(KeyVal(Sym("operator"), SetTerm(opsCdbGround)))
  }

}
