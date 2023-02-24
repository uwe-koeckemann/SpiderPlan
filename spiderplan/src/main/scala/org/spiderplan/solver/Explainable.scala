package org.spiderplan.solver

import org.aiddl.core.scala.representation.{CollectionTerm, Term}

trait Explainable {
  def explain(cdb: CollectionTerm): String
}
