/*******************************************************************************
 * Copyright (c) 2015 Uwe Köckemann <uwe.kockemann@oru.se>
 *  
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package org.spiderplan.causal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


import org.spiderplan.tools.logging.Logger;
import org.spiderplan.tools.stopWatch.StopWatch;
import org.spiderplan.causal.goals.DisjunctiveGoal;
import org.spiderplan.causal.goals.Goal;
import org.spiderplan.causal.goals.SingleGoal;
import org.spiderplan.representation.constraints.Constraint;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.representation.plans.SequentialPlan;
import org.spiderplan.representation.types.TypeManager;
import org.spiderplan.search.MultiHeuristicNode;
import org.spiderplan.search.MultiQueueSearch;
import org.spiderplan.search.MultiHeuristicNode.CompareMethod;
import org.spiderplan.tools.GenericComboBuilder;

/**
 * Implementation of heuristic forward planning
 * @author Uwe K&ouml;ckemann
 */
public class ForwardPlanning extends MultiQueueSearch {
	
	private ArrayList<Heuristic> heuristics = new ArrayList<Heuristic>();
	private boolean multiQueue;
	
	public boolean yahspLookahead = false;

	Map<Atomic,List<Term>> s0;
	Collection<Goal> g;
	
	ArrayList<StateVariableOperatorMultiState> A;
	ArrayList<StateVariableOperator> reducedOperators;

	public TypeManager tM;
	
	private int loohAhead = 0;
	
	private CommonDataStructures dStructs = new CommonDataStructures();
			
	public ForwardPlanning() {
		super.name = "ForwardPlanner";
	}
	
	private void initHeuristics( List<String> heuristicNames, Collection<Goal> g, ArrayList<StateVariableOperator> A, 
			TypeManager tM ) {
		for ( String name : heuristicNames ) {
			Heuristic h =  HeuristicFactory.createHeuristic(name, g, A, tM);
			if ( verbose ) Logger.msg(getName(), h.getClass().getSimpleName(), 2);
//			h.setKeepTimes(this.keepTimes);
			this.heuristics.add( h );
		}
	}
	
	/**
	 * Create initial search nodes.
	 * Since we may have multiple assignments for each variable we create an 
	 * initial search node for each combination of values.
	 * We assume that the sequence of values for each list in s0 is temporally
	 * ordered.
	 * <b>
	 * Goals are part of the search nodes since some {@link Constraint}s can add
	 * new ones that are need to be achieved in addition to make the node consistent.
	 * 
	 * @param s0 A map from {@link Atomic} variables to {@link List}s of value {@link Term}s.
	 * @param g A {@link Collection} of {@link Goal}s.
	 */
	private void init( Map<Atomic,List<Term>> s0, Collection<Goal> g ) {
		dStructs.init(reducedOperators, tM);
		
		ArrayList<Atomic> variables = new ArrayList<Atomic>();
		ArrayList<ArrayList<Term>> values = new ArrayList<ArrayList<Term>>();
		
		for ( Atomic k : s0.keySet() ) {	
			variables.add(k);
			ArrayList<Term> vals = new ArrayList<Term>();
			vals.addAll(s0.get(k));
			values.add(vals);
		}
		
		GenericComboBuilder<Term> cB = new GenericComboBuilder<Term>();
		ArrayList<ArrayList<Term>> s0Combos = cB.getCombos(values);
		
		if ( verbose ) Logger.msg(getName(), "Found " + s0Combos.size() + " combos for initial state...", 2);
		
		for ( int k = s0Combos.size()-1 ; k >= 0 ; k-- ) {
			if ( verbose ) Logger.msg(getName(), "Getting initial heuristics for combo #" + k, 2);
			
			ArrayList<Term> s0Combo = s0Combos.get(k);
			
			ForwardPlanningNode initNode = new ForwardPlanningNode(this.heuristics.size());
			
			initNode.prev = null;
			initNode.a = null;
			initNode.s = new HashMap<Atomic, List<Term>>();
			
			for ( int i = 0 ; i < variables.size() ; i++ ) {
				ArrayList<Term> valList = new ArrayList<Term>();
				valList.add(s0Combo.get(i));
				initNode.s.put(variables.get(i),valList);
			}
			
			initNode.g.addAll(g);
					
			boolean goalReachable = true;
			
			for ( int i = 0 ; i < heuristics.size() ; i++ ) {
				long hVal = this.getHeuristicValueFromMultiStateLookahead(heuristics.get(i), initNode.s, g, this.loohAhead );
				if ( verbose ) Logger.msg(getName(), "Initial heuristic value ("+heuristics.get(i).getClass().getSimpleName()+"):" + hVal, 2);
				
				initNode.setHeuristicValue(i, hVal);
				if ( initNode.getHeuristicValue(i) == Long.MAX_VALUE ) {
					
					if ( verbose ) {
						Map<SingleGoal,Long> sGoalValues = heuristics.get(i).getLastHeuristicValues();
						for ( SingleGoal sGoal : sGoalValues.keySet() ) {
							Logger.msg(getName() ,sGoal + " -> " + sGoalValues.get(sGoal), 0);
						}
					}
					
					goalReachable = false;
					break;
				}
			}
			
			if ( goalReachable ) {
				if ( k > 0 ) {
					/**
					 * Stack search spaces for each initial state combination.
					 * The last one added will be explored first (which is why we add
					 * them starting with the last combination (assuming a temporal ordering))
					 */
					super.backupAndClearQueues(); 
				}
				for ( int i = 0 ; i < super.getNumQueues() ; i++ ) {
					super.addToQueue(i, initNode);
				}
			}
			
			
		}
	}
	
	public void initSingleHeuristicPerQueue( Map<Atomic,List<Term>> s0, Collection<Goal> g, ArrayList<StateVariableOperatorMultiState> A, 
									TypeManager tM, List<String> heuristicNames, boolean[] useHelpfulActions ) {
		Logger.registerSource(super.name, super.verbosity);
		
		this.tM = tM;
		this.A = A;
		this.s0 = s0;	
		this.g = g;
		
		Logger.msg(getName() , "Initializing heuristics...", 2);
		
		reducedOperators = new ArrayList<StateVariableOperator>();
		for ( StateVariableOperatorMultiState a : A ) {
			reducedOperators.addAll(this.getAllStateVariableOperatorCombos(a));
		}
		
		initHeuristics(heuristicNames, g, reducedOperators, tM);
		multiQueue = true;
		
		ArrayList<Integer> queueToHeuristicMap = new ArrayList<Integer>();
		
		for ( int i = 0 ; i < heuristics.size() ; i++ ) {
			queueToHeuristicMap.add(i);
			super.addNewQueue();
		}
		
		for ( int i = 0 ; i < heuristics.size() ; i++ ) {
			if ( useHelpfulActions[i] ) {		// Same heuristic object used for helpful actions...
				queueToHeuristicMap.add(i);
				super.addNewQueue();
			}
		}
		 
		this.queueToHeuristicMap = new int[queueToHeuristicMap.size()];
		for ( int i = 0 ; i < queueToHeuristicMap.size() ; i++ ) {
			this.queueToHeuristicMap[i] = queueToHeuristicMap.get(i).intValue();
		}
				
		this.init(s0, g);
	}
	
	public void initLexicographicHeuristicOrder( Map<Atomic,List<Term>> s0, Collection<Goal> g, ArrayList<StateVariableOperatorMultiState> A, 
									TypeManager tM, List<String> heuristicNames, boolean useHelpfulActions ) {
		Logger.registerSource(super.name, super.verbosity);
		
		reducedOperators = new ArrayList<StateVariableOperator>();
		for ( StateVariableOperatorMultiState a : A ) {
			reducedOperators.addAll(this.getAllStateVariableOperatorCombos(a));
		}
		
		initHeuristics(heuristicNames, g, reducedOperators, tM);
		multiQueue = false;
		
		ArrayList<Integer> queueToHeuristicMap = new ArrayList<Integer>();
				
		for ( int i = 0 ; i < heuristics.size() ; i++ ) {
			queueToHeuristicMap.add(i);
			super.addNewQueue();
		}
				 
		this.queueToHeuristicMap = new int[queueToHeuristicMap.size()];
		for ( int i = 0 ; i < queueToHeuristicMap.size() ; i++ ) {
			this.queueToHeuristicMap[i] = queueToHeuristicMap.get(i).intValue();
		}
		
		this.A = A;
		this.g = g;
		this.tM = tM;
		
		reducedOperators = new ArrayList<StateVariableOperator>();
		for ( StateVariableOperatorMultiState a : A ) {
			reducedOperators.addAll(this.getAllStateVariableOperatorCombos(a));
		}
				
		this.init(s0, g);
	}
		
	@Override
	public boolean isGoal(MultiHeuristicNode n) {
		ForwardPlanningNode fdN = (ForwardPlanningNode)n;
		for ( Goal g : fdN.g ) {
			if ( !g.wasReached() ) {
				return false;
			}
		}
		return true;
	}

	@Override
	public ArrayList<LinkedList<MultiHeuristicNode>> expand( MultiHeuristicNode n ) {
		
		ForwardPlanningNode fpn = (ForwardPlanningNode)n;
		
		if ( verbose ) Logger.msg(getName(),"Expanding state:", 4);
		if ( verbose ) {
			for ( Atomic key : fpn.s.keySet() ) {
				if ( verbose ) Logger.msg(getName(), "    " + key + " -> " + fpn.s.get(key), 4);
			}
		}
								
		ArrayList<LinkedList<MultiHeuristicNode>> expansion = new ArrayList<LinkedList<MultiHeuristicNode>>(this.heuristics.size());
		for ( int i = 0 ; i < super.getNumQueues() ; i++ ) {
			expansion.add( new LinkedList<MultiHeuristicNode>() );	
		}
				
		if ( keepTimes ) StopWatch.start("[ForwardPlanner] Expand (internal)");
	
		if ( keepTimes ) StopWatch.start("[ForwardPlanner] Applicable");
		Set<StateVariableOperatorMultiState> app = SequentialStateFunctions.getApplicable(fpn.s, A);
		if ( keepTimes ) StopWatch.stop("[ForwardPlanner] Applicable");
		
		if ( verbose ) Logger.msg(getName(),"Applicable actions:", 4);

		for ( StateVariableOperatorMultiState a : app ) {
			if ( verbose ) Logger.msg(getName(), "    " + a.getName(), 4);
			ForwardPlanningNode nNew = new ForwardPlanningNode(this.heuristics.size());
			
			nNew.prev = fpn;
			nNew.a = a;
			nNew.s = fpn.s;
			nNew.s = SequentialStateFunctions.apply(fpn.s, a);	
			nNew.g = fpn.g.copy();
			nNew.setForceExploration(fpn.forceExploration());
						
			boolean change = true;
			while ( change ) {
				change = false;
			
				for ( Goal goal : nNew.g ) {
					if ( goal.isLandmark() && !goal.wasReached() && goal.requirementsSatisfied() && goal.reachedInMultiState(nNew.s)) {
						nNew.g.setReached(goal, true);
						change = true;
					} else if ( !goal.isLandmark() && goal.requirementsSatisfied() ) {
						boolean prevState = goal.wasReached();
						boolean currState = goal.reachedInMultiState(nNew.s);
						nNew.g.setReached(goal, currState);
						if ( prevState != currState ) {
							change = true;
						}
					}
				}
			}

			boolean goalReachable = true;
						
			for ( int i = 0 ; i < heuristics.size() ; i++ ) {
				if ( keepTimes ) StopWatch.start("[ForwardPlanner] Calculating heuristic: " + heuristics.get(i).getClass().getSimpleName());
				long hVal = this.getHeuristicValueFromMultiStateLookahead(heuristics.get(i), nNew.s, nNew.g, this.loohAhead);
				
				nNew.setHeuristicValue(i, hVal);
				if ( nNew.getHeuristicValue(i) == Long.MAX_VALUE ) {					
					goalReachable = false;
					if ( keepTimes ) StopWatch.stop("[ForwardPlanner] Calculating heuristic: " + heuristics.get(i).getClass().getSimpleName());
					break;
				}
				if ( keepTimes ) StopWatch.stop("[ForwardPlanner] Calculating heuristic: " + heuristics.get(i).getClass().getSimpleName());
			}
			
			if ( verbose ) Logger.msg(getName(),"Adding to expansion "+Arrays.toString(nNew.getHeuristicValues())+":\n    "+nNew.getPlan().toString().replace("\n", "\n    "), 4);
			
			
			
			if ( goalReachable ) {	
				if ( keepTimes ) StopWatch.start("[ForwardPlanner] YAHSP lookahead (total)");
				if ( yahspLookahead ) {
					if ( keepTimes ) StopWatch.start("[ForwardPlanner] YAHSP getActionCostsOfSequentialState");
					Map<Object,Long> cost = dStructs.getActionCostsOfSequentialState(nNew.s);
					if ( keepTimes ) StopWatch.stop("[ForwardPlanner] YAHSP getActionCostsOfSequentialState");
					
					ArrayList<Entry<Atomic,Term>> activeGoals = new ArrayList<Map.Entry<Atomic,Term>>();
					for ( Goal g : nNew.g ) {
						if ( !g.wasReached() && g.requirementsSatisfied() ) {
							if ( g instanceof SingleGoal ) {
								activeGoals.addAll(g.getEntries());
							} else if ( g instanceof DisjunctiveGoal ) { 
								/* Just use first one. Alternatively all combinations could be added to maximize 
								 	possibility of finding some (while performing combinatorial number of lookaheads) */
								activeGoals.add(g.getEntries().iterator().next());
							}
						}
					}
					
					if ( keepTimes ) StopWatch.start("[ForwardPlanner] YAHSP lookahead");
					ForwardPlanningNode lookaheadNode = YAHSPLookahead.lookahead(nNew, cost, activeGoals, this.A);
					if ( keepTimes ) StopWatch.stop("[ForwardPlanner] YAHSP lookahead");
					
					if ( keepTimes ) StopWatch.start("[ForwardPlanner] YAHSP lookahead post");
					if ( lookaheadNode != null ) {
						goalReachable = true;
						for ( int i = 0 ; i < heuristics.size() ; i++ ) {
							if ( keepTimes ) StopWatch.start("[ForwardPlanner] Calculating heuristic: " + heuristics.get(i).getClass().getSimpleName());
							
							if ( keepTimes ) StopWatch.start("[ForwardPlanner] YAHSP lookahead post (h)");
							long hVal = this.getHeuristicValueFromMultiStateLookahead(heuristics.get(i), lookaheadNode.s, lookaheadNode.g, this.loohAhead);
							if ( keepTimes ) StopWatch.stop("[ForwardPlanner] YAHSP lookahead post (h)");
							
							lookaheadNode.setHeuristicValue(i, hVal);
							if ( lookaheadNode.getHeuristicValue(i) == Long.MAX_VALUE ) {					
								goalReachable = false;
								break;
							}
							if ( keepTimes ) StopWatch.stop("[ForwardPlanner] Calculating heuristic: " + heuristics.get(i).getClass().getSimpleName());
						}
						
						if ( goalReachable ) {
							if ( multiQueue ) {			// One queue per heuristic
								lookaheadNode.compareMethod = CompareMethod.Index;
								for ( int i = 0 ; i < heuristics.size() ; i++ ) {
									expansion.get(i).add(lookaheadNode);
								}
							} else {					// One queue for all, sorted lexicographically
								lookaheadNode.compareMethod = CompareMethod.Lexicographic;
								expansion.get(0).add(lookaheadNode);
							}
						}
					}
					if ( keepTimes ) StopWatch.stop("[ForwardPlanner] YAHSP lookahead post");
				}
				if ( keepTimes ) StopWatch.stop("[ForwardPlanner] YAHSP lookahead (total)");
				
				if ( multiQueue ) {			// One queue per heuristic
					nNew.compareMethod = CompareMethod.Index;
					for ( int i = 0 ; i < heuristics.size() ; i++ ) {
						expansion.get(i).add(nNew);
					}
				} else {					// One queue for all, sorted lexicographically
					nNew.compareMethod = CompareMethod.Lexicographic;
					expansion.get(0).add(nNew);
				}
			}
		}			
		return expansion;
	}
	
	/**
	 * If n is already in C return n, else null
	 * @param C
	 * @param n
	 * @return
	 */
	private MultiHeuristicNode getFromCollection( Collection<MultiHeuristicNode> C, MultiHeuristicNode n ) {
		for ( MultiHeuristicNode n2 : C ) {
			if ( n.equals(n2) ) {
				return n2;
			}
		}
		return null;
	}
		
	
	
	
	public SequentialPlan getCurrentPlan() {
		ForwardPlanningNode fdn = (ForwardPlanningNode)n;
		return fdn.getPlan();
	}
	
	public SequentialPlan getSolutionPlan() {
		ForwardPlanningNode fdn = (ForwardPlanningNode)solution;
		return fdn.getPlan();
	}
	
	public void setLookAhead( int lookAhead )  {
		this.loohAhead = lookAhead;
	}
	
	public int getLookAhead( )  {
		return this.loohAhead;
	}
	
	/**
	 * Prunes all nodes that use actions with matching names. Also removes them from the
	 * set of ground actions and re-initializes all heuristics without the given actions 
	 * and updates the heuristic value of all nodes in the search space.
	 * @param prunedActionNames List of {@link Atomic} action names that are to be pruned.
	 */
	public void pruneMatchingActions( Collection<Atomic> prunedActionNames ) {
		this.pruneMatchingActionsAndReinitHeuristics(prunedActionNames);
	}
	
	/**
	 * Prunes all nodes that use actions with matching names. Also removes them from the
	 * set of ground actions and re-initializes all heuristics without the given actions. 
	 * Instead of updating all heuristic values this method just restarts search, which
	 * may be reasonable since many nodes in the old search space may never be visited
	 * now.
	 * @param prunedActionNames List of {@link Atomic} action names that are to be pruned.
	 */
	public void pruneMatchingActionsAndRestartSearch( Collection<Atomic> prunedActionNames ) {
		this.pruneMatchingActionsAndReinitHeuristics(prunedActionNames);
		this.reset();
		this.init(s0, g);
	}
	
	/**
	 * Prunes all nodes that use actions with matching names. Also removes them from the
	 * set of ground actions and re-initializes all heuristics without the given actions 
	 * and updates the heuristic value of all nodes in the search space.
	 * @param prunedActionNames List of {@link Atomic} action names that are to be pruned.
	 */
	private void pruneMatchingActionsAndReinitHeuristics( Collection<Atomic> prunedActionNames ) {
		/**
		 * Remove operators
		 */
		ArrayList<StateVariableOperatorMultiState> remList = new ArrayList<StateVariableOperatorMultiState>();
		for ( Atomic prunedActionName : prunedActionNames ) {
			for ( StateVariableOperatorMultiState svo : this.A ) {
				if ( svo.getName().match(prunedActionName) != null ) {
					remList.add(svo);
				}
			}
		}
		A.removeAll(remList);
		
		/**
		 * Re-initialize heuristics
		 */
		ArrayList<StateVariableOperator> reducedOperators = new ArrayList<StateVariableOperator>();
		for ( StateVariableOperatorMultiState a : A ) {
			reducedOperators.addAll(this.getAllStateVariableOperatorCombos(a));
		}
		
		for ( Heuristic h : this.heuristics ) {
			h.initializeHeuristic( g, reducedOperators, tM);
		}
	}
	 
//	public void draw() {
//		AbstractTypedGraph<String,String> g = new DirectedSparseGraph<String,String>();
//		Map<String,String> edgeLabels = new HashMap<String,String>(); 
//		
//		HashSet<MultiHeuristicNode> unprocessed = new HashSet<MultiHeuristicNode>();
//		
//		unprocessed.addAll(queues.get(0));
//		
//		int edgeID = 0;
//		
//		while ( !unprocessed.isEmpty() ) {
//			MultiHeuristicNode node = unprocessed.iterator().next();
//			ForwardPlanningNode fdn = (ForwardPlanningNode)node;
//			
//			if ( fdn.prev !=  null ) {
//				unprocessed.add(fdn.prev);
//			}
//			unprocessed.remove(node);
//			
//			String hPrev;
//			if ( fdn.prev != null ) 
//				hPrev = Arrays.toString(fdn.prev.getHeuristicValues());
//			else 
//				hPrev = "null";
//		
//			String h = Arrays.toString(fdn.getHeuristicValues());
//			
//			if ( fdn.prev != null &&  fdn.prev.a != null ) {
//				g.addEdge(""+(edgeID++), fdn.prev.a.getName().toString() + "h=" + hPrev  , fdn.a.getName().toString() + "h=" + h  );
//			} else if ( fdn.a != null ) {
//				g.addEdge(""+(edgeID++), "ROOT"+hPrev  , fdn.a.getName().toString()+ "h=" + h );
//			}
//		}
//		
//		new GraphFrame<String,String>(g, null,  "Search Space", GraphFrame.LayoutClass.FR, edgeLabels);
//		
//	}
	

	
	private Collection<StateVariableOperator> getAllStateVariableOperatorCombos( StateVariableOperatorMultiState svo ) {
		StateVariableOperatorMultiState svCopy = svo.copy();
		List<Map<Atomic,Term>> allEffCombos = SequentialStateFunctions.getAllStateCombos(svCopy.getEffects());
		
		Collection<StateVariableOperator> r = new ArrayList<StateVariableOperator>();
		
		for ( Map<Atomic,Term> effCombo : allEffCombos ) {
			StateVariableOperator svoNew = new StateVariableOperator();
			svoNew.setName(svCopy.getName());
			
			svoNew.getPreconditions().putAll(svCopy.getPreconditions());	
			svoNew.getEffects().putAll(effCombo);
			
			r.add(svoNew);
		}
		return r;		
	}
	
//	HashMap<Integer,Long> hCache = new HashMap<Integer, Long>();
	/**
	 * Calculate value of a specific heuristic. If
	 * lookahead is used the minimum value of future 
	 * states is taken (recursively computed).
	 * The branching factor of the lookahead is very
	 * large so it should only be used with caution and
	 * small values.
	 * @param h The {@link Heuristic}
	 * @param state The state to be evaluated. 
	 * @param goal The goal to be reached
	 * @param lookahead Lookahead value
	 * @return
	 */
	private long getHeuristicValueFromMultiStateLookahead( Heuristic h, Map<Atomic,List<Term>> state, Collection<Goal> goal, int lookahead ) {
		long minCost = Long.MAX_VALUE;
//		int hashValue = state.hashCode()+17*lookahead+31*h.hashCode();
		
//		Long cachedVal = hCache.get(Integer.valueOf(hashValue)); 
//		if ( cachedVal != null ) {
//			return cachedVal.longValue();
//		}
		
		if ( lookahead == 0 ) {	
			List<Map<Atomic,Term>> allStates = SequentialStateFunctions.getAllStateCombos(state);
			for ( Map<Atomic,Term> s : allStates ) {
			
				long hVal = h.calculateHeuristicValue(s, goal, dStructs);
				
				if ( hVal < minCost ) {
					minCost = hVal;
				}
			}
		} else {			
			Set<StateVariableOperatorMultiState> app = SequentialStateFunctions.getApplicable(state, this.A);
			
			for ( StateVariableOperatorMultiState a : app ) {
				 Map<Atomic,List<Term>> s_prime = SequentialStateFunctions.apply(state, a);
				 long hVal = getHeuristicValueFromMultiStateLookahead(h, s_prime, goal, lookahead-1);
				 if ( hVal < minCost ) {
					 minCost = hVal; 
				 }
				 if ( minCost == 0 ) {	// Not going to get better...
//					 hCache.put(Integer.valueOf(hashValue), Long.valueOf(0));
					 return 0;
				 }
			}			
			
		}
		
//		hCache.put(Integer.valueOf(hashValue), Long.valueOf(minCost));
		return minCost;
	}
	
	/**
	 * Add some new nodes from the outside
	 * @param newNodes
	 * @param removeFromVisited If true it will remove the new nodes from the set of visited nodes to make sure they are considered again.
	 * 
	 */
	public void addNewPlanningNodes( Collection<ForwardPlanningNode> newNodes, boolean removeFromVisited ) {
		
		if ( removeFromVisited ) {
			super.visited.removeAll(newNodes);
		}
		
		for ( ForwardPlanningNode nNew : newNodes ) {
					
			boolean goalReachable = true;
			for ( int i = 0 ; i < heuristics.size() ; i++ ) {
				nNew.setHeuristicValue(i, this.getHeuristicValueFromMultiStateLookahead( heuristics.get(i), nNew.s, nNew.g, loohAhead));
				if ( nNew.getHeuristicValue(i) == Long.MAX_VALUE ) {
					goalReachable = false;
					break;
				}
			}
			if ( goalReachable ) {
				for ( int i = 0 ; i < super.getNumQueues() ; i++ ) {
					super.addToQueue(i,nNew);
				}
			}
			
			
		}
	}
	
	public void clearVisited() {
		super.visited.clear();
	}
}
