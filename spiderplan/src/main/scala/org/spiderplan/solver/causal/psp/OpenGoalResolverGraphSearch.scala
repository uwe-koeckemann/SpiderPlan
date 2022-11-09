package org.spiderplan.solver.causal.psp

import org.aiddl.common.scala.search.{GraphSearch, TreeSearch}
import org.aiddl.core.scala.representation.*
import org.spiderplan.solver.SpiderPlan
import org.spiderplan.solver.SpiderPlan.Instruction
import org.spiderplan.solver.SpiderPlan.Type.*
import org.spiderplan.solver.{Heuristic, Resolver, ResolverInstruction}
import org.aiddl.common.scala.planning.PlanningTerm.Name
import org.spiderplan.solver.causal.heuristic.HAddReuse
import org.spiderplan.solver.causal.psp.goal_selection.{GoalOrdering, GoalOrderingAllFifo}

class OpenGoalResolverGraphSearch extends GraphSearch {
  val f_expand = new OpenGoalResolverExpander()
  var f_heuristic: Heuristic = _
  var operators: CollectionTerm = _

  override def init( x: Term ) =
    val cdb0 = x(0).asCol
    this.operators = x(1).asCol
    f_expand.init(operators)
    f_heuristic = new HAddReuse
    super.init(ListTerm(cdb0))

  override def expand(cdb: Term): Seq[Term] = {
    val satGoals = cdb.getOrElse(ClosedGoal, SetTerm.empty)
    val openGoals = cdb.getOrElse(OpenGoal, SetTerm.empty).asCol
      .filter( g => !(satGoals.asCol contains g(0)) )

    val resolverTerms = openGoals.flatMap( og => f_expand(og, cdb.asCol) )
    resolverTerms.map( rTerm => Tuple(
      rTerm,
      SpiderPlan.applyResolver(
        cdb.asCol,
        Resolver(rTerm.asCol.map( ri => ResolverInstruction.from(ri) ).toSeq))
    )).toSeq
  }

  override def g(n: Term): Num = Num(n.asCol(Statement).asCol.count(
    s => this.operators.exists( o => o(Name) unifiable s(1).asKvp.key )
  ))
  override def h( n: Term ): Num = f_heuristic(n.asCol)

  override def isGoal(cdb: Term): Boolean = {
    val satGoals = cdb.getOrElse(ClosedGoal, SetTerm.empty)
    val openGoals = cdb.getOrElse(OpenGoal, SetTerm.empty).asCol
      .filter( g => !(satGoals.asCol contains g(0)) )
    openGoals.isEmpty
  }

}