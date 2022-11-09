package org.spiderplan.solver.csp

import org.aiddl.core.scala.function.{Function, Verbose}
import org.aiddl.core.scala.representation.{KeyVal, ListTerm, SetTerm, Substitution, Term}
import org.spiderplan.solver.SpiderPlan.Type.{Csp, Operator}
import org.aiddl.common.scala.planning.PlanningTerm.Name
import org.aiddl.common.scala.reasoning.constraint.ConstraintTerm.{Constraints, Variables}
import org.aiddl.common.scala.reasoning.constraint.CspSolver

import java.util.logging.Level

/**
 * Solve CSPs in operators (and ICs) as a preprocessing step and replace them will all consistent substitutions
 * of variables in the CSP
 */
class CspPreprocessor extends Function with Verbose {

  override def apply(cdb: Term): Term =
    val operators = cdb.getOrElse(Operator, SetTerm.empty).asCol

    var remList: List[Term] = Nil
    var addList: List[Term] = Nil

    operators.foreach( o => {
      if ( o(Constraints).asCol.containsKey(Csp) ) {
        val csp = CspAssembler(cdb.asCol, o(Constraints).asCol)
        this.logger.info(s"${o(Name)} has CSP $csp")

        // Solve, create and apply substitution, add o to remList and substituted operators to add list
        val solver = new CspSolver() {
          checkWithGroundArgsOnly = true
          usePropagation = false
        }
        solver.init(csp)
        var solution = solver.search

        logger.fine("Adding operators that satisfy CSP")
        logger.depth += 1
        var satOps = 0

        while { solution.isDefined } do {
          val sub = new Substitution
          solution.get.foreach(a => sub.add(a.asKvp.key, a.asKvp.value))
          val oSub = (o \ sub)
          logger.fine(oSub.toString)
          addList = oSub :: addList
          solution = solver.search
          satOps += 1
        }
        logger.depth -= 1
        logger.fine( s"All done (found $satOps operators)")


        remList = o :: remList
      }
    })
    val newOperators = operators.removeAll(ListTerm(remList)).addAll(ListTerm(addList))

    cdb.asCol.put(KeyVal(Operator, newOperators))
}
