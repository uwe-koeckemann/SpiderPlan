package org.spiderplan.solver

import org.aiddl.core.scala.representation.{CollectionTerm, Num}

trait Heuristic {
  def apply( cdb: CollectionTerm ): Num
}
