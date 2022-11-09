package org.spiderplan.solver.conditional

import org.aiddl.core.scala.function.Verbose
import org.aiddl.core.scala.representation.*
import org.aiddl.core.scala.util.logger.Logger
import ConditionalConstraint.*
import org.spiderplan.solver.ResolverInstruction.AddAll
import org.spiderplan.solver.Solver.FlawResolver
import org.spiderplan.solver.Solver.FlawResolver.Result
import org.spiderplan.solver.SpiderPlan.Type.*
import org.spiderplan.solver.{Resolver, SpiderPlan}
import org.spiderplan.tool.SubstitutionCombiner

class ConditionalConstraintResolver(subSolver: SpiderPlan) extends FlawResolver with Verbose {
  var nextFreeId = 1
  logger.name = this.getClass.getSimpleName

  override def resolve(cdb: CollectionTerm): Result = {
    val ccs = cdb.getOrElse(Conditional, SetTerm.empty).asCol
    val ccsSat = cdb.getOrElse(ConditionalSat, SetTerm.empty).asCol
    val statements = cdb.getOrElse(Statement, SetTerm.empty).asCol

    val resolvers = ccs.flatMap( cc => {
      val id = cc(Id)
      val condition = cc(Condition)
      val condStatements = condition(Statement).asCol

      val options = condStatements.map( a => {
        statements.map( b => a unify b ).flatten.toVector
      }).toVector

      val matchPossible = options.forall( o => !o.isEmpty )

      if ( matchPossible ) {
        val combiner = new SubstitutionCombiner(options)
        var matches: List[Substitution] = Nil
        while {
          combiner.search.flatMap( s => {
            this.nextFreeId += 1;
            s.add(id, Num(this.nextFreeId - 1))
          }) match {
            case None => false
            case Some(option) =>
              logger.fine(s"Matching substitution: $option")
              matches = option :: matches
              true
          }
        } do {}

        val condCheckCCs = matches.map( s => cc \ s).filter( cc => cc(Name).isGround && !ccsSat.contains(cc(Name)) )

        val ccResolvers: Seq[Resolver] = condCheckCCs.flatMap( cc => {
          val condCdb = SpiderPlan.cdbMerge(cdb, cc(Condition).asCol)
          if ( subSolver.solve(condCdb).isDefined ) {
            cc(Resolvers).asCol.map( r => {
              val resolver: Resolver = Resolver(AddAll(ConditionalSat, SetTerm(cc(Name))) :: r.asCol.map( entry => entry match {
                case KeyVal(key, col: CollectionTerm) => AddAll(key, col)
                case _ => throw new IllegalArgumentException(s"Resolver $r\nhas entry $entry that is not a key-value pair with a collection value.")
              }).toList)
              resolver
            })
          } else {
            Nil
          }
        })
        ccResolvers
      } else {
        Nil
      }
    })

    if ( resolvers.isEmpty )
      logger.info("No conditional applies.")
      Result.Consistent
    else
      logger.fine("Resolvers:")
      logger.depth += 1
      resolvers.foreach(r => logger.fine(s"$r"))
      logger.depth -= 1
      Result.Search(resolvers)
  }
}
