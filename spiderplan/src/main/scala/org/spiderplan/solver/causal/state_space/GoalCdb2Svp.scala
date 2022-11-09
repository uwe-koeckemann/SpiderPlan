package org.spiderplan.solver.causal.state_space

import org.aiddl.core.scala.function.Function
import org.aiddl.core.scala.representation.*
import org.aiddl.common.scala.planning.PlanningTerm.*
import org.aiddl.common.scala.reasoning.temporal.Timepoint
import org.spiderplan.solver.SpiderPlan
import org.spiderplan.solver.Solver.Propagator.Result
import org.spiderplan.solver.SpiderPlan.Type.{ClosedGoal, OpenGoal}
import org.spiderplan.solver.temporal.TemporalConstraintSolver

class GoalCdb2Svp extends Function {

  var intervalMap: Map[Term, Term] = Map.empty

  override def apply(x: Term): Term = {
    val satGoals = x.getOrElse(ClosedGoal, SetTerm.empty).asCol
    val openGoals = x.getOrElse(OpenGoal, SetTerm.empty).asCol.filter( g => !(satGoals contains g(0)) )
    SetTerm(openGoals.map( g => {
      intervalMap = intervalMap.updated(g(1), g(0))
      g(1)
    } ).toSet)
  }

  def getGoalInterval(goal: Term): Term = this.intervalMap(goal)
}
