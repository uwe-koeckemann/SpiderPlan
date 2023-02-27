package org.spiderplan.lib.coordination_oru.demo

import org.aiddl.common.scala.Common.NIL
import org.aiddl.common.scala.execution.Actor
import org.aiddl.common.scala.execution.Actor.ActionInstanceId
import org.aiddl.common.scala.execution.dispatch.PartialOrderDispatcher
import org.aiddl.common.scala.math.graph.Graph2Dot
import org.aiddl.common.scala.planning.state_variable.heuristic.{CausalGraphHeuristic, FastForwardHeuristic}
import org.aiddl.common.scala.reasoning.temporal.Timepoint.ST
import org.aiddl.core.scala.container.{Container, Entry}
import org.aiddl.core.scala.parser.Parser
import org.aiddl.core.scala.representation.*
import org.aiddl.core.scala.util.logger.Logger
import org.spiderplan.solver.causal.{ForwardOpenGoalResolver, OperatorGrounder, OperatorGrounderFull}
import org.spiderplan.solver.causal.psp.OpenGoalResolverGraphSearch
import org.spiderplan.solver.causal.heuristic.ForwardHeuristicWrapper
import org.spiderplan.lib.coordination_oru.motion.MotionPlanner
import org.spiderplan.lib.coordination_oru.propagator.MotionPlanningPropagator
import org.spiderplan.solver.domain.DomainConstraintSolver
import org.spiderplan.solver.temporal.TemporalConstraintSolver
import org.spiderplan.solver.Solver.{FlawResolver, Propagator}
import org.spiderplan.solver.SpiderPlan.Type.*
import org.spiderplan.solver.{Heuristic, SpiderPlan, SpiderPlanFactory, SpiderPlanGraphSearch, SpiderPlanTreeSearch}
import org.aiddl.core.scala.function
import org.aiddl.common.scala.math.graph.GraphType
import org.spiderplan.solver.csp.CspPreprocessor
import org.spiderplan.solver.resource.ReusableResourceResolver

import java.util.logging.Level
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

@main def robotMovesObjects = {
  /**
   * Parse files and load problem
   */
  val c = new Container()
  val parser = new Parser(c)
  val mDomain = parser.parseFile("./test/combined-task-and-motion/domain.aiddl")
  val mProblem01 = parser.parseFile("./test/combined-task-and-motion/problem-01.aiddl")
  val domainCdb = c.getProcessedValueOrPanic(mDomain, Sym("problem")).asCol
  val problemCdb = c.getProcessedValueOrPanic(mProblem01, Sym("problem")).asCol
  val cdb = domainCdb.putAll(problemCdb)

  /**
   * Create planner
   */
  val spiderPlan: SpiderPlanGraphSearch = new SpiderPlanGraphSearch(
    Vector((new ForwardHeuristicWrapper(new FastForwardHeuristic), Num(1)))
  ) {
    override val preprocessors: Vector[function.Function] = Vector(
      new TemporalConstraintSolver,
      new CspPreprocessor {
        logSetName("CSP Preprocessor")
      },
      new OperatorGrounder
    )
    override val propagators: Vector[Propagator] = Vector(
      new DomainConstraintSolver,
      new TemporalConstraintSolver,
      new MotionPlanningPropagator
    )
    override val solvers: Vector[FlawResolver] = Vector(
      new ReusableResourceResolver,
      new ForwardOpenGoalResolver(heuristic=None),
    )
  }

  spiderPlan.logConfigRecursive(level=Level.INFO)
  spiderPlan.dumpGraphDuringSearch = true

  /**
   * Solve problem and match against answer
   */
  spiderPlan.solve(cdb) match {
    case None => println("NO-PLAN-FOUND")
    case Some(answer) => {
      /**
       * Output spider plan search graph to file
       */
      val graph2dot = new Graph2Dot(GraphType.Directed)
      val dot = graph2dot(spiderPlan.graph)
      Files.write(Paths.get("search.dot"), dot.asStr.value.getBytes(StandardCharsets.UTF_8))

      /**
       * Create and populate dispatcher
       */
      val dispatcher = new PartialOrderDispatcher

      var previous: List[Term] = Nil
      val propVals = answer(PropagatedValue).asCol
      val groups = answer(Statement).asCol.groupBy(s => propVals(Tuple(ST, s(0)))(0))

      groups.toList.sortBy((t, _) => t.asNum).foreach(
        (t, statements) => {
          println(s"time: $t")
          var newPrevious: List[Term] = Nil
          statements.foreach(s => {
            newPrevious = s(0) :: newPrevious
            println(s"  ${s(0)}, ${s(1)} ${previous.mkString(", ")}")
            dispatcher.add(s(0), s(1), previous)
          })
          previous = newPrevious
        }
      )

      /**
       * Create instant actor that accepts every action and succeeds each action instantly
       */
      object InstantActor extends Actor {
        override def supported(action: Term): Boolean = true

        override def dispatch(action: Term): Option[ActionInstanceId] = {
          val id = super.nextId
          actionIdMap.put(id, action)
          println(s"Dispatching: $action")
          super.update(id, Actor.Status.Succeeded)
          Some(id)
        }
        def tick = {}
      }

      /**
       * Create coordination actors (there could be more than one if we define multiple coordinators)
       */
      val actors = MotionPlanner.createCoordinators(answer(Sym("motion")).asCol)

      /**
       * Attach actors to dispatcher
       */
      dispatcher.actors = InstantActor :: actors

      /**
       * Tick dispatcher until it becomes idle
       */
      while (!dispatcher.isIdle) {
        dispatcher.tick
        Thread.sleep(100)
      }
    }
  }
}
