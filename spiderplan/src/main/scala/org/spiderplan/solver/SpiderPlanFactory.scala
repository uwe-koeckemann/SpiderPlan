package org.spiderplan.solver

import org.aiddl.common.scala.planning.state_variable.heuristic.CausalGraphHeuristic
import org.aiddl.core.scala.function
import org.aiddl.core.scala.representation.*
import org.spiderplan.solver.Solver.{FlawResolver, Propagator}
import org.spiderplan.solver.causal.{ForwardOpenGoalResolver, OperatorGrounderFull}
import org.spiderplan.solver.causal.heuristic.{ForwardHeuristicWrapper, HAddReuse}
import org.spiderplan.solver.causal.psp.{OpenGoalResolver, OpenGoalResolverSingleFlaw}
import org.spiderplan.solver.conditional.ConditionalConstraintResolver
import org.spiderplan.solver.csp.{CspPreprocessor, CspResolver}
import org.spiderplan.solver.domain.DomainConstraintSolver
import org.spiderplan.solver.temporal.TemporalConstraintSolver

import java.util.logging.Level

object SpiderPlanFactory {

  /**
   * Get a full instance of graph serach spiderplan that includes all solvers and propagators provided by the basic
   * spiderplan library.
   *
   * @param cdb            constraint database to initialize solvers and propagators where needed
   * @param verbosityLevel verbosity level to use for components
   * @return a graph search instance of spiderplan
   */
  def fullGraphSearch(cdb: CollectionTerm, verbosityLevel: Level): SpiderPlanGraphSearch = {
    val subSolver: SpiderPlanTreeSearch = new SpiderPlanTreeSearch {
      override val preprocessors: Vector[function.Function] = Vector.empty
      override val propagators: Vector[Propagator] = Vector(
        new DomainConstraintSolver { logSetName("Domain"); logConfig(verbosityLevel) },
        new TemporalConstraintSolver //{ setVerbose(verbosityLevel) }
      )
      override val solvers: Vector[FlawResolver] = Vector(
      )
    }

    val spider = new SpiderPlanGraphSearch {
      //this.includePathLength = true
      override val preprocessors: Vector[function.Function] = Vector(
        new TemporalConstraintSolver,
        new CspPreprocessor { logSetName("CSP Preprocessor") },
        new OperatorGrounderFull
      )

      override val propagators: Vector[Propagator] = Vector(
        new DomainConstraintSolver { logSetName("Domain") },
        new TemporalConstraintSolver //{ setVerbose(verbosityLevel) }
      )

      override val solvers: Vector[FlawResolver] = Vector(
        new CspResolver { logSetName("CSP") },
        new ForwardOpenGoalResolver(heuristic=None) {logSetName("GoalResolver") },
        new ConditionalConstraintResolver(subSolver) { logSetName("Conditional") }
      )

      override val heuristics: Vector[Heuristic] = Vector(
        //new HAddReuse //{ setVerbose(verbosityLevel) }
        new ForwardHeuristicWrapper(new CausalGraphHeuristic)
      )
    }
    spider.logSetName("SpiderPlan")
    spider.logConfigRecursive(verbosityLevel)
    spider
  }

  /**
   * Get a full instance of tree serach SpiderPlan that includes all solvers and propagators provided by the basic
   * SpidePlan library.
   *
   * @param cdb            constraint database to initialize solvers and propagators where needed
   * @param verbosityLevel verbosity level to use for components
   * @return a graph search instance of spiderplan
   */
  def fullTreeSearch(cdb: CollectionTerm, verbosityLevel: Level): SpiderPlanTreeSearch = {
    val subSolver: SpiderPlanTreeSearch = new SpiderPlanTreeSearch {
      override val preprocessors: Vector[function.Function] = Vector.empty

      override val propagators: Vector[Propagator] = Vector(
        new DomainConstraintSolver,
        new TemporalConstraintSolver
      )
      override val solvers: Vector[FlawResolver] = Vector(
      )
    }

    val spider = new SpiderPlanTreeSearch {
      override val preprocessors: Vector[function.Function] = Vector(
        new OperatorGrounderFull
      )
      override val propagators: Vector[Propagator] = Vector(
        new DomainConstraintSolver,
        new TemporalConstraintSolver
      )
      override val solvers: Vector[FlawResolver] = Vector(
        new CspResolver,
        new ForwardOpenGoalResolver(heuristic = Some(new CausalGraphHeuristic)),
        new ConditionalConstraintResolver(subSolver)
      )
    }
    spider.logSetName("SpiderPlan")
    spider.logConfigRecursive(verbosityLevel)
    spider
  }
}
