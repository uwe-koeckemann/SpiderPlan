package org.spiderplan.solver.causal.psp.goal_selection

import org.aiddl.core.scala.representation.Term

case class GoalOrderingAllFifo(queue: List[Term]) extends GoalOrdering {
  def add(goals: Seq[Term]): GoalOrdering = GoalOrderingAllFifo(queue ++ goals)
  def pop: GoalOrderingAllFifo = GoalOrderingAllFifo(queue.tail)
  def next: Term = queue.head
}
