package org.spiderplan.grpc

import org.aiddl.common.scala.execution.Actor.Status
import org.aiddl.core.scala.util.StopWatch
import org.aiddl.core.scala.container.Container
import org.aiddl.core.scala.representation.{CollectionTerm, KeyVal, ListTerm, Num, Sym, Term}
import org.aiddl.external.grpc.scala.container.ContainerServer
import org.spiderplan.solver.SpiderPlanFactory
import org.aiddl.core.scala.function.Function
import org.aiddl.core.scala.util.logger.Logger
import org.spiderplan.solver.SpiderPlanGraphSearch
import org.aiddl.common.scala.planning.state_variable.heuristic.{CausalGraphHeuristic, FastForwardHeuristic}
import org.aiddl.core.scala.function
import org.aiddl.core.scala.representation.*
import org.spiderplan.solver.Solver.{FlawResolver, Propagator}
import org.spiderplan.solver.causal.{ForwardOpenGoalResolver, OperatorGrounder}
import org.spiderplan.solver.causal.heuristic.{ForwardHeuristicWrapper, HAddReuse}
import org.spiderplan.solver.causal.psp.{OpenGoalResolver, OpenGoalResolverSingleFlaw}
import org.spiderplan.solver.conditional.ConditionalConstraintResolver
import org.spiderplan.solver.csp.{CspPreprocessor, CspResolver}
import org.spiderplan.solver.domain.DomainConstraintSolver
import org.spiderplan.solver.temporal.TemporalConstraintSolver
import org.spiderplan.solver.SpiderPlan.Type.*
import org.aiddl.common.scala.planning.PlanningTerm.*
import org.spiderplan.solver.pruning.PruningPropagator

import java.nio.charset.StandardCharsets
import java.util.logging.Level
import java.nio.file.{Files, Paths}
object SpiderPlanInstance extends Function {
  def apply(cdb: Term): Term = {
    val spiderPlan = new SpiderPlanGraphSearch(
      Vector(
        (new ForwardHeuristicWrapper(new FastForwardHeuristic), Num(1.0)),
        //(new ForwardHeuristicWrapper(new CausalGraphHeuristic), Num(1))
        //(new ForwardHeuristicWrapper(new FastForwardHeuristic), Num(0.5))
      )) {
      self: SpiderPlanGraphSearch =>

      override val preprocessors: Vector[function.Function] = Vector(
        new TemporalConstraintSolver,
        new CspPreprocessor {
          logSetName("CSP Preprocessor")
        },
        new OperatorGrounder
      )

      override val propagators: Vector[Propagator] = Vector(
        new PruningPropagator,
        new DomainConstraintSolver {
          logSetName("Domain")
        },
        new TemporalConstraintSolver, //{ setVerbose(verbosityLevel) }
      )

      override val solvers: Vector[FlawResolver] = Vector(
        new CspResolver {
          logSetName("CSP")
        },
        new ForwardOpenGoalResolver(heuristic = None) {
          logSetName("GoalResolver")
          seenList = Some(self.seenList)
        },
      )
    }
    spiderPlan.logSetName("SpiderPlan")
    spiderPlan.logConfigRecursive(Level.INFO)

    StopWatch.recordedTimes.clear()

    val c = new Container()
    val input = c.eval(cdb).asCol
    /*
          val statements = SetTerm(input(Statement).asCol.filter( s =>
            input(OpenGoal).asCol.exists( g => g(1) unifiable s(1) )
            || input(Operator).asCol.exists( o => o(Preconditions).asCol.exists( p => p(1) unifiable s(1) ) ) ).toSet)

          println(s"A: ${input(Statement).asCol.length} B: ${statements.asCol.length}")

          val acs = SetTerm(input(Temporal).asCol.filter(c => statements.exists(s => c(0) == s(0) || c(1) == s(0) )).toSet)
          println(s"A: ${input(Temporal).asCol.length} B: ${acs.asCol.length}")
          */

    try {
      StopWatch.start("[SpiderPlan] Main")
      val r = spiderPlan.solve(input) match {
        case Some(solution) => solution
        case None => Sym("NIL")
      }
      StopWatch.stop("[SpiderPlan] Main")
      spiderPlan.searchGraph2File("search.dot")
      Files.write(Paths.get("stopwatch.txt"), StopWatch.summary.getBytes(StandardCharsets.UTF_8))
      println(StopWatch.summary)
      r
    } catch {
      case e: Exception => {
        e.printStackTrace()
        Sym("NIL")
      }
    }
  }
}
