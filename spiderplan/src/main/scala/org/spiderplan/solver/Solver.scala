package org.spiderplan.solver

import org.aiddl.core.scala.function.{Function, Initializable}
import org.aiddl.core.scala.representation.*

object Solver {
  object Propagator {
    enum Result {
      case Consistent
      case Inconsistent
      case ConsistentWith(r: org.spiderplan.solver.Resolver)
    }
  }
  trait Propagator {
    def propagate(cdb: CollectionTerm): Propagator.Result
  }
  object FlawResolver {
    enum Result {
      case Consistent
      case Inconsistent
      case Search(rs: Iterable[org.spiderplan.solver.Resolver])
    }
  }
  trait FlawResolver {
    def resolve(x: CollectionTerm): FlawResolver.Result
  }
}
