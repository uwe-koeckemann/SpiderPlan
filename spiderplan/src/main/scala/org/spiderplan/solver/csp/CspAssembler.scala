package org.spiderplan.solver.csp

import org.aiddl.core.scala.representation.*

object CspAssembler {
  def apply( mainCdb: CollectionTerm, cspCdb: CollectionTerm ): Term = {
    val csp = cspCdb.getOrElse(Sym("csp"), SetTerm.empty).asCol
    val domLookup = mainCdb.getOrElse(Sym("domain"), SetTerm.empty).asCol

    var xs: Set[Term] = Set.empty
    var cs: Set[Term] = Set.empty
    var dKeys: Set[Term] = Set.empty
    var ds: Set[Term] = Set.empty

    csp.asCol.foreach(e => e match {
      case KeyVal(Sym("variables"), vars: ListTerm) => xs = xs ++ vars.list.toSet
      case KeyVal(Sym("variables"), vars: SetTerm) => xs = xs ++ vars.set
      case KeyVal(Sym("constraints"), constraints: ListTerm) => cs = cs ++ constraints.list.toSet
      case KeyVal(Sym("constraints"), constraints: SetTerm) => cs = cs ++ constraints.set
      case KeyVal(Sym("domains"), domains: CollectionTerm) =>
        domains.foreach(d => {
          val x = d.asKvp.key
          if (!(dKeys contains x))
            dKeys = dKeys + x

            if (d.asKvp.value.isInstanceOf[Sym])
              ds = ds + KeyVal(x, domLookup(d.asKvp.value))
            else
              ds = ds + d
        })
      case _ => println(s"Ignoring unknown expression $e in CSP constraint type")
    })

    val actualVariables = xs.filter(_.isInstanceOf[Var]).toVector

    val cspNew = Tuple(
      KeyVal(Sym("variables"), ListTerm(actualVariables)),
      KeyVal(Sym("constraints"), SetTerm(cs)),
      KeyVal(Sym("domains"), SetTerm(ds))
    )

    cspNew
  }
}
