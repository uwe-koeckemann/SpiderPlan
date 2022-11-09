package org.spiderplan.solver.causal.psp

import org.aiddl.common.scala.Common.NIL
import org.aiddl.common.scala.planning.PlanningTerm.*
import org.aiddl.common.scala.reasoning.resource.{FlexibilityOrdering, LinearMcsSampler}
import org.aiddl.common.scala.reasoning.temporal.AllenConstraint.Equals
import org.aiddl.core.scala.function.{Function, Verbose}
import org.aiddl.core.scala.representation.*
import org.aiddl.core.scala.util.logger.Logger
import OpenGoalResolverSingleFlaw.GoalOrderingKey
import org.spiderplan.solver.ResolverInstruction.*
import org.spiderplan.solver.Solver.FlawResolver
import org.spiderplan.solver.Solver.FlawResolver.Result.{Consistent, Search}
import org.spiderplan.solver.SpiderPlan.Type.*
import org.spiderplan.solver.causal.heuristic.HAddReuse
import org.spiderplan.solver.{Resolver, ResolverInstruction, SpiderPlan, SpiderPlanTreeSearch}

import scala.collection.{immutable, mutable}


object OpenGoalResolverSingleFlaw {
  val GoalOrderingKey = Sym("goal.ordering")
}

class OpenGoalResolverSingleFlaw extends FlawResolver with Verbose {
  private var nextFreeId = 1L
  logger.name = this.getClass.getSimpleName

  def resolve( cdb: CollectionTerm ): FlawResolver.Result = {
    val satGoals = cdb.getOrElse(ClosedGoal, SetTerm.empty).asCol
    val openGoals = cdb.getOrElse(OpenGoal, SetTerm.empty).asCol.filter( g => !(satGoals contains g(0)) )
    val statements = cdb.getOrElse(Statement, SetTerm.empty).asCol
    val propValues = cdb.getOrElse(PropagatedValue, SetTerm.empty)
    var goalOrder = cdb.getOrElse(GoalOrderingKey, ListTerm.empty).asList.toList
    val operators = cdb.getOrElse(Operator, SetTerm.empty).asCol

    val h = new HAddReuse

    goalOrder = goalOrder.filterNot( satGoals contains _ ) // remove goals that have been satisfied elsewhere
    goalOrder = openGoals.filterNot( goalOrder contains _ ).toList ++ goalOrder // add new goals at the end TODO: add strategies

    if ( goalOrder.isEmpty ) {
      Consistent
    } else {
      val goal: Term = goalOrder.head
      logger.info( s"Selected goal: $goal")
      logger.depth += 1
      val statementResolvers: Seq[Resolver] = statements.map( (statement: Term) => {
        val sKvp = goal(1).unify(statement(1))
        //log(1, s"$goal <-> $statement => $sKvp ")
        sKvp match {
          case Some(sub) => {
            logger.info(s"Substitution resolver: $sub")
            Resolver(List(
              AddAll(Temporal, SetTerm(Tuple(Equals, statement(0), goal(0)))),
              AddAll(ClosedGoal, SetTerm(goal(0))),
              Replace(GoalOrderingKey, ListTerm(goalOrder.tail)),
              Substitute(sub.asTerm)
            ))
          }
          case _ => Resolver(Nil)
        }
      }).filterNot(r => r.isEmpty).toSeq
      val opResolvers: Seq[Resolver] = operators.flatMap( o => {
        o(Effects).asCol.map( (eff: Term) => {
          //val sInt = goal(0).unify(eff(0))
          val sKvp = eff(1).unify(goal(1))
          //val sCom = sInt.flatMap( _ + sKvp )
          //log(1, s"$goal <-> $eff => $sKvp")
          sKvp match {
            case Some(sub) => {
              var subLocal = new Substitution
              sub.asTerm.foreach( t => subLocal.add(t.asKvp.key, t.asKvp.value ) )

              val idVar = o(Id)
              val idTerm = Integer(nextFreeId)
              nextFreeId += 1
              subLocal.add(idVar, idTerm)
              o(Name).asTup.foreach( t => if ( (t\sub).isInstanceOf[Var] ) subLocal.add(t, Var(s"${t.asVar.name}_$idTerm")) )
              val oSub = o\subLocal

              val oConsRes: Seq[ResolverInstruction] = oSub(Constraints).asCol.map {
                case KeyVal(cType, cs) => AddAll(cType, cs.asCol)
                case c => throw new IllegalArgumentException(s"Unsupported constraint entry $c.\nPLease use format type:collection")
              }.toSeq

              val r = Resolver(oConsRes ++ List(
                AddAll(Statement, SetTerm(Tuple(oSub(Interval), KeyVal(oSub(Name), Bool(true))))),
                AddAll(Statement, oSub(Effects).asCol),
                AddAll(OpenGoal, oSub(Preconditions).asCol),
                AddAll(ClosedGoal, SetTerm(goal(0))),
                AddAll(Temporal, SetTerm(Tuple(Equals, eff(0)\subLocal, goal(0)))),
                Substitute(sub.asTerm),
                Replace(GoalOrderingKey, ListTerm(goalOrder.tail))
              ))
              logger.info(s"Operator resolver: ${oSub(Name)}")
              r
            }
          case _ => Resolver(Nil)
        }
        })
      }).filterNot(r => r.isEmpty).toSeq
      logger.depth -= 1
      var resolverList = statementResolvers ++ opResolvers

      resolverList = resolverList.sortBy( r => {
        val cdbResolved = SpiderPlan.applyResolver(cdb, r)
        h(cdbResolved).asNum
      })

      logger.info(s"Founds ${resolverList.length} resolvers")

      Search(resolverList)
    }
  }
}
