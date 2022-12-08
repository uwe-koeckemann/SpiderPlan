package org.spiderplan.solver.causal

import org.aiddl.core.scala.function.{Function, Verbose}
import org.aiddl.core.scala.representation.*
import org.aiddl.core.scala.util.logger.Logger
import org.aiddl.common.scala.planning.PlanningTerm.*
import org.aiddl.common.scala.planning.state_variable.ReachableOperatorEnumerator
import org.aiddl.common.scala.planning.state_variable.UnboundEffectGrounder
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
class OperatorGrounder extends Function with Verbose {
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

    logger.info(s"State used for grounding: ${Logger.prettyPrint(svpState, 2)}")
    logger.info(s"Operators used for grounding: ${Logger.prettyPrint(svpOps, 2)}")


    var svpProblem = SetTerm(
      KeyVal(Domains, cdb(Sym("domain"))),
      KeyVal(InitialState, svpState),
      KeyVal(Operators, svpOps)
    )

    val upGrounder = new UnboundEffectGrounder()
    svpProblem = upGrounder(svpProblem).asSet

    val groundOps = grounder(svpProblem)
    svpProblem = svpProblem.put(KeyVal(Operators, groundOps)).asSet

    logger.info("Ground operators (SVP): ")
    logger.info(s"${Logger.prettyPrint(groundOps, 2)}")


    val cdbOps = cdb(Sym("operator")).asCol

    logger.info("Ground operators: ")
    val opsCdbGround = groundOps.asCol.map( o => {
      cdbOps.find(_ (Name) unifiable o(Name)) match
        case Some(oCdb) => {
          val sub = (oCdb(Name) unify o(Name)).get // Okay because we already confirmed this works with find
          logger.info(s"${oCdb(Name) \ sub}")
          oCdb \ sub
        }
        case None => throw IllegalStateException(s"No matching CDB operator found for ${o(Name)}")
    }).toSet

    x.asCol.put(KeyVal(Sym("operator"), SetTerm(opsCdbGround)))
  }

}
