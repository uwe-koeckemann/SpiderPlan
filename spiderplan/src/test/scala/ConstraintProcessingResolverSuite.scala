import org.aiddl.common.scala.Common.NIL
import org.aiddl.core.scala.container.{Container, Entry}
import org.aiddl.core.scala.parser.Parser
import org.aiddl.core.scala.representation.*
import org.scalatest.funsuite.AnyFunSuite
import org.spiderplan.solver.csp.*
import org.spiderplan.solver.Solver.{FlawResolver, Propagator}
import org.spiderplan.solver.SpiderPlan.Type.*
import org.spiderplan.solver.causal.heuristic.HAddReuse
import org.spiderplan.solver.causal.psp.OpenGoalResolverGraphSearch
import org.spiderplan.solver.domain.DomainConstraintSolver
import org.spiderplan.solver.temporal.TemporalConstraintSolver
import org.spiderplan.solver.{Heuristic, SpiderPlan, SpiderPlanFactory, SpiderPlanGraphSearch, SpiderPlanTreeSearch}

import java.util.logging.Level

class ConstraintProcessingResolverSuite extends AnyFunSuite {
  val c = new Container()
  val parser = Parser(c)
  val mDomain = parser.parseFile("./test/planning-with-constraints/domain.aiddl")
  val mProblem01 = parser.parseFile("./test/planning-with-constraints/problem-01.aiddl")
  val mProblem02 = parser.parseFile("./test/planning-with-constraints/problem-02.aiddl")

  val domainCdb = c.getProcessedValueOrPanic(mDomain, Sym("problem")).asCol

  test("CSP on movement 01") {
    val problemCdb = c.getProcessedValueOrPanic(mProblem01, Sym("problem")).asCol
    val cdb = domainCdb.putAll(problemCdb).asCol
    val f = c.getFunction(Sym("#lambda_0"))


    val spiderPlan = SpiderPlanFactory.fullGraphSearch(cdb, Level.WARNING)

    val answer = spiderPlan.solve(cdb)

    assert(spiderPlan solve cdb match {
      case None => false
      case Some(a) => true
    })
  }

  test("CSP on movement 02") {
    val problemCdb = c.getProcessedValueOrPanic(mProblem02, Sym("problem")).asCol
    val cdb = domainCdb.putAll(problemCdb).asCol
    val f = c.getFunction(Sym("#lambda_0"))

    val spiderPlan = SpiderPlanFactory.fullGraphSearch(cdb, Level.WARNING)
    val answer = spiderPlan.solve(cdb)

    assert(spiderPlan solve cdb match {
      case None => false
      case Some(a) => true
    })
  }

  test("Constraint processing solved as part of CDB") {
    val c = new Container()
    val parser = Parser(c)
    val mDomain = parser.parseFile("./test/planning-n-queens/domain.aiddl")
    val cdb = c.getProcessedValueOrPanic(mDomain, Sym("problem")).asCol
    val spiderPlan = SpiderPlanFactory.fullGraphSearch(cdb, Level.WARNING)

    assert(spiderPlan solve cdb match {
      case None => false
      case Some(a) => true
    })
  }
}