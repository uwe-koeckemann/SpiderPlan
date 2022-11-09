package org.spiderplan.solver.causal.psp

import org.aiddl.common.scala.search.TreeSearch
import org.aiddl.core.scala.representation.*
import org.spiderplan.solver.ResolverInstruction
import org.spiderplan.solver.SpiderPlan
import org.spiderplan.solver.Resolver
import org.spiderplan.solver.SpiderPlan.Instruction
import org.spiderplan.solver.SpiderPlan.Type.OpenGoal
import org.spiderplan.solver.SpiderPlan.Type.ClosedGoal
import org.spiderplan.solver.SpiderPlan.Type.Statement
import org.spiderplan.solver.SpiderPlan.Type.PropagatedValue
import org.spiderplan.solver.SpiderPlan.Type.Temporal
import org.spiderplan.solver.causal.psp.goal_selection.{GoalOrdering, GoalOrderingAllFifo}

class OpenGoalResolverSearch extends TreeSearch {
  val f_expand = new OpenGoalResolverExpander()

  var openGoals: Seq[Term] = Nil
  var initCdb: CollectionTerm = _
  var goalOrdering: List[GoalOrdering] = Nil

  override def init( x: Term ) =
    initCdb = x(0).asCol
    val operators = x(1).asCol

    val satGoals = initCdb.getOrElse(ClosedGoal, SetTerm.empty).asCol
    openGoals = initCdb.getOrElse(OpenGoal, SetTerm.empty).asCol.filter( g => !(satGoals contains g(0)) ).toSeq
    goalOrdering = List(GoalOrderingAllFifo(openGoals.toList))

    f_expand.init(operators)
    super.init(initCdb)

  override def choiceHook: Unit = {
    val addedGoals = choice.head.asCol.flatMap( e => e match {
      case Tuple(Instruction.AddAll, OpenGoal, newGoals) => newGoals.asList.list
      case _ => Seq.empty
    }).toSeq
    goalOrdering = goalOrdering.head.add(addedGoals) :: goalOrdering.tail
  }

  override def expandHook: Unit = {
    goalOrdering = goalOrdering.head.pop :: goalOrdering
  }

  override def backtrackHook: Unit = {
    goalOrdering = goalOrdering.tail
  }

  override def expand: Option[Seq[Term]] = {
    val resolver = Resolver(choice.flatMap( c => c.asCol.map( inst => ResolverInstruction.from(inst) ) ))

    val cdb = SpiderPlan.applyResolver(initCdb, resolver)
    val satGoals = cdb.getOrElse(ClosedGoal, SetTerm.empty).asCol
    openGoals = cdb.getOrElse(OpenGoal, SetTerm.empty).asCol.filter( g => !(satGoals contains g(0)) ).toSeq

    if (openGoals.isEmpty) None
    else {
      val selectedGoal = openGoals.head
      Some(f_expand(selectedGoal, cdb))
    }
  }
}