package org.spiderplan.solver.csp

import org.aiddl.common.scala.reasoning.constraint.ConstraintTerm.{Variables, Constraints}
import org.aiddl.core.scala.util.logger.Logger
import org.aiddl.core.scala.function.Verbose
import org.aiddl.core.scala.representation.*
import org.aiddl.common.scala.reasoning.constraint.CspSolver
import org.spiderplan.solver.Solver.FlawResolver
import org.spiderplan.solver.Solver.FlawResolver.Result
import org.spiderplan.solver.conditional.ConditionalConstraint.*
import org.spiderplan.solver.ResolverInstruction.AddAll
import org.spiderplan.solver.{Resolver, ResolverInstruction, SpiderPlan}
import org.spiderplan.solver.SpiderPlan.Type.*
import org.spiderplan.tool.SubstitutionCombiner


class CspResolver extends FlawResolver with Verbose {
  logger.name = this.getClass.getSimpleName

  def resolve( cdb: CollectionTerm ): Result = {
    val csp = CspAssembler(cdb, cdb)

    logger.info(s"Open variables: ${csp(Variables).asCol.mkString("[", ", ", "]")}")

    if ( csp(Variables).asCol.isEmpty ) {

      val consSat = csp(Constraints).asCol.forall( c => {
        val args = c(0)
        val fCon = c(1).asFunRef
        fCon(args).asBool.v
      })
      logger.info(s"No open variables. Consistent: $consSat")
      if (consSat)
        Result.Consistent
      else
        Result.Inconsistent
    }
    else {
      val solver = new CspSolver {
        checkWithGroundArgsOnly = true
      }

      logger.info(s"Solving for open variables")
      logger.fine("CSP", Logger.prettyPrint(csp, 1))

      solver.init(csp)

      Result.Search(new Iterable[Resolver] {
        override def iterator: Iterator[Resolver] = {
          new Iterator[Resolver] {
            var done = false
            private var nextResolver: Option[Resolver] = None

            override def hasNext: Boolean = {
              if (done) false
              else {
                nextResolver match {
                  case Some(_) => true
                  case None =>
                    nextResolver = this.computeNextResolver
                    done = nextResolver == None
                    nextResolver != None
                }
              }
            }

            def next: Resolver = {
              if (done) throw new NoSuchElementException()
              else this.nextResolver match {
                case Some(r) =>
                  this.nextResolver = None
                  r
                case None => this.computeNextResolver match {
                  case Some(r) => r
                  case None => throw new NoSuchElementException()
                }
              }
            }

            private def computeNextResolver: Option[Resolver] = {
              var done = false
              solver.search match {
                case None => done = true; None
                case Some(a) => {
                  Some(Resolver(List(ResolverInstruction.Substitute(ListTerm(a)))))
                }

              }
            }
          }
        }
      })
    }
  }
}
