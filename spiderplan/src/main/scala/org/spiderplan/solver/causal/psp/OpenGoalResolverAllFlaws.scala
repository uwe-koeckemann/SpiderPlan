package org.spiderplan.solver.causal.psp

import org.aiddl.common.scala.Common.NIL
import org.aiddl.common.scala.planning.PlanningTerm.*
import org.aiddl.common.scala.reasoning.resource.{FlexibilityOrdering, LinearMcsSampler}
import org.aiddl.common.scala.reasoning.temporal.AllenConstraint.Equals
import org.aiddl.core.scala.function.{Function, Verbose}
import org.aiddl.core.scala.representation.*
import org.aiddl.core.scala.util.logger.Logger
import org.spiderplan.solver.ResolverInstruction.*
import org.spiderplan.solver.Solver.FlawResolver
import org.spiderplan.solver.Solver.FlawResolver.Result.{Consistent, Search}
import org.spiderplan.solver.SpiderPlan.Type.*
import org.spiderplan.solver.{Resolver, ResolverInstruction}

import scala.collection.{immutable, mutable}


class OpenGoalResolverAllFlaws(val operators: CollectionTerm ) extends FlawResolver with Verbose {
  logger.name = this.getClass.getSimpleName

  def resolve(cdb: CollectionTerm): FlawResolver.Result = {
    val flawSearch = new OpenGoalResolverGraphSearch
    flawSearch.init(Tuple(cdb, operators))
    flawSearch.includePathLength = true
    //flawSearch.omega = Num(1.0)

    if (flawSearch.isGoal(cdb)) Consistent
    else Search(
      new Iterable[Resolver] {
        override def iterator: Iterator[Resolver] = {
          new Iterator[Resolver] {
            var done = false
            private var nextResolver: Option[Resolver] = None

            override def hasNext: Boolean = {
              if (done) false
              else {
                nextResolver match {
                  case Some(_) => true
                  case None => nextResolver = this.computeNextResolver;
                    nextResolver != None
                }
              }
            }

            def next: Resolver = {
              if (done) throw new NoSuchElementException()
              else this.nextResolver match {
                case Some(r) => this.nextResolver = None;
                  r
                case None => this.nextResolver match {
                  case Some(r) => r
                  case None => throw new NoSuchElementException()
                }
              }
            }

            private def computeNextResolver: Option[Resolver] = {
              var done = false
              var r: Term = NIL
              var count = 0
              while {
                !done
              } do {
                count += 1
                flawSearch.next match {
                  case (NIL, _) => done = true;
                    r = NIL
                  case (n, true) => done = true;
                    r = flawSearch.path(n)
                  case (n, false) => {
                    if (count >= 15)
                      done = true;
                    flawSearch.step(n)
                  }
                }
              }

              if (r == NIL) None
              else
                Some(Resolver(r.asList.flatMap(rs =>
                  rs.asCol.map(ri =>
                    ResolverInstruction.from(ri)))))
            }
          }
        }
      })
  }
}
