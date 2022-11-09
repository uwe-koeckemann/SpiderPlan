package org.spiderplan.solver.causal

import org.aiddl.common.scala.Common.NIL
import org.aiddl.common.scala.planning.PlanningTerm.*
import org.aiddl.common.scala.planning.state_variable.Expansion
import org.aiddl.common.scala.reasoning.resource.{FlexibilityOrdering, LinearMcsSampler}
import org.aiddl.common.scala.reasoning.temporal.AllenConstraint.Equals
import org.aiddl.core.scala.function.{Function, Initializable, Verbose}
import org.aiddl.core.scala.representation.*
import org.aiddl.core.scala.util.logger.Logger
import org.spiderplan.solver.ResolverInstruction.*
import org.spiderplan.solver.Solver.FlawResolver
import org.spiderplan.solver.Solver.FlawResolver.Result
import org.spiderplan.solver.SpiderPlan.Type.*
import org.spiderplan.solver.causal.state_space.{GoalCdb2Svp, OperatorCdb2Svp, StateCdb2Svp}
import org.spiderplan.solver.{Resolver, ResolverInstruction}

import scala.collection.{immutable, mutable}

import org.aiddl.core.scala.representation.conversion.given_Conversion_Term_KeyVal

import scala.language.implicitConversions

class ForwardOpenGoalResolver[F <: Function with Initializable](heuristic: Option[F]) extends FlawResolver with Verbose {
  val extractState = new StateCdb2Svp
  val extractGoal = new GoalCdb2Svp
  val extractOperators = new OperatorCdb2Svp
  val expansion = new Expansion

  var nextId = 0L

  override def resolve(cdb: CollectionTerm): FlawResolver.Result = {
    val goal = extractGoal(cdb).asCol
    if (goal.isEmpty) {
      this.logger.info("No open goals.")
      Result.Consistent
    } else {
      this.logger.info(s"Trying to reach: ${goal.mkString(", ")}")
      val state = extractState(cdb).asCol
      if ( state.containsAll(goal) ) {
        // If goal reached:
        //    -> link to effects and mark goals as satisfied
        var closedGoals: Set[Term] = Set.empty
        val constraints: Set[Term] = goal.map( g => {
          val variable = g.key
          val currentInterval = extractState.getIntervalMap(variable)
          val goalInterval = extractGoal.getGoalInterval(g)
          closedGoals += goalInterval
          Tuple(Equals, currentInterval, goalInterval)
        }).toSet

        FlawResolver.Result.Search(List(Resolver(List(
          AddAll(Temporal, SetTerm(constraints)),
          AddAll(ClosedGoal, SetTerm(closedGoals)),
        ), Some("Linking goals"))))
        //    Else:
        //    -> expand state and create resolvers that link preconditions of chosen operators to most recent statement
      } else {
        //    Else:
        //    -> expand state and create resolvers that link preconditions of chosen operators to most recent statement
        val ops = extractOperators(cdb).asCol
        expansion.init(ops)
        var edges = expansion.expand(state)

        edges = this.heuristic match {
          case Some(h) => {
            val problem = SetTerm(
              KeyVal(InitialState, state),
              KeyVal(Goal, goal),
              KeyVal(Operators, ops),
            )
            h.init(problem)
            edges.sortBy((_, s) => h(s).asNum)
          }
          case None => edges
        }

        val resolvers: Seq[Resolver] = edges.map( (action, _) => {
          val actionName = action
          var spiderOp = extractOperators.getSpiderPlanOperator(actionName)

          val subLocal = new Substitution
          val idVar = spiderOp(Id)
          val idTerm = Integer(nextId)
          nextId += 1
          subLocal.add(idVar, idTerm)

          if ( spiderOp(Name).isInstanceOf[Tuple] ) {
            spiderOp(Name).asTup.foreach(t => if ((t \ subLocal).isInstanceOf[Var]) subLocal.add(t, Var(s"${t.asVar.name}_$idTerm")))
          }
          Term.collect(_.isInstanceOf[Var])(spiderOp\subLocal).distinct.foreach( t => subLocal.add(t, Var(s"${t.asVar.name}_$idTerm")) )

          spiderOp = spiderOp\subLocal

          val oConsPre: Set[Term] = spiderOp(Preconditions).asCol.map( p => {
            val variable = p(1).key
            val currentInterval = extractState.getIntervalMap(variable)
            val c = Tuple(Equals, currentInterval, p(0))
            c
          }).toSet

          val oConsRes: Seq[ResolverInstruction] = spiderOp(Constraints).asCol.map {
            case KeyVal(cType, cs) => AddAll(cType, cs.asCol)
            case c => throw new IllegalArgumentException(s"Unsupported constraint entry $c.\nPLease use format type:collection")
          }.toSeq

          val r = Resolver(oConsRes ++ List(
            AddAll(Statement, SetTerm(Tuple(spiderOp(Interval), KeyVal(spiderOp(Name), Bool(true))))),
            AddAll(Statement, spiderOp(Effects).asCol),
            AddAll(Statement, spiderOp(Preconditions).asCol),
            AddAll(Temporal, SetTerm(oConsPre)),
          ), Some(s"${action}"))
          r
        })

        if ( resolvers.isEmpty ) {
          this.logger.info("No resolver found.")
          FlawResolver.Result.Inconsistent
        }
        else {
          this.logger.info("Adding resolvers.")
          FlawResolver.Result.Search(resolvers)
        }
      }
    }
  }
}
