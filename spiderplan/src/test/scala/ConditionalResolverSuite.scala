import org.aiddl.common.scala.Common.NIL
import org.aiddl.core.scala.container.{Container, Entry}
import org.aiddl.core.scala.parser.Parser
import org.aiddl.core.scala.representation.*
import org.aiddl.core.scala.util.logger.Logger
import org.scalatest.funsuite.AnyFunSuite
import org.spiderplan.solver.csp.*
import org.spiderplan.solver.Solver.{FlawResolver, Propagator}
import org.spiderplan.solver.{Heuristic, SpiderPlan, SpiderPlanFactory, SpiderPlanGraphSearch, SpiderPlanTreeSearch}
import org.spiderplan.solver.SpiderPlan.Type.*
import org.spiderplan.solver.causal.heuristic.HAddReuse
import org.spiderplan.solver.causal.psp.OpenGoalResolverGraphSearch
import org.spiderplan.solver.domain.DomainConstraintSolver
import org.spiderplan.solver.temporal.TemporalConstraintSolver

import java.util.logging.Level

class ConditionalResolverSuite extends AnyFunSuite {
  val c = new Container()
  val parser = new Parser(c)
  val mDomain = parser.parseFile("./test/human-aware-logistics/domain.aiddl")
  val mProblem01 = parser.parseFile("./test/human-aware-logistics/problem-01.aiddl")
  val mProblem02 = parser.parseFile("./test/human-aware-logistics/problem-02.aiddl")
  val mProblem03 = parser.parseFile("./test/human-aware-logistics/problem-03.aiddl")

  val domainCdb = c.getProcessedValueOrPanic(mDomain, Sym("problem")).asCol

  test("Conditional constraint does not apply because of temporal condition") {
    val problemCdb = c.getProcessedValueOrPanic(mProblem01, Sym("problem")).asCol
    val cdb = domainCdb.putAll(problemCdb)
    val spiderPlan = SpiderPlanFactory.fullTreeSearch(cdb, Level.WARNING)

    val answer = spiderPlan.solve(cdb)

    assert(answer match {
      case Some(cdbSolved) =>
        cdbSolved(Statement).asCol.length == cdb(Statement).asCol.length
          && cdbSolved(Temporal).asCol.length == cdb(Temporal).asCol.length
      case None => false
    })
  }

  test("Conditional constraint does not apply because of mismatch") {
    val problemCdb = c.getProcessedValueOrPanic(mProblem02, Sym("problem")).asCol
    val cdb = domainCdb.putAll(problemCdb)
    val spiderPlan = SpiderPlanFactory.fullTreeSearch(cdb, Level.WARNING)

    val answer = spiderPlan.solve(cdb)

    assert(answer match {
      case Some(cdbSolved) =>
        cdbSolved(Statement).asCol.length == cdb(Statement).asCol.length
          && cdbSolved(Temporal).asCol.length == cdb(Temporal).asCol.length
      case None => false
    })
  }

  test("Conditional constraint applies and is resolved") {
    val problemCdb = c.getProcessedValueOrPanic(mProblem03, Sym("problem")).asCol
    val cdb = domainCdb.putAll(problemCdb)
    val spiderPlan = SpiderPlanFactory.fullTreeSearch(cdb, Level.WARNING)
    val answer = spiderPlan.solve(cdb)

    assert(answer match {
      case Some(cdbSolved) =>
        cdbSolved(Statement).asCol.length != cdb(Statement).asCol.length
          || cdbSolved(Temporal).asCol.length != cdb(Temporal).asCol.length
      case None => false
    })
  }
}