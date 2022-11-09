package org.spiderplan.solver.causal.state_space

import org.aiddl.core.scala.function.Function
import org.aiddl.core.scala.representation.*
import org.aiddl.common.scala.planning.PlanningTerm.*
import org.aiddl.common.scala.reasoning.temporal.Timepoint
import org.spiderplan.solver.SpiderPlan
import org.spiderplan.solver.temporal.TemporalConstraintSolver

class StateCdb2Svp extends Function {

  var intervalMap: Map[Term, Term] = Map.empty

  val propagateTime = new TemporalConstraintSolver

  override def apply(x: Term): Term = {
    val propVals = x.asCol.getOrPanic(SpiderPlan.Type.PropagatedValue).asCol
    val statements = x.asCol(SpiderPlan.Type.Statement).asCol

    def est = Timepoint.Est(propVals) _

    var lastChange: Map[Term, Term] = Map()

    statements.toList.sortBy( s => {
      est(s(0))
    } ).foreach(
      s => {
        val svx = s(1).asKvp
        lastChange = lastChange.updated(svx.key, svx.value)
        intervalMap = intervalMap.updated(svx.key, s(0))
      }
    )
    SetTerm(lastChange.map((x, y) => KeyVal(x, y)).toSet)
  }

  def getIntervalMap: Map[Term, Term] = this.intervalMap

}
