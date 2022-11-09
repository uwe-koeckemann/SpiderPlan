package org.spiderplan.solver.domain

import org.aiddl.common.scala.Common.NIL
import org.aiddl.core.scala.function.{Function, Verbose}
import org.aiddl.core.scala.representation.*
import org.spiderplan.solver.Solver.Propagator
import org.spiderplan.solver.SpiderPlan.Type.{Domain, Signature, Statement}

import scala.collection.immutable.HashMap
import scala.collection.{immutable, mutable}


// TODO: add substitution for variables with only one possible value
// TODO: add support for numerical domains (with optional ranges)

class DomainConstraintSolver extends Propagator with Verbose {
  logger.name = this.getClass.getSimpleName

  override def propagate(cdb: CollectionTerm): Propagator.Result =
    if (check(cdb)) Propagator.Result.Consistent
    else Propagator.Result.Inconsistent

  private def getLookupKey(s: Term): (Term, Int) = s match {
    case Tuple(name, x@_*) => (name, x.length)
    case name => (name, 0)
  }

  private def check( cdb: Term ): Boolean = {
    val domains = cdb.getOrElse(Domain, SetTerm.empty).asCol
    val signatures = cdb.getOrElse(Signature, SetTerm.empty).asCol
    val statements = cdb.getOrElse(Statement, SetTerm.empty).asCol
    val sigLookup: Map[(Term, Int), Term] = signatures.map( sig => getLookupKey(sig.asKvp.key) -> sig).toMap

    val varDomains: mutable.Map[Term, Set[Term]] = new mutable.HashMap()

    val isConsistent = statements.forall( stmt => {
      val sig = sigLookup.get(getLookupKey(stmt(1).asKvp.key))

      (stmt, sig) match {
        case (Tuple(_, KeyVal(x, v)), Some(KeyVal(xSig, vSig))) => {
          val argsOkay = !x.isInstanceOf[Tuple] || (1 until x.length).forall( i => {
            if ( !domains.containsKey(xSig(i)) )
              throw new IllegalStateException(
                s"Type ${xSig(i)} not found in domains. Known domain keys: ${domains.map(_.asKvp.key).mkString(", ")}\n" +
                  s"-> Add missing domain to CDB or rename missing key to known key.")
            checkAndUpdateVariables(x(i), xSig(i), varDomains, domains) && (x(i).isInstanceOf[Var] || xSig(i).isInstanceOf[Var] || (domains(xSig(i)).asCol contains x(i)))
          })
          val valOkay = checkAndUpdateVariables(v, vSig, varDomains, domains) && (v.isInstanceOf[Var] || vSig.isInstanceOf[Var] || (domains(vSig).asCol contains v))

          val r = argsOkay && valOkay
          if ( !r ) this.logger.info(s"Bad signature for ${stmt(1)} with $sig.")

          r
        }
        case (_, None) => throw new IllegalArgumentException(s"Statement $stmt does not have a matching signature.")
        case _ => throw new IllegalArgumentException(s"Statement $stmt or signature $sig do not have the right form. " +
          s"Statement should be tuple with key-value pair as second element. Signature should be key-value pair.")
      }
    })
    //new ResolverSequenceIterator(isConsistent, if ( isConsistent ) List(ListTerm.empty) else List.empty )

    isConsistent
  }

  def domainSat(domain: Term, value: Term): Boolean = {
    domain match {
      case d: CollectionTerm => d contains value
      case Tuple(Sym("range"), Tuple(l: Num, u: Num)) => l <= value.asNum && value.asNum <= u
      case Tuple(Sym("range"), Tuple(l: Num, u: Num), options: CollectionTerm) => {
        {
          options.get(Sym("type")) match {
            case Some(Sym("int")) => value.isInstanceOf[Integer]
            case Some(Sym("real")) => value.isInstanceOf[Real]
            case Some(Sym("rational")) => value.isInstanceOf[Rational]
            case None => true
            case Some(bad) => throw new IllegalArgumentException(s"type option of domain must be one of: int, rational or real (was: $bad)")
          }
        } && {
          options.get(Sym("include-lower")) match {
            case None | Some(Bool(true) ) => l <= value.asNum
            case Some(Bool(false)) => l < value.asNum
            case Some(bad) => throw new IllegalArgumentException(s"include-lower option of domain range must have boolean value true or false (was: $bad).")
          }
        } && {
          options.get(Sym("include-upper")) match {
            case None | Some(Bool(true)) => value.asNum <= u
            case Some(Bool(false)) => value.asNum < u
            case Some(bad) => throw new IllegalArgumentException(s"include-upper option of domain range must have boolean value true or false (was: $bad).")
          }
        }
      }
      case _ => throw new IllegalArgumentException(s"Not a valid domain constraint expression: $domain")
    }
  }

  def checkAndUpdateVariables( x: Term, t: Term, varDomains: mutable.Map[Term, Set[Term]], domains: CollectionTerm ): Boolean = {
    if ( x.isInstanceOf[Var] && !t.isInstanceOf[Var] ) {
      if ( !varDomains.contains(x) ) {
        val feasible: Set[Term] = domains(t).asSet.set
        varDomains.put(x, feasible)
        true
      } else {
        val feasible = varDomains(x) intersect domains(t).asSet.set
        if ( feasible.isEmpty ) {
          false
        } else {
          varDomains.put(x, feasible)
          true
        }
      }
    } else {
      true
    }
  }
}