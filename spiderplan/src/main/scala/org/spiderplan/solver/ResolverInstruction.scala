package org.spiderplan.solver

import org.aiddl.core.scala.representation._
import org.spiderplan.solver.SpiderPlan.Instruction

object ResolverInstruction {
  def from(t: Term): ResolverInstruction = {
    t match {
      case Tuple(Instruction.AddAll, k, c: CollectionTerm) => AddAll(k, c)
      case Tuple(Instruction.PutAll, k, c: CollectionTerm) => PutAll(k, c)
      case Tuple(Instruction.Replace, k, c: CollectionTerm) => Replace(k, c)
      case Tuple(Instruction.Substitute, c: CollectionTerm) => Substitute(c)
      case _ => throw new IllegalArgumentException(s"Resolver instruction not supported: ${t}")
    }
  }
}

enum ResolverInstruction {
  case AddAll(key: Term, c: CollectionTerm)
  case PutAll(key: Term, c: CollectionTerm)
  case Replace(key: Term, c: CollectionTerm)
  case Substitute(c: CollectionTerm)
}

//type Resolver = Seq[ResolverInstruction]

object Resolver {
  def apply( instructions: Seq[ResolverInstruction] ): Resolver = new Resolver(instructions, resolverReason = None)
  def apply( instructions: Seq[ResolverInstruction], reason: => Option[String]  ): Resolver = new Resolver(instructions, reason)
}

class Resolver(val instructions: Seq[ResolverInstruction], resolverReason: => Option[String] ) extends Seq[ResolverInstruction] {
  lazy val reason: Option[String] = resolverReason

  def reasonStr: String = reason match {
    case Some(s) => s
    case None => "None"
  }

  def apply(cdb: CollectionTerm): CollectionTerm = {
    import ResolverInstruction._
    instructions.foldLeft(cdb: CollectionTerm)(
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

  override def apply(i: Int): ResolverInstruction = instructions(i)

  override def length: Int = instructions.length

  override def iterator: Iterator[ResolverInstruction] = instructions.iterator
}