package org.spiderplan.solver

import org.aiddl.core.scala.representation._

object SpiderPlan {
  object Instruction {
    val AddAll = Sym("add-all")
    val PutAll = Sym("put-all")
    val Replace = Sym("relplace")
    val Substitute = Sym("substitute")
  }
  object Type {
    val Csp = Sym("csp")
    val Domain = Sym("domain")
    val Signature = Sym("signature")
    val Statement = Sym("statement")
    val Temporal = Sym("temporal")
    val PropagatedValue = Sym("propagated-value")
    val OpenGoal = Sym("goal")
    val ClosedGoal = Sym("goal.sat")
    val Operator = Sym("operator")
    val ResourceReusableUsage = Sym("resource.reusable.usage")
    val ResourceReusableCapacity = Sym("resource.reusable.capacity")
  }

  def cdbMerge( a: CollectionTerm, b: CollectionTerm ): CollectionTerm =
    b.foldLeft(a: CollectionTerm)( (c, n) =>
      c.put(KeyVal(
        n.asKvp.key,
        c.getOrElse(n.asKvp.key, SetTerm.empty).asCol.addAll(n.asKvp.value.asCol))) )

  def applyResolver( cdb: CollectionTerm, resolver: org.spiderplan.solver.Resolver ): CollectionTerm = {
    import SpiderPlan._
    import ResolverInstruction._
    resolver.foldLeft(cdb: CollectionTerm)(
      (c, inst) => {
        inst match {
          case AddAll(key, cs) => c.put(KeyVal(key, c.getOrElse(key, SetTerm.empty).asCol.addAll(cs)))
          case PutAll(key, cs) => c.put(KeyVal(key, c.getOrElse(key, SetTerm.empty).asCol.putAll(cs)))
          case Replace(key, cs) => c.put(KeyVal(key, cs))
          case Substitute(s) => (c \ Substitution.from(s.asCol)).asCol
        }
      }
    )
  }

  def applyResolverSeq( cdb: CollectionTerm, rs: Seq[org.spiderplan.solver.Resolver] ): CollectionTerm = {
    rs.foldLeft(cdb: CollectionTerm)( (c, r) => applyResolver(c, r) )
  }
}

trait SpiderPlan {
  def solve( cdb: CollectionTerm ): Option[CollectionTerm]
  def explain = {}
}