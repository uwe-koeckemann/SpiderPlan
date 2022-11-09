package org.spiderplan.tool

import org.aiddl.core.scala.representation._
import org.aiddl.common.scala.search.GenericTreeSearch

class SubstitutionCombiner(options: Seq[Seq[Substitution]]) extends GenericTreeSearch[Substitution, Substitution] {
  override val nil: Substitution = new Substitution()

  override def assembleSolution(choice: List[Substitution]): Option[Substitution] =
    choice.foldLeft( Some(new Substitution()): Option[Substitution] )( (c, n) => c.flatMap( _ + n ))

  override def expand: Option[Seq[Substitution]] =
    if ( depth < options.length ) Some(options(depth))
    else None

  override def isConsistent: Boolean = {
    val accSub = Some(new Substitution())
    for {
      sub <- choice
    } {
      accSub.flatMap(acc => acc + sub)
    }
    accSub != None
  }

}
