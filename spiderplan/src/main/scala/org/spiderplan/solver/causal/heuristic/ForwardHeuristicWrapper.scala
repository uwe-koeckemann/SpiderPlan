package org.spiderplan.solver.causal.heuristic

import org.aiddl.common.scala.Common
import org.aiddl.common.scala.Common.NIL
import org.aiddl.common.scala.planning.PlanningTerm.*
import org.aiddl.common.scala.planning.state_variable.{ForwardSearchPlanIterator, NoEffectVariableFilter}
import org.aiddl.common.scala.planning.state_variable.heuristic.{CausalGraphHeuristic, FastForwardHeuristic}
import org.aiddl.core.scala.function.{Function, Initializable}
import org.aiddl.core.scala.representation.*
import org.spiderplan.solver.ResolverInstruction.AddAll
import org.spiderplan.solver.SpiderPlan.Type.*
import org.spiderplan.solver.causal.state_space.{GoalCdb2Svp, OperatorCdb2Svp, StateCdb2Svp}
import org.spiderplan.solver.temporal.TemporalConstraintSolver
import org.spiderplan.solver.{Heuristic, Resolver, SpiderPlan, SpiderPlanTreeSearch}
import org.aiddl.core.scala.representation.conversion.given_Conversion_Term_Num
import org.aiddl.core.scala.util.StopWatch

import scala.language.implicitConversions

class ForwardHeuristicWrapper[F <: Function with Initializable](svpHeuristic: F) extends Heuristic {
  val extractGoal = new GoalCdb2Svp
  val extractOperators = new OperatorCdb2Svp

  var lastSeenGoal: Term = NIL
  var lastSeenOperators: Term = NIL

  private var first = true
  var useReInit = false

  def apply( cdb: CollectionTerm ): Num = {
    val goal = extractGoal(cdb).asCol
    if ( goal.isEmpty || !goal.isGround || !cdb.containsKey(CurrentState) ) {
      Num(0)
    } else {
      val ops = {
        if (useReInit) {
          extractOperators(cdb).asCol
        } else if (first) {
          extractOperators(cdb).asCol
        } else {
          lastSeenOperators
        }
      }

      var state = cdb(CurrentState)
      val needReInit = useReInit && (lastSeenGoal != goal || lastSeenOperators != ops)

      if (first || needReInit) {
        first = false

        lastSeenGoal = goal
        lastSeenOperators = ops
        //val filter = new NoEffectVariableFilter

        val problem = SetTerm(
          KeyVal(InitialState, state),
          KeyVal(Goal, goal),
          KeyVal(Operators, ops),
        ).asCol

        //val opsFiltered = problem(Operators).asCol
        state = problem(InitialState)

        lastSeenOperators = ops

        svpHeuristic.init(problem)
      }

      //println(s"|s| ${state.length} |O| ${ops.length} |g| ${goal.length} ${state.asSet.length}")

      StopWatch.start("HC")
      val h = svpHeuristic.apply(state).asNum
      StopWatch.stop("HC")
      h
    }
  }
}
