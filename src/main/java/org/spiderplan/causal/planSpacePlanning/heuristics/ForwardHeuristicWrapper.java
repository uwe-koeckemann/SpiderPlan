package org.spiderplan.causal.planSpacePlanning.heuristics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.spiderplan.causal.forwardPlanning.CausalReasoningTools;
import org.spiderplan.causal.forwardPlanning.CommonDataStructures;
import org.spiderplan.causal.forwardPlanning.StateVariableOperator;
import org.spiderplan.causal.forwardPlanning.causalGraph.CausalGraphHeuristic;
import org.spiderplan.causal.forwardPlanning.causalGraph.DomainTransitionGraph;
import org.spiderplan.causal.forwardPlanning.goals.Goal;
import org.spiderplan.causal.forwardPlanning.goals.SingleGoal;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.Operator;
import org.spiderplan.representation.expressions.Statement;
import org.spiderplan.representation.expressions.causal.OpenGoal;
import org.spiderplan.representation.expressions.domain.Substitution;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.representation.types.TypeManager;


public class ForwardHeuristicWrapper implements Heuristic {
	

	CausalGraphHeuristic forwardHeuristic = new CausalGraphHeuristic();
	Set<String> usedVars;
	
	public ForwardHeuristicWrapper( ConstraintDatabase cDB, Collection<Operator> O, TypeManager tM ) {
		usedVars = CausalReasoningTools.getRelevantVariables(cDB, O);
		
		List<StateVariableOperator> A = new ArrayList<StateVariableOperator> ();
		for ( Operator o : O ) {
			A.add(o.getStateVariableBasedOperator(usedVars));
		}
		
		List<Goal> goals = new ArrayList<Goal>();
		for ( OpenGoal og : cDB.get(OpenGoal.class) ) {
			if ( !og.isAsserted() ) {
				goals.add(new SingleGoal(og.getStatement().getVariable(), og.getStatement().getValue()));
			}
		}
		
		forwardHeuristic.initializeHeuristic(goals, A, tM);
	}

	@Override
	public long calculateHeuristicValue(ConstraintDatabase cDB, Collection<Operator> O) {
		List<Goal> goals = new ArrayList<Goal>();
		for ( OpenGoal og : cDB.get(OpenGoal.class) ) {
			if ( !og.isAsserted() ) {
				goals.add(new SingleGoal(og.getStatement().getVariable(), og.getStatement().getValue()));
			}
		}
		
		Map<Term,Term> state = new HashMap<Term,Term>();
		
		//TODO: Change to take first value that has no following value
		// How to: Analyze temporal graph and for each cluster take max(EST). Then take minimum EST among all clusters as state. 
		//TODO: Later: Allow multiple values
		for ( Statement s : cDB.get(Statement.class)) {
			if ( !state.containsKey(s.getVariable()) && usedVars.contains(s.getVariable().getUniqueName())) {
//			if ( usedVars.contains(s.getVariable().getUniqueName())) {
				state.put(s.getVariable(), s.getValue());
			}
		}
		
		long heuristicValue = forwardHeuristic.calculateHeuristicValue(state, goals, new CommonDataStructures());
		
		return heuristicValue;
	}

}
