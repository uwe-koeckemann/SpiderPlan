package org.spiderplan.grpc

import java.util.logging.Level
import java.nio.file.{Paths, Files}
import java.nio.charset.StandardCharsets

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

import scala.concurrent.ExecutionContext


object Main extends App {
  val c = new Container()
  c.addFunction(Sym("org.spiderplan.unified-planning.basic-graph-search"), SpiderPlanInstance)
  val server = new ContainerServer(ExecutionContext.global, 8061, c)

  server.start()
  server.blockUntilShutdown()
}
