package org.spiderplan.solver.causal

import org.aiddl.common.scala.planning.PlanningTerm.*
import org.aiddl.common.scala.planning.state_variable.ReachableOperatorEnumerator
import org.aiddl.common.scala.reasoning.temporal.Timepoint
import org.aiddl.core.scala.function.Function
import org.aiddl.core.scala.representation.*
import org.aiddl.core.scala.util.ComboIterator
import org.spiderplan.solver.Solver.Propagator.Result
import org.spiderplan.solver.SpiderPlan
import org.spiderplan.solver.SpiderPlan.Type.{Domain, Operator, Signature, Statement}
import org.spiderplan.solver.causal.state_space.{GoalCdb2Svp, OperatorCdb2Svp, StateCdb2Svp}
import org.spiderplan.solver.temporal.TemporalConstraintSolver

/**
 * Ground operators in the CDB enumerating all possible values for their parameters.
 *
 */
class OperatorGrounderFull extends Function {
  override def apply(x: Term): Term = {
    var cdb = x.asCol

    val domains = cdb.getOrElse(Domain, SetTerm.empty).asCol

    val opsCdbGround = SetTerm(cdb.getOrElse(Operator, SetTerm.empty).asCol.flatMap( o => {
      val sig = o(Signature).asList
      val choices = sig.map( s => domains(s.asKvp.value).asCol.map( v => KeyVal(s.asKvp.key, v) ).toSeq ).toList
      val comboIterator = new ComboIterator[Term](choices)

      comboIterator.map( combo => {
        val s = Substitution.from(ListTerm(combo.toList))
        o \ s
      }).toSeq
    }).toSet)

    x.asCol.put(KeyVal(Sym("operator"), opsCdbGround))
  }

}
