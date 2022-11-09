package org.spiderplan.solver.conditional

import org.aiddl.core.scala.representation.Sym

object ConditionalConstraint {
  val Name = Sym("name")
  val Signature = Sym("signature")
  val Id = Sym("id")
  val Conditional = Sym("conditional")
  val ConditionalSat = Sym("conditional.sat")
  val Condition = Sym("condition")
  val Resolvers = Sym("resolvers")
}
