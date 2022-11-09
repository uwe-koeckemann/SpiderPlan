package org.spiderplan.lib.coordination_oru.propagator

import com.vividsolutions.jts.geom.Coordinate
import org.aiddl.core.scala.container.Container
import org.aiddl.core.scala.function.Verbose
import org.aiddl.core.scala.parser.Parser
import org.aiddl.core.scala.representation.*
import org.aiddl.core.scala.util.FilenameResolver
import org.aiddl.external.scala.coordination_oru.factory.MotionPlannerFactory
import org.aiddl.external.scala.coordination_oru.util.Convert.{poseSteering2term, term2frame, term2pose}
import org.spiderplan.lib.coordination_oru.motion.MotionPlanner
import org.spiderplan.solver.Solver.Propagator
import org.spiderplan.solver.Resolver
import org.spiderplan.solver.ResolverInstruction.Substitute

class MotionPlanningPropagator extends Propagator with Verbose {
  val parser = new Parser(new Container())

  val planner = MotionPlannerFactory.fromAiddl(parser.str("[\n" +
    "type:ReedsSheppCarPlanner\n" +
    "algorithm:RRTConnect\n" +
    "radius:0.2\n" +
    "turning-radius:4.0\n" +
    "distance-between-path-points:0.5\n" +
    "]"))

  override def propagate(cdb: CollectionTerm): Propagator.Result = {
    val cs = cdb.getOrElse(Sym("motion"), SetTerm.empty).asCol

    logger.info(s"$cs")

    val pathSub = new Substitution()

    val (frames, maps, poses) = MotionPlanner.extract(cs)

    val consistent = cs.forall(c => c match {
      case Tuple(Sym("path"), id, r, l1, l2, map, pathVar: Var) if (!l1.isInstanceOf[Var] && !l2.isInstanceOf[Var]) =>
        logger.info(s"Planning for: $c")
        planner.setMap(maps(map))
        planner.setFootprint(frames(r)*)
        planner.setStart(term2pose(poses(map)(l1)))
        planner.setGoals(term2pose(poses(map)(l2)))
        if !planner.plan() then false
        else {
          val path = ListTerm(planner.getPath.map(p => poseSteering2term(p)).toVector)
          pathSub.add(pathVar, path)
          true
        }
      case Tuple(Sym("path"), _, _, _, _, _, _) => true
      case Tuple(Sym("map"), _, _) => true
      case Tuple(Sym("frame"), _, _: ListTerm) => true
      case Tuple(Sym("poses"), _, _: CollectionTerm) => true
      case Tuple(Sym("coordinate"), _, _) => true
      case _ => throw new IllegalArgumentException(s"Unknown motion expression: $c")
    })

    logger.info(s"Consistent? $consistent Paths: ${pathSub.toString()}")

    if !consistent then Propagator.Result.Inconsistent
    else if pathSub.isEmpty then Propagator.Result.Consistent
    else Propagator.Result.ConsistentWith(Resolver(List(Substitute(pathSub.asTerm)), Some("Adding Path")))
  }
}
