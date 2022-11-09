package org.spiderplan

import org.aiddl.core.scala.container.Container
import org.aiddl.core.scala.parser.Parser
import org.aiddl.core.scala.representation.Sym
import org.aiddl.core.scala.util.logger.Logger
import org.spiderplan.solver.SpiderPlanFactory

import java.util.logging.Level

@main def spiderPlan(domainFilename: String, problemFilename: String) = {
  val c = new Container()
  val parser = new Parser(c)
  val mDomain = parser.parseFile(domainFilename)
  val mProblem = parser.parseFile(problemFilename)

  val domain = c.getProcessedValueOrPanic(mDomain, Sym("problem")).asCol
  val problem = c.getProcessedValueOrPanic(mProblem, Sym("problem")).asCol

  val cdb = domain.putAll(problem)

  val spiderPlan = SpiderPlanFactory.fullGraphSearch(cdb, Level.INFO)

  val answer = spiderPlan.solve(cdb)

  answer match
    case Some(cdb) =>
      println("SOLUTION CONSTRAINT DATABASE:")
      println(Logger.prettyPrint(cdb, 0))
    case None => println("NO SOLUTION")
}
