import org.aiddl.common.scala.Common.NIL
import org.aiddl.core.scala.container.{Container, Entry}
import org.aiddl.core.scala.parser.Parser
import org.aiddl.core.scala.representation.*
import org.scalatest.funsuite.AnyFunSuite
import org.spiderplan.solver.Solver.FlawResolver.Result
import org.aiddl.common.scala.math.graph.Graph2Dot
import org.aiddl.common.scala.math.graph.GraphType
import org.aiddl.core.scala.util.logger.Logger
import org.spiderplan.solver.csp.*
import org.spiderplan.solver.Solver.FlawResolver.Result.Search
import org.spiderplan.solver.Solver.{FlawResolver, Propagator}
import org.spiderplan.solver.SpiderPlan.Type.*
import org.spiderplan.solver.causal.heuristic.HAddReuse
import org.spiderplan.solver.causal.psp.OpenGoalResolverGraphSearch
import org.spiderplan.solver.csp.CspResolver
import org.spiderplan.solver.domain.DomainConstraintSolver
import org.spiderplan.solver.temporal.TemporalConstraintSolver
import org.spiderplan.solver.{Heuristic, Solver, SpiderPlan, SpiderPlanFactory, SpiderPlanGraphSearch, SpiderPlanTreeSearch}

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import java.util.logging.Level

class CspSuite extends AnyFunSuite {
  val c = new Container()
  val parser = new Parser(c)
  val mProblem01 = parser.parseFile("./test/csp/problem-01.aiddl")

  test("4 Queens - Search Result") {
    val cdb = c.getProcessedValueOrPanic(mProblem01, Sym("problem")).asCol
    val cspResolver = new CspResolver
    val answer = cspResolver.resolve(cdb)

    assert(answer match {
      case Solver.FlawResolver.Result.Search(it) =>
        val solutionList = it.toList
        solutionList.length == 2
      case _ => false
    })
  }

  test("4 Queens - Grounded Consistent") {
    var cdb = c.getProcessedValueOrPanic(mProblem01, Sym("problem"))
    val cspResolver = new CspResolver

    val sub = new Substitution()
    sub.add(Var("X1"), Num(3))
    sub.add(Var("X2"), Num(1))
    sub.add(Var("X3"), Num(4))
    sub.add(Var("X4"), Num(2))

    cdb = cdb \ sub

    val answer = cspResolver.resolve(cdb.asCol)

    assert(answer == Solver.FlawResolver.Result.Consistent)
  }

  test("4 Queens - Grounded Inconsistent") {
    var cdb = c.getProcessedValueOrPanic(mProblem01, Sym("problem"))
    val cspResolver = new CspResolver

    val sub = new Substitution()
    sub.add(Var("X1"), Num(4))
    sub.add(Var("X2"), Num(2))
    sub.add(Var("X3"), Num(4))
    sub.add(Var("X4"), Num(2))

    cdb = cdb \ sub

    val answer = cspResolver.resolve(cdb.asCol)

    assert(answer == Solver.FlawResolver.Result.Inconsistent)
  }

  test("4 Queens - Partially Grounded Search") {
    var cdb = c.getProcessedValueOrPanic(mProblem01, Sym("problem"))
    val cspResolver = new CspResolver

    val sub = new Substitution()
    sub.add(Var("X1"), Num(3))
    sub.add(Var("X2"), Num(1))
    //sub.add(Var("X3"), Num(4))
    sub.add(Var("X4"), Num(2))

    cdb = cdb \ sub

    val answer = cspResolver.resolve(cdb.asCol)

    assert(answer match {
      case Solver.FlawResolver.Result.Search(it) =>
        val solutionList = it.toList
        solutionList.length == 1
      case _ => false
    })
  }

  test("4 Queens - SpiderPlan Graph Search") {
    val cdb = c.getProcessedValueOrPanic(mProblem01, Sym("problem")).asCol
    val spiderPlan = SpiderPlanFactory.fullGraphSearch(cdb, Level.WARNING)
    val answer = spiderPlan.solve(cdb)

    assert(answer match {
      case Some(cdb) => cdb.isGround
      case None => false
    })
  }

  test("4 Queens - SpiderPlan Tree Search") {
    val cdb = c.getProcessedValueOrPanic(mProblem01, Sym("problem")).asCol
    val spiderPlan = SpiderPlanFactory.fullTreeSearch(cdb, Level.WARNING)
    val answer = spiderPlan.solve(cdb)

    assert(answer match {
      case Some(cdb) => cdb.isGround
      case None => false
    })
  }

  test("Unified Planning - basic with complex condition (just CSP)") {
    val mProblem = parser.parseFile("./test/up-problems/basic-nc.aiddl")

    val cdb = c.getProcessedValueOrPanic(mProblem, Sym("problem")).asCol

    val cspResolver = new CspResolver
    val answer = cspResolver.resolve(cdb)

    assert(answer match
      case Result.Consistent => false
      case Result.Inconsistent => false
      case Result.Search(rs) => rs.toList.length == 1)
  }

  test("Unified Planning - basic with complex condition") {
    val mProblem = parser.parseFile("./test/up-problems/basic-nc.aiddl")

    val cdb = c.getProcessedValueOrPanic(mProblem, Sym("problem")).asCol
    val spiderPlan = SpiderPlanFactory.fullGraphSearch(cdb, Level.WARNING)
    val answer = spiderPlan.solve(cdb)

    val g = spiderPlan.graph
    val g2d = new Graph2Dot(GraphType.Directed)
    val dot = g2d(g).asStr.value
    Files.write(Paths.get("search.dot"), dot.getBytes(StandardCharsets.UTF_8))

    assert(answer match {
      case Some(_) => true
      case None => false
    })
  }
}