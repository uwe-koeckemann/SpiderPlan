package org.spiderplan.solver.temporal

import org.aiddl.common.scala.reasoning.temporal.Timepoint.ET
import org.aiddl.core.scala.representation.*
import org.spiderplan.solver.Resolver
import org.spiderplan.solver.ResolverInstruction.PutAll
import org.spiderplan.solver.Solver.Propagator
import org.spiderplan.solver.SpiderPlan.Type.{PropagatedValue, Statement}

import scala.collection.mutable

/**
 * Extend earliest end time of statements to be latest end time. Intended to reflect that statements added as effects
 * will only change value if another operator causes the change.
 */
class StateVariableIntervalExtender extends Propagator {
  val ExtendedKey = Sym("statement.extended-eet")

  override def propagate(cdb: CollectionTerm): Propagator.Result = {
    val patterns = cdb(ExtendedKey).asCol
    val propValues = cdb(PropagatedValue).asCol

    val lastInterval: mutable.HashMap[Term, Term] = new mutable.HashMap()
    val eet: mutable.HashMap[Term, Num] = new mutable.HashMap()
    val let: mutable.HashMap[Term, Num] = new mutable.HashMap()


    cdb(Statement).asCol.foreach( stmt => {
      if ( stmt.isGround ) {
        val s_int = stmt(0)
        val s_kvp = stmt(1).asKvp
        val s_eet = propValues(Tuple(ET, s_int))(0).asNum
        val c_eet = eet.getOrElse(s_kvp, InfNeg())
        if ( s_eet > c_eet ) {
          val s_let = propValues(Tuple(ET, s_int))(1).asNum
          eet.put(s_kvp, s_eet)
          let.put(s_kvp, s_let)
          lastInterval.put(s_kvp, s_int)
        }
      }
    })

    val changes = SetTerm(lastInterval.map( (kvp, int) => {
      val int_let = let(kvp)
      KeyVal(Tuple(ET, int), Tuple(int_let, int_let))
    }).toSet)
    val newPropVals = propValues.putAll(changes)

    val change = Resolver(List(
      PutAll(PropagatedValue, newPropVals)
    ))

    Propagator.Result.ConsistentWith(change)
  }
}
