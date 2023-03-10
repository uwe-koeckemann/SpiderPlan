package org.spiderplan.solver

import org.aiddl.core.scala.representation.CollectionTerm

trait PruneRuleGenerator {

  def getRuleFromLastFailure: Option[CollectionTerm => Option[Resolver]]

}
