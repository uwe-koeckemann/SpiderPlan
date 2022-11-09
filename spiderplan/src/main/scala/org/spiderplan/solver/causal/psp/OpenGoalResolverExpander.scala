package org.spiderplan.solver.causal.psp

import org.aiddl.common.scala.planning.PlanningTerm.*
import org.aiddl.common.scala.reasoning.temporal.AllenConstraint.Equals
import org.aiddl.core.scala.function.{Function, Initializable, Verbose}
import org.aiddl.core.scala.representation.*
import org.aiddl.core.scala.util.logger.Logger

import org.spiderplan.solver.SpiderPlan.Instruction

import org.spiderplan.solver.SpiderPlan.Type.OpenGoal
import org.spiderplan.solver.SpiderPlan.Type.ClosedGoal
import org.spiderplan.solver.SpiderPlan.Type.Statement
import org.spiderplan.solver.SpiderPlan.Type.PropagatedValue
import org.spiderplan.solver.SpiderPlan.Type.Temporal

class OpenGoalResolverExpander extends Function with Initializable with Verbose {

  var operators: CollectionTerm = _
  private var nextFreeId = 1L

  override def init(ops: Term): Unit =
    operators = ops.asCol

  override def apply( x: Term ): Term = ListTerm(this(x(0), x(1).asCol))

  def apply( goal: Term, cdb: CollectionTerm ): Seq[Term] = {
    val statements = cdb.getOrElse(Statement, SetTerm.empty).asCol
    logger.info(s"Selected goal: $goal")
    logger.depth += 1
    val statementResolvers: Seq[Term] = statements.flatMap( (statement: Term) => {
      val sKvp = goal(1).unify(statement(1))
      //log(1, s"$goal <-> $statement => $sKvp ")
      sKvp match {
        case Some(sub) => {
          logger.info(s"Substitution resolver: $sub")
          List(ListTerm(List(
            Tuple(Instruction.Substitute, sub.asTerm),
            Tuple(Instruction.AddAll, Temporal, SetTerm(Tuple(Equals, statement(0), goal(0)))),
            Tuple(Instruction.AddAll, ClosedGoal, SetTerm(goal(0)))
          )))
        }
        case _ => List()
      }
    }).toSeq
    val opResolvers: Seq[Term] = operators.flatMap( o => {
      o(Effects).asCol.flatMap( (eff: Term) => {
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
            Term.collect(_.isInstanceOf[Var])(o\subLocal).distinct.foreach( t => subLocal.add(t, Var(s"${t.asVar.name}_$idTerm")) )

            val oSub = o\subLocal

            val oConsRes: Seq[Term] = oSub(Constraints).asCol.map( c => {
              c match {
                case KeyVal(cType, cs) => Tuple(Instruction.AddAll, cType, cs)
                case _ => throw new IllegalArgumentException(s"Unsupported constraint entry $c.\nPLease use format type:collection")
              }
            }).toSeq

            val r = List(ListTerm(oConsRes ++ List(
              Tuple(Instruction.AddAll, Statement, SetTerm(Tuple(oSub(Interval), KeyVal(oSub(Name), Bool(true))))),
              Tuple(Instruction.AddAll, Statement, oSub(Effects)),
              Tuple(Instruction.AddAll, OpenGoal, oSub(Preconditions)),
              Tuple(Instruction.AddAll, ClosedGoal, SetTerm(goal(0))),
              Tuple(Instruction.AddAll, Temporal, SetTerm(Tuple(Equals, eff(0)\subLocal, goal(0)))),
              Tuple(Instruction.Substitute, sub.asTerm)
            )))
            logger.info(s"Operator resolver: ${oSub(Name)}")

            r
          }
          case _ => List()
        }
      })
    }).toSeq
    logger.depth -= 1
    // TODO: Apply value ordering
    statementResolvers ++ opResolvers
  }
}
