package org.spiderplan.lib.coordination_oru.motion

import com.vividsolutions.jts.geom.Coordinate
import org.aiddl.common.scala.execution.Actor
import org.aiddl.core.scala.container.Container
import org.aiddl.core.scala.parser.Parser
import org.aiddl.core.scala.representation.*
import org.aiddl.core.scala.util.FilenameResolver
import org.aiddl.external.scala.coordination_oru.MotionPlanningTerm.MapKey
import org.aiddl.external.scala.coordination_oru.actor.CoordinationActor
import org.aiddl.external.scala.coordination_oru.factory.MotionPlannerFactory
import org.aiddl.external.scala.coordination_oru.util.Convert.{term2frame, term2pose}
import se.oru.coordination.coordination_oru.RobotAtCriticalSection
import se.oru.coordination.coordination_oru.simulation2D.TrajectoryEnvelopeCoordinatorSimulation
import se.oru.coordination.coordination_oru.util.{BrowserVisualization, Missions}

import java.util.Comparator

object MotionPlanner {
  val parser = new Parser(new Container())
  val defaultPlannerCfg = parser.str("[\n" +
    "model:ReedsSheppCar\n" +
    "algorithm:LazyRRT\n" +  // RRTConnect, RRTstar, TRRT, SST, LBTRRT, PRMstar, SPARS, pRRT, LazyRRT
    "radius:0.1\n" +
    "turning-radius:4.0\n" +
    "distance-between-path-points:0.5\n" +
    "]").asCol

  def extract(cs: CollectionTerm): (Map[Term, Array[Coordinate]], Map[Term, String], Map[Term, CollectionTerm], Map[Term, CollectionTerm], Map[Term, CollectionTerm]) = {
    var frames: Map[Term, Array[Coordinate]] = Map.empty
    var robots: Map[Term, CollectionTerm] = Map.empty
    var maps: Map[Term, String] = Map.empty
    var poses: Map[Term, CollectionTerm] = Map.empty
    var planner: Map[Term, CollectionTerm] = Map.empty.withDefaultValue(defaultPlannerCfg)

    cs.foreach(c => c match {
      case Tuple(Sym("map"), name, fn) =>
        val yamlFile = FilenameResolver(fn).asStr.value
        maps = maps.updated(name, yamlFile)
      case Tuple(Sym("frame"), id, f: ListTerm) =>
        frames = frames.updated(id, term2frame(f))
      case Tuple(Sym("robot"), id, cfg) =>
        robots = robots.updated(id, cfg.asCol)
      case Tuple(Sym("planner"), mapId, cfg) =>
        planner = planner.updated(mapId, cfg.asCol)
      case Tuple(Sym("poses"), map, p: CollectionTerm) =>
        poses = poses.updated(map, p)
      case _ => {}
    })
    (frames, maps, poses, robots, planner)
  }

  def createCoordinators(cs: CollectionTerm): List[CoordinationActor] = {
    val (frames, maps, poses, robots, plannerConfig) = MotionPlanner.extract(cs)
    var actors: List[CoordinationActor] = Nil

    cs.foreach( c => c match {
      case Tuple(Sym("coordinate"), name, cfg) => {
        val MAX_ACCEL = cfg(Sym("max-accel")).intoDouble
        val MAX_VEL = cfg(Sym("max-vel")).intoDouble
        val map = cfg.get(MapKey).flatMap(f => Some(maps(f)))
        var ids: Map[Term, Int] = Map.empty
        val tec = new TrajectoryEnvelopeCoordinatorSimulation(MAX_VEL, MAX_ACCEL)

        tec.addComparator(new Comparator[RobotAtCriticalSection]() {
          override def compare(o1: RobotAtCriticalSection, o2: RobotAtCriticalSection): Int = {
            val cs = o1.getCriticalSection
            val robotReport1 = o1.getRobotReport
            val robotReport2 = o2.getRobotReport
            (cs.getTe1Start - robotReport1.getPathIndex) - (cs.getTe2Start - robotReport2.getPathIndex)
          }
        })
        tec.addComparator(new Comparator[RobotAtCriticalSection]() {
          override def compare(o1: RobotAtCriticalSection, o2: RobotAtCriticalSection): Int = o2.getRobotReport.getRobotID - o1.getRobotReport.getRobotID
        })

        //Need to setup infrastructure that maintains the representation
        tec.setupSolver(0, 100000000)
        //Start the thread that checks and enforces dependencies at every clock tick
        tec.startInference

        tec.setBreakDeadlocks(false, false, true)
        var robotIds: List[Int] = Nil
        var nextId = 1
        cfg(Sym("robots")).asCol.foreach( r => {
          val planner = MotionPlannerFactory.fromPlannerAndRobotCfg(plannerConfig(cfg(MapKey)), robots(r))
          //val planner = MotionPlannerFactory.fromAiddl(defaultPlannerCfg)
          //planner.setFootprint(frames(r)*)

          map match {
            case Some(yamlFile) => planner.setMap(yamlFile)
            case None => {}
          }
          tec.setMotionPlanner(nextId, planner)
          val pose = term2pose(poses(name)(cfg(Sym("start-pose"))(r)))
          tec.placeRobot(nextId, pose)
          ids = ids.updated(r, nextId)
          robotIds = nextId :: robotIds
          nextId += 1
        })

        tec.setQuiet(true)

        val viz = new BrowserVisualization
        viz.setInitialTransform(20.0, 9.0, 2.0)
        viz.setFontScale(1.6)
        map match {
          case Some(yamlFile) => viz.setMap(yamlFile)
          case None => {}
        }
        tec.setVisualization(viz)
        tec.setUseInternalCriticalPoints(true)

        Missions.startMissionDispatchers(tec, false, robotIds*)

        val pattern = cfg(Sym("extractor"))(0)
        val vars = cfg(Sym("extractor"))(1)

        val a = new CoordinationActor(pattern, vars, ids, tec)
        actors = a :: actors
      }
      case _ => {}
    })
    actors
  }
}
