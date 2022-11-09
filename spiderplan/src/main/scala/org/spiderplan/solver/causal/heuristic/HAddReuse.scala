package org.spiderplan.solver.causal.heuristic

import org.aiddl.core.scala.representation.*
import org.spiderplan.solver.SpiderPlan
import org.spiderplan.solver.Resolver
import org.spiderplan.solver.SpiderPlan.Type.*
import org.aiddl.common.scala.planning.PlanningTerm.*
import org.spiderplan.solver.ResolverInstruction.AddAll
import org.spiderplan.solver.{Heuristic, SpiderPlanTreeSearch}

class HAddReuse extends Heuristic {

  def apply( cdb: CollectionTerm ): Num = {
    val operators = cdb.getOrPanic(Operator).asCol
    val satGoals = cdb.getOrElse(ClosedGoal, SetTerm.empty)
    val openGoals = cdb.getOrElse(OpenGoal, SetTerm.empty).asCol
      .filter( g => !(satGoals.asCol contains g(0)) )

    if ( openGoals.isEmpty ) Num(0)
    else {
      val hs = openGoals.map(og => this.hAddStatement(og, cdb, operators))
      hs.reduce(_ + _)
    }
  }

  private def hAddStatement( q: Term, cdb: CollectionTerm, os: CollectionTerm ): Num =
    if ( cdb.getOrElse(Statement, SetTerm.empty).asCol.exists( s => q(1) unifiable s(1) ) ) {
      Num(1)
    } else {
      val costs: Iterable[Num] = os.flatMap( o => o(Effects).asCol.map(
        e => if ( e(1) unifiable q(1) ) {
          val resolver = Resolver(List(AddAll(Statement, o(Effects).asCol)))
          val cdbEff = SpiderPlan.applyResolver(cdb, resolver)
          this.hAddAction(o, cdbEff, os)
        } else {
          InfPos()
        }
      ))

      costs.min
    }

  private def hAddAction( a: Term, cdb: CollectionTerm, os: CollectionTerm ): Num = {
    Num(1) + a.getOrElse(Preconditions, SetTerm.empty).asCol.map( hAddStatement(_, cdb, os) ).reduce(_ + _).asNum
  }
}
