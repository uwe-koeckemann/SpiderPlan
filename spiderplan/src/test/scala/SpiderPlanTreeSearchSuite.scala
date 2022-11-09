import org.aiddl.core.scala.container.{Container, Entry}
import org.aiddl.core.scala.parser.Parser
import org.aiddl.core.scala.util.logger.Logger
import org.aiddl.core.scala.representation.*
import org.aiddl.common.scala.Common.NIL
import org.scalatest.funsuite.AnyFunSuite
import org.spiderplan.solver.Solver.FlawResolver
import org.spiderplan.solver.Solver.Propagator
import org.spiderplan.solver.causal.psp.{OpenGoalResolverAllFlaws, OpenGoalResolverGraphSearch, OpenGoalResolverSingleFlaw}
import org.spiderplan.solver.domain.DomainConstraintSolver
import org.spiderplan.solver.resource.ReusableResourceResolver
import org.spiderplan.solver.temporal.TemporalConstraintSolver
import org.spiderplan.solver.{SpiderPlanFactory, SpiderPlanTreeSearch}

import java.util.logging.Level


class SpiderPlanTreeSearchSuite extends AnyFunSuite {
  val c = new Container()
  val parser = new Parser(c)
  val mDomain = parser.parseFile("./test/robot-move-pick-place/domain.aiddl")
  val mProblem01 = parser.parseFile("./test/robot-move-pick-place/problem-01.aiddl")
  val mProblem02 = parser.parseFile("./test/robot-move-pick-place/problem-02.aiddl")
  val mProblem03 = parser.parseFile("./test/robot-move-pick-place/problem-03.aiddl")
  val mProblem04 = parser.parseFile("./test/robot-move-pick-place/problem-04.aiddl")
  val mProblem05 = parser.parseFile("./test/robot-move-pick-place/problem-05.aiddl")

  val domainCdb = c.getProcessedValueOrPanic(mDomain, Sym("problem")).asCol

  test("SpiderPlan - Goal is reachable via single substitution") {
    val problemCdb = c.getProcessedValueOrPanic(mProblem01, Sym("problem")).asCol
    val cdb = domainCdb.putAll(problemCdb)
    val spiderPlan = SpiderPlanFactory.fullTreeSearch(cdb, Level.WARNING)

    val answer = spiderPlan.solve(cdb)
    assert(answer != NIL)
  }

  test("SpiderPlan - Robot moves one time to reach goal") {
    val problemCdb = c.getProcessedValueOrPanic(mProblem02, Sym("problem")).asCol
    val cdb = domainCdb.putAll(problemCdb)
    val spiderPlan = SpiderPlanFactory.fullTreeSearch(cdb, Level.WARNING)

    val answer = spiderPlan.solve(cdb)
    assert(answer != NIL )
  }

   test("SpiderPlan - Robot picks up object") {
     val problemCdb = c.getProcessedValueOrPanic(mProblem03, Sym("problem")).asCol
     val cdb = domainCdb.putAll(problemCdb)
     val spiderPlan = SpiderPlanFactory.fullTreeSearch(cdb, Level.WARNING)

     val answer = spiderPlan.solve(cdb)
     assert(answer != NIL )
   }

  test("SpiderPlan - Robot picks up object, moves, and places it at new location") {
    val problemCdb = c.getProcessedValueOrPanic(mProblem04, Sym("problem")).asCol
    val cdb = domainCdb.putAll(problemCdb)
    val spiderPlan = SpiderPlanFactory.fullTreeSearch(cdb, Level.WARNING)

    val answer = spiderPlan.solve(cdb)
    assert(answer != NIL )
  }


  test("SpiderPlan - Robot moves, picks up object, moves, and places it at starting location") {
    val problemCdb = c.getProcessedValueOrPanic(mProblem05, Sym("problem")).asCol
    val cdb = domainCdb.putAll(problemCdb)
    val spiderPlan = SpiderPlanFactory.fullTreeSearch(cdb, Level.WARNING)

    val answer = spiderPlan.solve(cdb)
    assert(answer != NIL )
  }
}