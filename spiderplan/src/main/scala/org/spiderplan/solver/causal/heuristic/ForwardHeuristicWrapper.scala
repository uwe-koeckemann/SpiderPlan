package org.spiderplan.solver.causal.heuristic

import org.aiddl.common.scala.Common
import org.aiddl.common.scala.Common.NIL
import org.aiddl.common.scala.planning.PlanningTerm.*
import org.aiddl.common.scala.planning.state_variable.heuristic.CausalGraphHeuristic
import org.aiddl.core.scala.function.{Function, Initializable}
import org.aiddl.core.scala.representation.*
import org.spiderplan.solver.ResolverInstruction.AddAll
import org.spiderplan.solver.SpiderPlan.Type.*
import org.spiderplan.solver.causal.state_space.{GoalCdb2Svp, OperatorCdb2Svp, StateCdb2Svp}
import org.spiderplan.solver.temporal.TemporalConstraintSolver
import org.spiderplan.solver.{Heuristic, Resolver, SpiderPlan, SpiderPlanTreeSearch}

import org.aiddl.core.scala.representation.conversion.given_Conversion_Term_Num

import scala.language.implicitConversions

class ForwardHeuristicWrapper[F <: Function with Initializable](svpHeuristic: F) extends Heuristic {
  val propagateTime = new TemporalConstraintSolver
  val extractState = new StateCdb2Svp
  val extractGoal = new GoalCdb2Svp
  val extractOperators = new OperatorCdb2Svp

  var lastSeenGoal: Term = NIL
  var lastSeenOperators: Term = NIL

  def apply( x: CollectionTerm ): Num = {
    val cdb = propagateTime(x)
    val goal = extractGoal(cdb).asCol
    if ( goal.isEmpty || !goal.isGround ) {
      Num(0)
    } else {
      val ops = extractOperators(cdb).asCol
      val state = extractState(cdb).asCol

      val needReInit = {
        lastSeenGoal != goal || lastSeenOperators != ops
      }
      if (needReInit) {
        lastSeenGoal = goal
        lastSeenOperators = ops

        val problem = SetTerm(
          KeyVal(InitialState, state),
          KeyVal(Goal, goal),
          KeyVal(Operators, ops),
        )
        svpHeuristic.init(problem)
      }

      svpHeuristic.apply(state).asNum
    }
  }
}
