package org.spiderplan.solver.pruning

import org.aiddl.core.scala.representation.CollectionTerm
import org.aiddl.core.scala.function.Verbose
import org.spiderplan.solver.Resolver
import org.spiderplan.solver.Solver.Propagator

class PruningPropagator extends Propagator with Verbose {
  var pruningRules: List[CollectionTerm => Option[Resolver]] = Nil

  override def propagate(cdb: CollectionTerm): Propagator.Result =
    var currentCdb = cdb
    val answer =
      pruningRules
        .foldLeft(Some(Resolver.empty): Option[Resolver])((c, r) => {
          c.flatMap( c_r => r(currentCdb).flatMap( r => {
            currentCdb = r.apply(currentCdb)
            Some(c_r + r)
          }))
        })

    answer match {
      case Some(resolver) => Propagator.Result.ConsistentWith(resolver)
      case None => Propagator.Result.Inconsistent
    }
}
