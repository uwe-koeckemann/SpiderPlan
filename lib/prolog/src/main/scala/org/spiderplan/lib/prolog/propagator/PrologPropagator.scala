package org.spiderplan.lib.coordination_oru.propagator

import org.aiddl.common.scala.Common
import org.aiddl.core.scala.function.Verbose
import org.aiddl.core.scala.parser.Parser
import org.aiddl.core.scala.representation.*
import org.aiddl.core.scala.util.FilenameResolver
import org.aiddl.external.scala.prolog.QuerySolver
import org.spiderplan.solver.Solver.Propagator
import org.spiderplan.solver.ResolverInstruction.Substitute

class PrologPropagator extends Propagator with Verbose {
  val solver = new QuerySolver

  override def propagate(cdb: CollectionTerm): Propagator.Result = {
    val cs = cdb.getOrElse(Sym("prolog"), SetTerm.empty).asCol

    var kbs: Map[Sym, CollectionTerm] = Map.empty
    var queries: Set[(CollectionTerm, Term)] = Set.empty

    cs.foreach( e => {
      e match {
        case Tuple(Sym("kb"), name: Sym, kb: CollectionTerm) => kbs = kbs.updated(name, kb)
        case Tuple(Sym("query"), q: CollectionTerm, kb) => if (!q.isGround) then queries = queries.+((q, kb))
        case _ => throw new IllegalArgumentException(s"Unsupported Prolog constraint expression: $e")
      }
    })

    var subs: List[CollectionTerm] = Nil

    val consistent = queries.forall( (q, kb_term) => {
      val kb = if ( kb_term.isInstanceOf[Sym] ) kbs(kb_term.asSym) else kb_term.asCol
      val answer = solver(Tuple(q, kb))

      answer != Common.NIL
    })

    logger.info(s"$cs")

    logger.info(s"Consistent? $consistent")

    if !consistent then Propagator.Result.Inconsistent
    else Propagator.Result.Consistent
  }
}
