import org.aiddl.core.scala.container.{Container, Entry}
import org.aiddl.core.scala.parser.Parser
import org.aiddl.core.scala.representation.*
import org.scalatest.funsuite.AnyFunSuite
import org.spiderplan.solver.Solver.Propagator.Result.Consistent
import org.spiderplan.solver.Solver.Propagator.Result.Inconsistent
import org.spiderplan.solver.causal.psp.{OpenGoalResolverAllFlaws, OpenGoalResolverSingleFlaw}
import org.spiderplan.solver.domain.DomainConstraintSolver
import org.spiderplan.solver.temporal.TemporalConstraintSolver

class DomainConstraintSuite extends AnyFunSuite {
  val container = new Container
  val parser = Parser(container)
  val cdb = parser.str("{domain:{t_bool:{T F} " +
    "t_num:{1 2 3}} " +
    "t_num_range:(range (0 10) {type:real include-lower:false include-upper:false})" +
    "signature:{(p t_num):t_bool}}").asCol
  val solver = new DomainConstraintSolver

  test("Can be satisfied.") {
    val stmts = parser.str("statement:{" +
      "  (i1 (p 1):T) " +
      "  (i2 (p 2):F) " +
      "}").asKvp
    val cdbTest = cdb.put(stmts)
    assert(solver.propagate(cdbTest) == Consistent)
  }

  test("Value is not satisfied.") {
    val stmts = parser.str("statement:{" +
      "  (i1 (p 1):T) " +
      "  (i2 (p 2):R) " +
      "}").asKvp
    val cdbTest = cdb.put(stmts)
    assert(solver.propagate(cdbTest) == Inconsistent)
  }

  test("Variable signature is not satisfied.") {
    val stmts = parser.str("statement:{" +
      "  (i1 (p 4):T) " +
      "  (i2 (p 2):R) " +
      "}").asKvp
    val cdbTest = cdb.put(stmts)
    assert(solver.propagate(cdbTest) == Inconsistent)
  }

  test("Variable used cannot be satisfied.") {
    val stmts = parser.str("statement:{" +
      "  (i1 (p 4):T) " +
      "  (i2 (p ?X):?X) " +
      "}").asKvp
    val cdbTest = cdb.put(stmts)
    assert(solver.propagate(cdbTest) == Inconsistent)
  }
}
