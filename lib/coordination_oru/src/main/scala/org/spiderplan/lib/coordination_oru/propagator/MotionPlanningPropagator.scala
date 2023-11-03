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
import org.spiderplan.solver.{PruneRuleGenerator, Resolver}
import org.spiderplan.solver.ResolverInstruction.Substitute
import org.spiderplan.solver.ResolverInstruction.Replace
import org.spiderplan.solver.SpiderPlan.Type.Operator

class MotionPlanningPropagator extends Propagator with Verbose with PruneRuleGenerator {
  val parser = new Parser(new Container())

  var pruneRule: Option[CollectionTerm => Option[Resolver]] = None

  val planner = MotionPlannerFactory.fromAiddl(parser.str("[\n" +
    "model:ReedsSheppCar\n" +
    "algorithm:RRTConnect\n" +  // RRTConnect, RRTstar, TRRT, SST, LBTRRT, PRMstar, SPARS, pRRT, LazyRRT
    "footprint:[(-1.0 0.5) (1.0 0.5) (1.0 -0.5) (-1.0 -0.5)]\n" +
    "radius:1.0\n" +
    "turning-radius:4.0\n" +
    "distance-between-path-points:0.5\n" +
    "]"))

  override def propagate(cdb: CollectionTerm): Propagator.Result = {
    val cs = cdb.getOrElse(Sym("motion"), SetTerm.empty).asCol
    pruneRule = None
    logger.info(s"$cs")

    val pathSub = new Substitution()

    val (frames, maps, poses, robots, plannerCfg) = MotionPlanner.extract(cs)
    var failureCon: Option[Term] = None

    val consistent = cs.forall(c => c match {
      case Tuple(Sym("path"), id, r, l1, l2, map, pathVar: Var) if (!l1.isInstanceOf[Var] && !l2.isInstanceOf[Var]) =>
        val plannerConfig = plannerCfg(map)
        val planner = MotionPlannerFactory.fromPlannerAndRobotCfg(plannerConfig, robots(r))
        logger.info(s"Planning for: $c with ${robots(r)} and $plannerConfig")
        planner.setMap(maps(map))
        planner.setStart(term2pose(poses(map)(l1)))
        planner.setGoals(term2pose(poses(map)(l2)))
        if !planner.plan() then {
          failureCon = Some(c)
          false
        }
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
      case _ => true //throw new IllegalArgumentException(s"Unknown motion expression: $c")
    })

    logger.info(s"Consistent? $consistent Paths: ${pathSub.toString()}")

    if (!consistent) { // Remember pruning rule so spider-plan can add it to a pruning propagator
      failureCon match {
        case Some(failCon) => {
          this.pruneRule = Some((cdb: CollectionTerm) => {
            val ops = SetTerm(cdb(Operator).asCol.filterNot( o =>
              o.asCol.getOrElse(Sym("motion"), SetTerm.empty).asCol.contains(failCon)
            ).toSet)
            Some(Resolver(List(
              Replace(Operator, ops)
            )))
          })
        }
        case None => {}
      }

    }

    if !consistent then Propagator.Result.Inconsistent
    else if pathSub.isEmpty then Propagator.Result.Consistent
    else Propagator.Result.ConsistentWith(Resolver(List(Substitute(pathSub.asTerm)), Some(() => {"Adding Path"})))
  }

  override def getRuleFromLastFailure: Option[CollectionTerm => Option[Resolver]] = this.pruneRule

}
