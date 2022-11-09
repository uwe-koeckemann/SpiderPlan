package org.spiderplan.solver.resource

import org.aiddl.common.scala.Common.NIL
import org.aiddl.common.scala.reasoning.resource.{FlexibilityOrdering, LinearMcsSampler, PeakCollector}
import org.aiddl.core.scala.function.{Function, Verbose}
import org.aiddl.core.scala.representation.*
import org.spiderplan.solver.Resolver
import org.spiderplan.solver.ResolverInstruction.*
import org.spiderplan.solver.Solver.FlawResolver
import org.spiderplan.solver.SpiderPlan.Type.{PropagatedValue, ResourceReusableCapacity, ResourceReusableUsage, Temporal}

import scala.collection.{immutable, mutable}

class ReusableResourceResolver extends FlawResolver {
  def resolve( cdb: CollectionTerm ): FlawResolver.Result = {
    val caps = cdb.getOrElse(ResourceReusableCapacity, SetTerm.empty).asSet
    val usagesEntries = cdb.getOrElse(ResourceReusableUsage, SetTerm.empty).asSet
    val intervalDomains = cdb(PropagatedValue)

    val usageMap = new mutable.HashMap[Term, immutable.Set[Term]]()
    usagesEntries.foreach( u => {
      usageMap.put(u(1), usageMap.getOrElseUpdate(u(1), Set()) + KeyVal(u(0), u(2)))
    })
    var matchedCaps: Set[Term] = Set.empty
    val usages: SetTerm = SetTerm(usageMap.keys.map( r => {
      if ( r.isGround ) {
        caps.collectFirst(c => {
          {
            c.asKvp.key unify r
          } match {
            case Some(s) => c \ s
          }
        }) match {
          case Some(mc) => {
            matchedCaps = matchedCaps + mc
          }
          case None => throw new IllegalArgumentException(s"No capacity found for resource $r")
        }
        KeyVal(r, SetTerm(usageMap(r)))
      } else {
        NIL
      }
    }).filter(_ != NIL).toSet)

    val groundCaps = SetTerm(matchedCaps)

    val sample = new PeakCollector
    val peaks = sample(Tuple(groundCaps, usages, intervalDomains))

    // TODO: Return Inconsistent when no resolver can work for any flaw
    if ( peaks.length == 0 )
      FlawResolver.Result.Consistent
    else {
      val valueOrdering = new FlexibilityOrdering
      val resolvers = valueOrdering(Tuple(peaks, intervalDomains)).asList.map( r => {
        Resolver(List(AddAll(Temporal, SetTerm(r))), Some(s"$r resolves reusable resource peak"))
      })

      FlawResolver.Result.Search(resolvers)
    }
  }
}
