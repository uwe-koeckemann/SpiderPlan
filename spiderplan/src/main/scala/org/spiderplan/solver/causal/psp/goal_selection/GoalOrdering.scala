package org.spiderplan.solver.causal.psp.goal_selection

import org.aiddl.core.scala.representation.Term

trait GoalOrdering {
  def add(goals: Seq[Term]): GoalOrdering

  def pop: GoalOrdering

  def next: Term
}
