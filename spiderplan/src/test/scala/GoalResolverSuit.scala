import org.aiddl.common.scala.Common.NIL
import org.aiddl.core.java.tools.StopWatch
import org.aiddl.core.scala.container.{Container, Entry}
import org.aiddl.core.scala.parser.Parser
import org.aiddl.core.scala.representation.*
import org.aiddl.core.scala.util.logger.Logger
import org.scalatest.funsuite.AnyFunSuite
import org.spiderplan.solver.csp.*
import org.spiderplan.solver.Solver.FlawResolver.Result.Search
import org.spiderplan.solver.Solver.Propagator.Result
import org.spiderplan.solver.Solver.{FlawResolver, Propagator}
import org.spiderplan.solver.SpiderPlan.Type.*
import org.spiderplan.solver.{Heuristic, Solver, SpiderPlan, SpiderPlanFactory, SpiderPlanGraphSearch, SpiderPlanTreeSearch}
import org.aiddl.common.scala.planning.PlanningTerm.*
import org.aiddl.common.scala.planning.state_variable.ReachableOperatorEnumerator
import org.aiddl.common.scala.planning.state_variable.data.RelaxedPlanningGraphCreator
import org.spiderplan.solver.causal.{OperatorGrounder, OperatorGrounderFull}
import org.spiderplan.solver.causal.heuristic.HAddReuse
import org.spiderplan.solver.causal.psp.OpenGoalResolverGraphSearch
import org.spiderplan.solver.causal.state_space.{GoalCdb2Svp, OperatorCdb2Svp, StateCdb2Svp}
import org.spiderplan.solver.domain.DomainConstraintSolver
import org.spiderplan.solver.temporal.TemporalConstraintSolver

import java.util.logging.Level

class GoalResolverSuit extends AnyFunSuite {
  val c = new Container()
  val parser = new Parser(c)
  val mDomain = parser.parseFile("./test/goal/elevator/domain.aiddl")
  val domainCdb = c.getProcessedValueOrPanic(mDomain, Sym("problem")).asCol

  /*test("Elevator - Converting to SVP") {
    val mProblem = Parser.parseInto("./test/goal/elevator/problem-01.aiddl", c)
    val problemCdb = c.getProcessedValueOrPanic(mProblem, Sym("problem")).asCol
    var cdb = domainCdb.putAll(problemCdb)

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

    val goalConv = new GoalCdb2Svp
    val svpGoal = goalConv(cdb)

    var svpProblem = SetTerm(
      KeyVal(InitialState, svpState),
      KeyVal(Goal, svpGoal),
      KeyVal(Operators, svpOps)
    )

    val groundOps = grounder(svpProblem)
    svpProblem = svpProblem.put(KeyVal(Operators, groundOps)).asSet

    val rpgGen = new RelaxedPlanningGraphCreator

    val rpg = rpgGen(svpProblem)
  }*/

  test("Elevator + GraphSearch - Move two passengers") {
    val mProblem = parser.parseFile("./test/goal/elevator/problem-01.aiddl")
    val problemCdb = c.getProcessedValueOrPanic(mProblem, Sym("problem")).asCol
    var cdb = domainCdb.putAll(problemCdb)

    val spiderPlan = SpiderPlanFactory.fullGraphSearch(cdb, Level.WARNING)
    val answer = spiderPlan.solve(cdb)

    assert(answer match {
      case Some(cdb) => true
      case None => false
    })
  }

  test("Elevator + TreeSearch - Move two passengers") {
    val mProblem = parser.parseFile("./test/goal/elevator/problem-01.aiddl")
    val problemCdb = c.getProcessedValueOrPanic(mProblem, Sym("problem")).asCol
    var cdb = domainCdb.putAll(problemCdb)

    val spiderPlan = SpiderPlanFactory.fullTreeSearch(cdb, Level.WARNING)
    val answer = spiderPlan.solve(cdb)

    assert(answer match {
      case Some(cdb) => true
      case None => false
    })
  }
}

