package org.spiderplan.solver.causal.state_space

import org.aiddl.core.scala.function.Function
import org.aiddl.core.scala.representation._

import org.aiddl.common.scala.planning.PlanningTerm._

class OperatorCdb2Svp extends Function {

  var spiderOpMap: Map[Term, Term] = Map.empty

  override def apply(x: Term): Term = {
    val oCdb = x.asCol.getOrElse(Sym("operator"), SetTerm.empty).asCol

    val oSvp = oCdb.map( oIn => {
      val name = oIn(Name)
      val signature = oIn(Signature)
      val preIn = oIn(Preconditions)
      val effIn = oIn(Effects)

      val pre = SetTerm(preIn.asCol.map( p => p(1) ).toSet)
      val eff = SetTerm(effIn.asCol.map( e => e(1) ).toSet)

      val oOut = Tuple(
        KeyVal(Name, name),
        KeyVal(Signature, signature),
        KeyVal(Preconditions, pre),
        KeyVal(Effects, eff)
      )
      spiderOpMap = spiderOpMap.updated(name, oIn)
      oOut
    })

    SetTerm(oSvp.toSet)
  }

  def getSpiderPlanOperator(name: Term): Term = this.spiderOpMap(name)

}
