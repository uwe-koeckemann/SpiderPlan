import org.aiddl.common.scala.Common.NIL
import org.aiddl.common.scala.math.graph.{Graph2Dot, GraphType}
import org.aiddl.core.scala.container.{Container, Entry}
import org.aiddl.core.scala.parser.Parser
import org.aiddl.core.scala.representation.*
import org.aiddl.core.scala.util.logger.Logger
import org.scalatest.funsuite.AnyFunSuite
import org.spiderplan.solver.Solver.FlawResolver.Result
import org.spiderplan.solver.Solver.FlawResolver.Result.Search
import org.spiderplan.solver.Solver.{FlawResolver, Propagator}
import org.spiderplan.solver.SpiderPlan.Type.*
import org.spiderplan.solver.causal.heuristic.HAddReuse
import org.spiderplan.solver.causal.psp.OpenGoalResolverGraphSearch
import org.spiderplan.solver.csp.*
import org.spiderplan.solver.domain.DomainConstraintSolver
import org.spiderplan.solver.temporal.TemporalConstraintSolver
import org.spiderplan.solver.*
import org.spiderplan.solver.resource.ReusableResourceResolver

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import java.util.logging.Level

class ReusableResourceResolverSuite extends AnyFunSuite {
  val c = new Container()
  val parser = new Parser(c)

  val tcSolver = new TemporalConstraintSolver
  val rrResolver = new ReusableResourceResolver

  test("Scheduling problem 01: Resolvable conflict") {
    val problemModule = parser.parseFile("./test/reusable-resources/problem-01.aiddl")
    var cdb = c.getProcessedValueOrPanic(problemModule, Sym("problem")).asCol
    cdb = tcSolver(cdb).asCol
    val answer = rrResolver.resolve(cdb)

    val passed = answer match
      case FlawResolver.Result.Consistent => false
      case FlawResolver.Result.Inconsistent => false
      case FlawResolver.Result.Search(rs) => rs.size == 2

    assert(passed)
  }

  test("Scheduling problem 02: Unresolvable conflict still suggests two resolvers") {
    val problemModule = parser.parseFile("./test/reusable-resources/problem-02.aiddl")
    var cdb = c.getProcessedValueOrPanic(problemModule, Sym("problem")).asCol
    cdb = tcSolver(cdb).asCol
    val answer = rrResolver.resolve(cdb)

    val passed = answer match
      case FlawResolver.Result.Consistent => false
      case FlawResolver.Result.Inconsistent => false
      case FlawResolver.Result.Search(rs) => rs.size == 2

    assert(passed)
  }

  test("Scheduling problem 03: No conflict") {
    val problemModule = parser.parseFile("./test/reusable-resources/problem-03.aiddl")
    var cdb = c.getProcessedValueOrPanic(problemModule, Sym("problem")).asCol
    cdb = tcSolver(cdb).asCol
    val answer = rrResolver.resolve(cdb)

    val passed = answer match
      case FlawResolver.Result.Consistent => true
      case FlawResolver.Result.Inconsistent => false
      case FlawResolver.Result.Search(rs) => false

    assert(passed)
  }

  test("Scheduling problem 04: Exception when capacity missing") {
    val problemModule = parser.parseFile("./test/reusable-resources/problem-04.aiddl")
    var cdb = c.getProcessedValueOrPanic(problemModule, Sym("problem")).asCol
    cdb = tcSolver(cdb).asCol
    assertThrows[IllegalArgumentException](rrResolver.resolve(cdb))
  }

  test("Scheduling problem 05: Non-ground usage ignored") {
    val problemModule = parser.parseFile("./test/reusable-resources/problem-05.aiddl")
    var cdb = c.getProcessedValueOrPanic(problemModule, Sym("problem")).asCol
    cdb = tcSolver(cdb).asCol
    val answer = rrResolver.resolve(cdb)

    val passed = answer match
      case FlawResolver.Result.Consistent => true
      case FlawResolver.Result.Inconsistent => false
      case FlawResolver.Result.Search(rs) => false

    assert(passed)
  }
}