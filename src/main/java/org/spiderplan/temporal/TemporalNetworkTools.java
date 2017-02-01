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
package org.spiderplan.temporal;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.spiderplan.modules.tools.ConstraintRetrieval;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.expressions.Expression;
import org.spiderplan.representation.expressions.Statement;
import org.spiderplan.representation.expressions.ExpressionTypes.TemporalRelation;
import org.spiderplan.representation.expressions.domain.Substitution;
import org.spiderplan.representation.expressions.ValueLookup;
import org.spiderplan.representation.expressions.temporal.AllenConstraint;
import org.spiderplan.representation.expressions.temporal.Interval;
import org.spiderplan.representation.expressions.temporal.PlanningInterval;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.representation.types.TypeManager;
import org.spiderplan.temporal.stpSolver.IncrementalSTPSolver;
import org.spiderplan.tools.GenericComboBuilder;
import org.spiderplan.tools.Global;
import org.spiderplan.tools.visulization.TemporalNetworkVisualizer;

/**
 * Collection of static methods that manipulate
 * {@link Statement}s and {@link AllenConstraint}s
 * in a {@link ConstraintDatabase}.
 * @author Uwe Koeckemann
 *
 */
public class TemporalNetworkTools {
			
	
	/**
	 * Merge all statements that use the same state variable and value and have an {@link AllenConstraint}
	 * of type Equals between them.
	 * @param cdb {@link ConstraintDatabase} whose {@link AllenConstraint}s should be compressed
	 * @return {@link Substitution} that was applied to merge intervals.
	 */
	public static Substitution compressTemporalConstraints( ConstraintDatabase cdb ) {
		Statement from, to;
		List<List<Term>> equalTerms = new ArrayList<List<Term>>();
		LinkedList<AllenConstraint> workList = new LinkedList<AllenConstraint>();
		Map<Term,Statement> statements = new HashMap<Term,Statement>();
		

		for ( AllenConstraint tC : cdb.get(AllenConstraint.class) ) {
			if ( tC.getRelation().equals(TemporalRelation.Equals) ) {
				from = cdb.getStatement(tC.getFrom());
				to = cdb.getStatement(tC.getTo());
				if ( from.getVariable().equals(to.getVariable()) && from.getValue().equals(to.getValue()) ) {
					statements.put(from.getKey(),from);
					statements.put(to.getKey(),to);
					workList.add(tC);
				}
			}
		}
		
		while ( !workList.isEmpty() ) {
			ArrayList<Term> included = new ArrayList<Term>();
			AllenConstraint first = workList.get(0);
			workList.remove(0);
			
			cdb.remove(first);

			included.add(first.getFrom());
			included.add(first.getTo());
			
			boolean changed = true;
			while ( changed ) {
				changed = false;
				for ( int i = 0 ; i < workList.size() ; i++ ) {
					AllenConstraint tC = workList.get(i);
					if ( included.contains(tC.getFrom())  && !included.contains(tC.getTo()) ) {
						included.add(tC.getTo());
						workList.remove(i);
						i--;
						cdb.remove(tC);	
						changed = true;
					}
					if ( included.contains(tC.getTo())  && !included.contains(tC.getFrom()) ) {
						included.add(tC.getFrom());
						workList.remove(i);
						i--;
						cdb.remove(tC);
						changed = true;
					}
				}
			}
			equalTerms.add(included);
		}
		
		Substitution theta = new Substitution();
		for ( List<Term> allInts : equalTerms ) {			
			Term keep = allInts.get(0);	
			for ( int i = 1 ; i < allInts.size() ; i++ ) {
				cdb.remove(statements.get(allInts.get(i)));
				theta.add(allInts.get(i),keep);
			}
		}

		Term.setAllowConstantSubstitution(true);
		cdb.substitute(theta);
		cdb.substitute(theta); //TODO: WTF
		Term.setAllowConstantSubstitution(false);
		
		return theta;
	}
	
	/**
	 * Get all statements connected to interval <code>a</code> via binary temporal constraints.
	 * @param cdb Constraint database
	 * @param a Interval term
	 * @return collection of statements whose intervals appear in tempral constraints using <code>a</code>
	 */
	public static  Collection<Statement> directlyConnectedStatements( ConstraintDatabase cdb, Term a ) {
		Set<Statement> r = new HashSet<Statement>();
		for ( AllenConstraint tC : cdb.get(AllenConstraint.class) ) {
			if ( tC.isBinary() && tC.getFrom().equals(a) ) {
				r.add(cdb.getStatement(tC.getTo()));
			}
			if ( tC.isBinary() && tC.getTo().equals(a) ) {
				r.add(cdb.getStatement(tC.getFrom()));
			}
		}
		return r;	
	}
	/**
	 * Get all temporal constraints between to intervals
	 * @param cdb Constraint database
	 * @param a Termporal interval term
	 * @param b Temporal interval term
	 * @return Collection of {@link AllenConstraint} constraints between <code>a</code> and <code>b</code>
	 */
	public static  Collection<Expression> getTemporalConstraintsBetween( ConstraintDatabase cdb, Term a, Term b ) {
		ArrayList<Expression> r = new ArrayList<Expression>();
		for ( AllenConstraint tC : cdb.get(AllenConstraint.class) ) {
			if ( tC.isBinary() && ( tC.getFrom().equals(a) && tC.getTo().equals(b) 
					|| tC.getFrom().equals(b) && tC.getTo().equals(a) ) ) {
				r.add(tC);
			} 
		}
		return r;
	}


			
	
	/**
	 * Test if all {@link Statement}s and {@link AllenConstraint}s in one {@link ConstraintDatabase} are also in second {@link ConstraintDatabase}.
	 * @param cdb1 First {@link ConstraintDatabase}
	 * @param cdb2 Second {@link ConstraintDatabase}
	 * @return <code>true</code> iff all {@link Statement} and {@link AllenConstraint} objects in <code>cdb1</code> are also in <code>cdb2</code>, <code>false</code> otherwise
	 */
	public static boolean isContainedIn( ConstraintDatabase cdb1, ConstraintDatabase cdb2 ) {		
//		StopWatch.start("isContainedIn");

		List<Statement> S1 = cdb1.get(Statement.class);
		List<Statement> S2 = cdb2.get(Statement.class);
		
		if ( S1.size() > S2.size() ) {
			return false;
		}
		
		for ( int i = 0 ; i < S1.size() ; i++ ) {
			if ( !S1.get(i).equals(S2.get(i)) ) {
				return false;
			}
		}
		
		List<AllenConstraint> AC1 = cdb1.get(AllenConstraint.class);
		List<AllenConstraint> AC2 = cdb2.get(AllenConstraint.class);
		
		if ( AC1.size() > AC2.size() ) {
			return false;
		}
		
		for ( int i = 0 ; i < AC1.size() ; i++ ) {
			if ( !AC1.get(i).equals(AC2.get(i)) ) {
//				System.out.println(AC1.get(i) + " != " + AC2.get(i));
				return false;
			}
		}
		
//		StopWatch.stop("isContainedIn");
		return true;
	}
		
	/**
	 * Remove all groups in from {@link AllenConstraint}s and add new {@link AllenConstraint}s for 
	 * combinations of group members.
	 * @param C {@link Collection} of {@link Expression}s
	 * @param groupMapping A mapping of group key {@link Term} to all their member key {@link Term}s.
	 */
	public static void replaceGroupKeys( Collection<Expression> C, Map<Term,ArrayList<Term>> groupMapping ) {
		ArrayList<AllenConstraint> addList = new ArrayList<AllenConstraint>();
		ArrayList<AllenConstraint> remList = new ArrayList<AllenConstraint>();
		
		for ( Expression c : C ) {
			if ( c instanceof AllenConstraint ) {
				AllenConstraint tC = (AllenConstraint)c;
				
				if ( tC.isUnary() && groupMapping.containsKey(tC.getFrom()) ) {
					ArrayList<Term> allChoices = new ArrayList<Term>();
					
					allChoices.addAll(groupMapping.get(tC.getFrom()));
					
					Interval[] bounds = new Interval[tC.getNumBounds()];
					for ( int i = 0 ; i < tC.getNumBounds() ; i++ ) {
						bounds[i] = tC.getBound(i);
					}
					
					for ( Term choice : allChoices ) {
						addList.add( new AllenConstraint(choice, tC.getRelation(), bounds));
					}
					remList.add(tC);
				} else if ( tC.isBinary() && ( groupMapping.containsKey(tC.getFrom()) || groupMapping.containsKey(tC.getTo())) ) { 
					ArrayList<Term> allFromChoices = new ArrayList<Term>();
					ArrayList<Term> allToChoices = new ArrayList<Term>();	
					
					if ( groupMapping.containsKey(tC.getFrom()) ) {
						allFromChoices.addAll(groupMapping.get(tC.getFrom()));
					} else {
						allFromChoices.add( tC.getFrom() );
					}
					
					if ( groupMapping.containsKey(tC.getTo()) ) {
						allToChoices.addAll(groupMapping.get(tC.getTo()));
					} else {
						allToChoices.add( tC.getTo() );
					}
					
					ArrayList<ArrayList<Term>> input = new ArrayList<ArrayList<Term>>();
					input.add(allFromChoices);
					input.add(allToChoices);
					GenericComboBuilder<Term> cB = new GenericComboBuilder<Term>();
					ArrayList<ArrayList<Term>> combos = cB.getCombos(input);
					
					Interval[] bounds = new Interval[tC.getNumBounds()];
					for ( int i = 0 ; i < tC.getNumBounds() ; i++ ) {
						bounds[i] = tC.getBound(i);
					}
					
					for ( ArrayList<Term> combo : combos ) {
						addList.add( new AllenConstraint(combo.get(0), combo.get(1), tC.getRelation(), bounds ) );
					}
					remList.add(tC);					
				}
			}
		}
		C.removeAll(remList);
		C.addAll(addList);
	}
	
//	public static Map<Atomic,List<Term>> getSequencedValues( ConstraintDatabase cdb, TypeManager tM ) {
//		TemporalNetworkTools tools = new TemporalNetworkTools();
//		
//		IncrementalSTPSolver csp = new IncrementalSTPSolver(0, Global.MaxTemporalHorizon);
//		csp.isConsistent(cdb, tM);
//		
//		Map<Atomic,List<TermAndEST>> sequencedValues = new HashMap<Atomic, List<TermAndEST>>();
//		
//		for ( Statement s : cdb.get(Statement.class)) {
//			if ( !sequencedValues.containsKey(s.getVariable()) ) {
//				sequencedValues.put(s.getVariable(), new ArrayList<TermAndEST>());
//			}
//			TermAndEST x = tools.new TermAndEST(s.getValue(), csp.getEST(s.getKey()));
//			sequencedValues.get(s.getVariable()).add(x);
//		}
//		
//		Map<Atomic,List<Term>> r = new HashMap<Atomic, List<Term>>();
//		for ( Atomic key : sequencedValues.keySet() ) {
//			Collections.sort(sequencedValues.get(key));
//			List<Term> l = new ArrayList<Term>();
//			for ( TermAndEST v : sequencedValues.get(key) ) {
//				l.add(v.value);
//			}
//			r.put(key, l);
//		}
//		return r;
//	}
	
	/**
	 * Create a sequence of intervals for each state-variable containing all
	 * intervals that change this state-variable. Each sequence is sorted by 
	 * the earliest start time (EST) of its intervals.
	 * @param cdb {@link ConstraintDatabase} which should be sequenced
	 * @return Map from state-variables (represented by {@link Atomic}) to sequenced intervals (represented by {@link Term}).
	 */
	public static Map<Atomic,List<Term>> getSequencedIntervals( ConstraintDatabase cdb ) {
		TemporalNetworkTools tools = new TemporalNetworkTools();
		ValueLookup tiLookup = cdb.getUnique(ValueLookup.class);
		
		Map<Atomic,List<TermAndEST>> sequencedValues = new HashMap<Atomic, List<TermAndEST>>();
		
		for ( Statement s : cdb.get(Statement.class)) {
			if ( !sequencedValues.containsKey(s.getVariable()) ) {
				sequencedValues.put(s.getVariable(), new ArrayList<TermAndEST>());
			}
			TermAndEST x = tools.new TermAndEST(s.getKey(), tiLookup.getEST(s.getKey()));
			sequencedValues.get(s.getVariable()).add(x);
		}
		
		Map<Atomic,List<Term>> r = new HashMap<Atomic, List<Term>>();
		for ( Atomic key : sequencedValues.keySet() ) {
			Collections.sort(sequencedValues.get(key));
//			System.out.println(key + " -> " + sequencedValues.get(key));
			List<Term> l = new ArrayList<Term>();
			for ( TermAndEST v : sequencedValues.get(key) ) {
				l.add(v.value);
			}
			r.put(key, l);
		}
		return r;
	}
	
	private class TermAndEST implements Comparable<TermAndEST> {
		public Term value;
		public long EST;
		
		public TermAndEST( Term value, long EST ) {
			this.value = value;
			this.EST = EST;
		}

		@Override
		public int compareTo(TermAndEST o) {
			return (int) (this.EST - o.EST);
		}
		@Override
		public String toString() {
			return "(" + EST + " " + value.toString() + ")";
		}
	}


	
	/**
	 * Get only those {@link Statement}s from a {@link ConstraintDatabase} that are lie inside
	 * the planning interval. Can be used to focus on ongoing things and avoid spending time to 
	 * match things to past statements. 
	 * @param cdb A {@link ConstraintDatabase}
	 * @param tM A {@link TypeManager} (needed for temporal reasoning).
	 * @return A {@link Collection} of {@link Statement}s inside the {@link PlanningInterval} of the {@link ConstraintDatabase}. 
	 * 	Note: The {@link PlanningInterval} constraint must be in <code>cdb</code> for this to work.
	 */
	public static Collection<Statement> getStatementsInPlanningInterval( ConstraintDatabase cdb, TypeManager tM ) {
		List<Statement> S = cdb.get(Statement.class);
		if ( S.isEmpty() ) {
			return new ArrayList<Statement>();
		}
		PlanningInterval pI = cdb.getUnique(PlanningInterval.class);
		ValueLookup tiLookup = cdb.getUnique(ValueLookup.class);
		
		long tMin = 0;
		long tMax = Global.MaxTemporalHorizon;
		
		if ( pI != null ) {
			tMin = pI.getStartTimeValue();
			tMax = pI.getHorizonValue();
		}
		
		Collection<Statement> r = new ArrayList<Statement>();
		
		for ( Statement s : cdb.get(Statement.class) ) {
			if ( tiLookup.getLET(s.getKey()) >= tMin && tiLookup.getEST(s.getKey()) <= tMax ) {
				r.add(s);
			}
		}
		
		return r;
	}
	

	/**
	 * Solve STP of a {@link ConstraintDatabase} in debug mode. This will provide console output
	 * about which constraints cannot be added.
	 * @param cDB {@link ConstraintDatabase} to inspect. 
	 */
	public static void inspect( ConstraintDatabase cDB ) {
		IncrementalSTPSolver csp = new IncrementalSTPSolver(0, Global.MaxTemporalHorizon);
		csp.debug = true;
		csp.isConsistent(cDB);
	}
	
	/**
	 * Draw the temporal network of a {@link ConstraintDatabase}
	 * @param cDB A {@link ConstraintDatabase}
	 */
	public static void draw( ConstraintDatabase cDB ) {
		TemporalNetworkVisualizer tnv = new TemporalNetworkVisualizer();
		tnv.draw(cDB);
	}
	/**
	 * Dump timeline data of CDB (after temporal propagation) so it can be visualized.
	 * To create a figure from the dumped data use the script "PlotTimelines" in main.python
	 * @param cDB {@link ConstraintDatabase} after temporal propagation
	 * @param tiLookup Propagated earliest and latest start and end times of all intervals
	 * @param fName Target filename
	 */
	public static void dumbTimeLineData( ConstraintDatabase cDB, ValueLookup tiLookup, String fName ) {
		StringBuilder sb = new StringBuilder();
			
		for ( Statement s : cDB.get(Statement.class)) {
			System.out.println(s);
			sb.append(s.getVariable());
			sb.append("|");
			sb.append(tiLookup.getEST(s.getKey()));
			sb.append("|");
			sb.append(tiLookup.getLST(s.getKey()));
			sb.append("|");
			sb.append(tiLookup.getEET(s.getKey()));
			sb.append("|");
			sb.append(tiLookup.getLET(s.getKey()));
			sb.append("|");
			sb.append(s.getValue());
			sb.append("\n");
		}
				
		try{
			FileWriter fstream = new FileWriter(Global.workingDir+fName);
			BufferedWriter out = new BufferedWriter(fstream);

			out.write( sb.toString() );
			out.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}
