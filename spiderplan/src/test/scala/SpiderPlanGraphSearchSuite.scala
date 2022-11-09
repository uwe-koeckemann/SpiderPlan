import org.aiddl.common.scala.Common.NIL
import org.aiddl.common.scala.math.graph.Graph2Dot
import org.aiddl.common.scala.math.graph.GraphType
import org.aiddl.core.java.tools.StopWatch
import org.aiddl.core.scala.container.{Container, Entry}
import org.aiddl.core.scala.parser.Parser
import org.aiddl.core.scala.representation.*
import org.aiddl.core.scala.util.logger.Logger
import org.scalatest.funsuite.AnyFunSuite
import org.spiderplan.solver.csp.*
import org.spiderplan.solver.{Heuristic, SpiderPlan, SpiderPlanFactory, SpiderPlanGraphSearch}
import org.spiderplan.solver.Solver.Propagator
import org.spiderplan.solver.Solver.FlawResolver
import org.spiderplan.solver.causal.heuristic.HAddReuse
import org.spiderplan.solver.causal.psp.OpenGoalResolverGraphSearch
import org.spiderplan.solver.domain.DomainConstraintSolver
import org.spiderplan.solver.temporal.TemporalConstraintSolver

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import java.util.logging.Level

class SpiderPlanGraphSearchSuite extends AnyFunSuite {
  val c = new Container()
  val parser = new Parser(c)
  val mDomain = parser.parseFile("./test/robot-move-pick-place/domain.aiddl")
  val mProblem01 = parser.parseFile("./test/robot-move-pick-place/problem-01.aiddl")
  val mProblem02 = parser.parseFile("./test/robot-move-pick-place/problem-02.aiddl")
  val mProblem03 = parser.parseFile("./test/robot-move-pick-place/problem-03.aiddl")
  val mProblem04 = parser.parseFile("./test/robot-move-pick-place/problem-04.aiddl")
  val mProblem05 = parser.parseFile("./test/robot-move-pick-place/problem-05.aiddl")
  val mProblem06 = parser.parseFile("./test/robot-move-pick-place/problem-06.aiddl")
  val mProblem07 = parser.parseFile("./test/robot-move-pick-place/problem-07.aiddl")

  val domainCdb = c.getProcessedValueOrPanic(mDomain, Sym("problem")).asCol

  test("Goal is reachable via single substitution") {
    val problemCdb = c.getProcessedValueOrPanic(mProblem01, Sym("problem")).asCol
    val cdb = domainCdb.putAll(problemCdb)
    val spiderPlan = SpiderPlanFactory.fullGraphSearch(cdb, Level.WARNING)
    val answer = spiderPlan.solve(cdb)
    assert(answer != None )
  }

   test("Robot moves one time to reach goal") {
     val problemCdb = c.getProcessedValueOrPanic(mProblem02, Sym("problem")).asCol
     val cdb = domainCdb.putAll(problemCdb)
     val spiderPlan = SpiderPlanFactory.fullGraphSearch(cdb, Level.WARNING)
     val answer = spiderPlan.solve(cdb)
     assert(answer != None )
   }

   test("Robot picks up object") {
     val problemCdb = c.getProcessedValueOrPanic(mProblem03, Sym("problem")).asCol
     val cdb = domainCdb.putAll(problemCdb)
     val spiderPlan = SpiderPlanFactory.fullGraphSearch(cdb, Level.WARNING)
     val answer = spiderPlan.solve(cdb)
     assert(answer != None )
   }


  test("Robot picks up object, moves, and places it at new location") {
    val problemCdb = c.getProcessedValueOrPanic(mProblem04, Sym("problem")).asCol
    val cdb = domainCdb.putAll(problemCdb)
    val spiderPlan = SpiderPlanFactory.fullGraphSearch(cdb, Level.WARNING)
    val answer = spiderPlan.solve(cdb)
    assert(answer != None )
  }


  test("Robot moves, picks up object, moves, and places it at starting location") {
    val problemCdb = c.getProcessedValueOrPanic(mProblem05, Sym("problem")).asCol
    val cdb = domainCdb.putAll(problemCdb)
    val spiderPlan = SpiderPlanFactory.fullGraphSearch(cdb, Level.WARNING)
    val answer = spiderPlan.solve(cdb)
    assert(answer != None )
  }

  test("Robot moves two times") {
    val problemCdb = c.getProcessedValueOrPanic(mProblem06, Sym("problem")).asCol
    val cdb = domainCdb.putAll(problemCdb)
    val spiderPlan = SpiderPlanFactory.fullGraphSearch(cdb, Level.WARNING)
    val answer = spiderPlan.solve(cdb)
    assert(answer != None )
  }

  test("Fetching two objects with two robots") {
    val problemCdb = c.getProcessedValueOrPanic(mProblem07, Sym("problem")).asCol
    val cdb = domainCdb.putAll(problemCdb)
    val spiderPlan = SpiderPlanFactory.fullGraphSearch(cdb, Level.WARNING)
    val answer = spiderPlan.solve(cdb)
    assert(answer != None)
  }

}