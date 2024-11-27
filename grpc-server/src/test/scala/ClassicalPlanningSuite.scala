import org.aiddl.common.scala.Common
import org.aiddl.core.scala.container.Container
import org.aiddl.core.scala.parser.Parser
import org.aiddl.core.scala.representation.Sym
import org.scalatest.funsuite.AnyFunSuite
import org.spiderplan.grpc.SpiderPlanInstance

class ClassicalPlanningSuite extends AnyFunSuite {

  test("UP test-case should not fail - 01") {
    val c = new Container()
    val parser = new Parser(c)
    val mod = parser.parseFile("src/test/resources/classical/classical-1.aiddl")
    val problem = c.getProcessedValueOrPanic(mod, Sym("problem"))

    val answer = SpiderPlanInstance(problem)
    assert(answer != Common.NIL)
  }

}
