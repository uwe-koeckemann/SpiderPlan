package org.spiderplan.solver

import org.aiddl.common.scala.Common.NIL
import org.aiddl.common.scala.search.GenericGraphSearch
import org.aiddl.core.scala.representation.CollectionTerm
import org.aiddl.core.java.tools.StopWatch
import org.aiddl.core.scala.function.{Function, Verbose}
import org.aiddl.core.scala.representation.*
import org.aiddl.core.scala.util.logger.Logger
import org.spiderplan.solver.Solver.Propagator
import org.spiderplan.solver.Solver.FlawResolver
import org.spiderplan.solver.ResolverInstruction.*

import org.aiddl.core.scala.representation.conversion.given_Conversion_Term_Num

import scala.language.implicitConversions

trait SpiderPlanGraphSearch extends GenericGraphSearch[Resolver, CollectionTerm] with SpiderPlan {
  val preprocessors: Vector[Function]
  val solvers: Vector[FlawResolver]
  val propagators: Vector[Propagator]
  val heuristics: Vector[Heuristic]

  logger.name = this.getClass.getSimpleName

  override def logGetVerboseSubComponents: List[Verbose] = {
    (preprocessors.withFilter(_.isInstanceOf[Verbose]).map(_.asInstanceOf[Verbose])
      ++ solvers.withFilter(_.isInstanceOf[Verbose]).map(_.asInstanceOf[Verbose])
      ++ propagators.withFilter(_.isInstanceOf[Verbose]).map(_.asInstanceOf[Verbose])
      ++ heuristics.withFilter(_.isInstanceOf[Verbose]).map(_.asInstanceOf[Verbose])).toList
  }

  override def solve( cdb: CollectionTerm): Option[CollectionTerm] =
    val preCdb = preprocessors.foldLeft(cdb: Term)( (c, f) => {
      StopWatch.start(s"[Preprocessor] ${this.nameOf(f)}")
      val r = f(c)
      StopWatch.stop(s"[Preprocessor] ${this.nameOf(f)}")
      r
    }).asCol
    this.init(List(preCdb))
    search.flatMap( rs => Some(SpiderPlan.applyResolverSeq(preCdb, rs)))

  override def h(cdb: CollectionTerm): Num =
    if ( heuristics.isEmpty ) Num(0)
    else {
      heuristics.map(h => {
        StopWatch.start(s"[Heuristic] ${this.nameOf(h)}")
        val value = h(cdb)
        StopWatch.stop(s"[Heuristic] ${this.nameOf(h)}")
        value
      }).reduce(_ + _)
    }

  override def propagate(n: CollectionTerm): Option[(CollectionTerm, Option[Resolver])] = {
    assert(currentNode == n)
    propagationEdge match {
      case Some((edge, cdb)) =>
        if ( edge.isEmpty ) {
          Some((n, None))
        } else {
          if ( n == cdb ) Some(n, None)
          else Some((cdb, Some(edge)))
        }
      case None => None
    }
  }

  override def expand(n: CollectionTerm): Seq[(Resolver, CollectionTerm)] = {
    flawResolvers match {
      case None => Nil
      case Some(resolvers) =>
        resolvers.map( r => (r, SpiderPlan.applyResolver(n, r)) )
    }
  }

  override def f(n: CollectionTerm): Num = {
    val r = super.f(n)
    r
  }

  var currentNode: CollectionTerm = _
  var propagationEdge: Option[(Resolver, CollectionTerm)] = None
  var flawResolvers: Option[Seq[Resolver]] = None

  private def nameOf( p: Any ): String = {
    if (p.isInstanceOf[Verbose]) p.asInstanceOf[Verbose].logName else p.getClass.getSimpleName
  }

  override def isGoal(n: CollectionTerm): Boolean = {
    // isGoal is called before expand and propagate and requires their results
    // so we do everything here and just look-up results later
    // this should work because we never ask for expansion or propagation of a node that
    // has not been checked to be a goal first
    currentNode = n
    propagationEdge = None
    flawResolvers = None

    var propChanges: Resolver = Resolver(Nil, Some(""))
    var cdb = n
    var propList: List[Propagator.Result] = Nil

    val pCon = this.propagators.forall( p => {
      StopWatch.start(s"[Propagator] ${this.nameOf(p)}")
      val r = p.propagate(cdb)
      StopWatch.stop(s"[Propagator] ${this.nameOf(p)}")
      propList = r :: propList
      r match {
        case Propagator.Result.Consistent => true
        case Propagator.Result.Inconsistent => {
          logger.info(s"Rejected by propagator: ${this.nameOf(p)}")
          false
        }
        case Propagator.Result.ConsistentWith(changes) =>
          cdb = SpiderPlan.applyResolver(cdb, changes)
          propChanges = Resolver(propChanges ++ changes, Some("Propagation"))
          true
      }
    })

    if ( pCon ) {
      var isGoal = true
      var resolverList: List[Resolver] = Nil
      var fCon = true

      this.solvers.forall( s => {
        StopWatch.start(s"[Solver] ${this.nameOf(s)}")
        val r = s.resolve(cdb)
        StopWatch.stop(s"[Solver] ${this.nameOf(s)}")
        r match {
          case FlawResolver.Result.Consistent => {
            logger.info(s"Accepted by resolver: ${this.nameOf(s)}")
            fCon = true
            true
          }
          case FlawResolver.Result.Inconsistent => {
            logger.info(s"Rejected by resolver: ${this.nameOf(s)}")
            isGoal = false
            fCon = false
            false
          }
          case FlawResolver.Result.Search(resolvers) => {
            logger.info(s"Expanded by resolver: ${this.nameOf(s)}")
            resolverList = resolverList ++ resolvers
            isGoal = false
            fCon = true
            false
          }
        }
      })
      if ( fCon ) {
        propagationEdge = Some((propChanges, cdb))
        flawResolvers = Some(resolverList)
      }
      isGoal
    } else {
      false
    }
  }

  def graph: Term = {
    var id: Long = 0L
    var nodeIds: Map[Term, Term] = Map.empty
    var nodes: Set[Term] = Set.empty
    var edges: Set[Term] = Set.empty
    var nodeContent: Map[Term, Term] = Map.empty
    var nodeAttributes: Map[Term, Set[Term]] = Map.empty.withDefaultValue(Set.empty)
    var edgeAttributes: Map[Term, Set[Term]] = Map.empty.withDefaultValue(Set.empty)

    var edgeLabels: Set[Term] = Set.empty

    var done: Set[Term] = Set.empty

    def processNode( node: CollectionTerm, shape: Sym, style: Sym ): Term = {

      if (!done(node)) {
        done = done + node
        val nodeId = nodeIds.getOrElse(node, {
          id += 1
          Sym(s"n$id")
          Str(s"${this.tDiscovery(node)}/${this.tClosed.getOrElse(node,"-")}")
        })

        nodes = nodes + nodeId
        nodeContent = nodeContent.updated(nodeId, node)
        nodeIds = nodeIds.updated(node, nodeId)

        if (this.predecessor.contains(node)) {
          val preNode = this.predecessor(node)
          val preNodeId = nodeIds.getOrElse(preNode, {
            id += 1
            Sym(s"n$id")
            Str(s"${this.tDiscovery(preNode)}/${this.tClosed.getOrElse(preNode,"-")}")
          })
          nodeIds = nodeIds.updated(preNode, preNodeId)
          val edge = Tuple(preNodeId, nodeId)
          edges += edge

          val reason = this.edges(node).reasonStr
          if ( !reason.isEmpty ) {
            edgeLabels = edgeLabels + KeyVal(edge, Str(reason))
          }
        }
        val currentAtts = nodeAttributes(nodeId)
        nodeAttributes = nodeAttributes.updated(nodeId, currentAtts ++ Set(KeyVal(Sym("shape"), shape), KeyVal(Sym("style"), style)))
        nodeId
      } else {
        nodeIds(node)
      }

    }


    for ( node <- this.prunedList ) {
      val id = processNode(node, Sym("box"), Sym("filled"))
      val reasonNodeId = Tuple(Sym("reason"), id)
      val edge = Tuple(id, reasonNodeId)
      nodes = nodes + reasonNodeId
      edges = edges + edge
      nodeAttributes = nodeAttributes.updated(reasonNodeId, Set(
        KeyVal(Sym("shape"), Sym("note")),
        KeyVal(Sym("label"), Str(this.prunedReason(node)))
      ))

    }
    for ( node <- this.closedList ) {
      processNode(node, Sym("circle"), Sym("solid"))
    }
    for ((_, node) <- this.openList) {
      processNode(node, Sym("circle"), Sym("dotted"))
    }
    for ( node <- this.goalList) {
      processNode(node, Sym("circle"), Sym("filled"))
      var curNode = node
      var preNode = this.predecessor.get(node)
      while { preNode != None } do {
        val edge = Tuple(nodeIds(preNode.get), nodeIds(curNode))

        val currentAtts = edgeAttributes(edge)
        edgeAttributes = edgeAttributes.updated(edge, currentAtts ++ Set(KeyVal(Sym("style"), Sym("dashed"))))

        curNode = preNode.get
        preNode = predecessor.get(curNode)
      }
    }
    val nodeAttsTerm = SetTerm(nodeAttributes.map( (k, v) => KeyVal(k, SetTerm(v)) ).toSet)
    val edgeAttsTerm = SetTerm(edgeAttributes.map( (k, v) => KeyVal(k, SetTerm(v)) ).toSet)


    ListTerm(
      KeyVal(Sym("V"), SetTerm(nodes)),
      KeyVal(Sym("E"), SetTerm(edges)),
      KeyVal(Sym("node-attributes"), nodeAttsTerm),
      KeyVal(Sym("edge-attributes"), edgeAttsTerm),
      KeyVal(Sym("labels"), SetTerm(edgeLabels))
    )
  }
}
