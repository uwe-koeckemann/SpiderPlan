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
import org.spiderplan.representation.constraints.Constraint;
import org.spiderplan.representation.constraints.ConstraintTypes.TemporalRelation;
import org.spiderplan.representation.constraints.Interval;
import org.spiderplan.representation.constraints.PlanningInterval;
import org.spiderplan.representation.constraints.PossibleIntersection;
import org.spiderplan.representation.constraints.AllenConstraint;
import org.spiderplan.representation.constraints.Statement;
import org.spiderplan.representation.constraints.TemporalIntervalLookup;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Substitution;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.representation.types.TypeManager;
import org.spiderplan.temporal.stpSolver.IncrementalSTPSolver;
import org.spiderplan.tools.GenericComboBuilder;
import org.spiderplan.tools.Global;
import org.spiderplan.tools.Loop;
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
	 * Check if this TemporalDatabase has Statements that use the Labels occurring in
	 * the provided TemporalConstraint. Can be used for careful adding to avoid 
	 * "Unknown Statement" errors during consistency checks. Also used to filter out
	 * TemporalConstraints when creating databases from subsets of Statements in method
	 * getSubsetDatabases( int n ).
	 * @param tC
	 * @return
	 */
	public static boolean hasFittingStatements( ConstraintDatabase cdb, AllenConstraint tC ) {
		boolean foundFrom = false;
		boolean foundTo = tC.getTo() == null; // if null we will not look for this one
		for ( Statement s : cdb.getStatements() ) {
			
			if ( !foundFrom ) {
				foundFrom = s.getKey().equals(tC.getFrom() );
			}
			if ( !foundTo ) {
				foundTo = s.getKey().equals(tC.getTo() );
			}
			if ( foundFrom && foundTo ) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns true if there exists a TemporalConstraint which has a and b as interval keys.
	 * @param a
	 * @param b
	 * @return
	 */
	public static  boolean hasTemporalConstraintBetween( ConstraintDatabase cdb, Term a, Term b ) {
		for ( AllenConstraint tC : cdb.getConstraints().get(AllenConstraint.class) ) {
			if ( tC.isBinary() && ( tC.getFrom().equals(a) && tC.getTo().equals(b) 
				|| tC.getFrom().equals(b) && tC.getTo().equals(a) ) ) {
				return true;
			} 
		}
		return false;
	}
	
	/**
	 * Returns all temporal constraints between two intervals
	 * @param a
	 * @param b
	 * @return
	 */
	public static  Collection<Constraint> getTemporalConstraintsBetween( ConstraintDatabase cdb, Term a, Term b ) {
		ArrayList<Constraint> r = new ArrayList<Constraint>();
		for ( AllenConstraint tC : cdb.getConstraints().get(AllenConstraint.class) ) {
			if ( tC.isBinary() && ( tC.getFrom().equals(a) && tC.getTo().equals(b) 
				|| tC.getFrom().equals(b) && tC.getTo().equals(a) ) ) {
				r.add(tC);
			} 
		}
		return r;
	}
	
	/**
	 * Returns Get all {@link Statement}s connected directly to <i>a</i> via a binary {@link AllenConstraint}.
	 * @param a A {@link Term} representing an interval
	 * @return A {@link Collection} of {@link Statement}s
	 */
	public static  Collection<Statement> directlyConnectedStatements( ConstraintDatabase cdb, Term a ) {
		Set<Statement> r = new HashSet<Statement>();
		for ( AllenConstraint tC : cdb.getConstraints().get(AllenConstraint.class) ) {
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
	 * Compress temporal constraints by merging all intervals that have temporal
	 * Equal constraint between them.
	 * @return {@link Substitution} that was applied to merge intervals.
	 */
	public static Substitution compressTemporalConstraints( ConstraintDatabase cdb ) {
		Statement from, to;
		List<List<Term>> equalTerms = new ArrayList<List<Term>>();
		LinkedList<AllenConstraint> workList = new LinkedList<AllenConstraint>();
		Map<Term,Statement> statements = new HashMap<Term,Statement>();
		

		for ( AllenConstraint tC : cdb.getConstraints().get(AllenConstraint.class) ) {
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
			
			cdb.getConstraints().remove(first);

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
						cdb.getConstraints().remove(tC);	
						changed = true;
					}
					if ( included.contains(tC.getTo())  && !included.contains(tC.getFrom()) ) {
						included.add(tC.getFrom());
						workList.remove(i);
						i--;
						cdb.getConstraints().remove(tC);
						changed = true;
					}
				}
			}
			equalTerms.add(included);
		}
		
//		System.out.println(".-.............................");
		Substitution theta = new Substitution();
		for ( List<Term> allInts : equalTerms ) {			
//			System.out.println(allInts);
			Term keep = allInts.get(0);	
			for ( int i = 1 ; i < allInts.size() ; i++ ) {
				cdb.remove(statements.get(allInts.get(i)));
				theta.add(allInts.get(i),keep);
			}
		}
		
//		System.out.println("=============================================================================");
//		System.out.println("=============================================================================");
//		for ( Statement s : cdb.getConstraints().get(Statement.class)){ 
//			System.out.println(s);
//		}
//		for ( AllenConstraint s : cdb.getConstraints().get(AllenConstraint.class)){ 
//			System.out.println(s);
//		}
//		System.out.println("=============================================================================");
//		System.out.println("=============================================================================");
		

		Term.setAllowConstantSubstitution(true);
		cdb.substitute(theta);
		cdb.substitute(theta); //TODO: WTF
		Term.setAllowConstantSubstitution(false);
		
//		System.out.println(theta);
		
//		System.out.println("=============================================================================");
//		System.out.println("=============================================================================");
//		for ( Statement s : cdb.getConstraints().get(Statement.class)){ 
//			System.out.println(s);
//		}
//		for ( AllenConstraint s : cdb.getConstraints().get(AllenConstraint.class)){ 
//			System.out.println(s);
//		}
//		System.out.println("=============================================================================");
//		System.out.println("=============================================================================");
		
		return theta;
	}
	
	/**
	 * Compress temporal constraints by merging all intervals that have temporal
	 * Equal constraint between them.
	 */
	public static void compressAllTemporalConstraints( ConstraintDatabase cdb ) {
		Statement from, to;
		List<List<Term>> equalTerms = new ArrayList<List<Term>>();
		LinkedList<AllenConstraint> workList = new LinkedList<AllenConstraint>();
		Map<Term,Statement> statements = new HashMap<Term,Statement>();
		
	
		for ( AllenConstraint tC : cdb.getConstraints().get(AllenConstraint.class) ) {
			if ( tC.getRelation().equals(TemporalRelation.Equals) ) {
				from = cdb.getStatement(tC.getFrom());
				to = cdb.getStatement(tC.getTo());
				statements.put(from.getKey(),from);
				statements.put(to.getKey(),to);
				workList.add(tC);
			}
		}
		
		while ( !workList.isEmpty() ) {
			ArrayList<Term> included = new ArrayList<Term>();
			AllenConstraint first = workList.get(0);
			workList.remove(0);
			
			cdb.getConstraints().remove(first);

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
						cdb.getConstraints().remove(tC);	
						changed = true;
					} else if ( included.contains(tC.getTo())  && !included.contains(tC.getFrom()) ) {
						included.add(tC.getFrom());
						workList.remove(i);
						i--;
						cdb.getConstraints().remove(tC);
						changed = true;
					} else if ( included.contains(tC.getTo())  && included.contains(tC.getFrom()) ) {
						workList.remove(i);
						i--;
						cdb.getConstraints().remove(tC);
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
		cdb.substitute(theta);
	}
		
	/**
	 * Remove {@link Statement}s that are not
	 * connected to any other {@link Statement}
	 * via {@link AllenConstraint}s.
	 * This can reduce the workload of temporal reasoning.
	 */
	public static void removeDisconnectedTemporalConstraints( ConstraintDatabase cdb ) {
		
		HashMap<Term,Integer> countConnections = new HashMap<Term, Integer>();
		HashMap<Term,HashSet<String>> usedUnaryRelations = new HashMap<Term, HashSet<String>>();
		HashMap<Atomic,Integer> countAssignments = new HashMap<Atomic, Integer>();
		
		/**
		 * To how many other intervals is each interval linked?
		 */
		for ( Constraint c : cdb.getConstraints() ) {
			if ( c instanceof AllenConstraint ) {
				AllenConstraint tC = (AllenConstraint)c;
				
				if ( tC.getTo() != null ) {
					if (  !countConnections.containsKey(tC.getFrom()) ) {
						countConnections.put(tC.getFrom(), 0 );
					}
					countConnections.put(tC.getFrom(), countConnections.get(tC.getFrom()).intValue() + 1);
					
					if (  !countConnections.containsKey(tC.getTo()) ) {
						countConnections.put(tC.getTo(), 0 );
					}
					countConnections.put(tC.getTo(), countConnections.get(tC.getTo()).intValue() + 1);
				} else if ( !countConnections.containsKey(tC.getFrom()) || countConnections.get(tC.getFrom()).intValue() < 2 ) {	// If it has a connection to another interval we don't need another reason to keep it
					if ( !usedUnaryRelations.containsKey(tC.getFrom()) ) {
						usedUnaryRelations.put(tC.getFrom(), new HashSet<String>());
					}
					if ( usedUnaryRelations.get(tC.getFrom()).contains(tC.getRelation() ) ) {		// Same unary relation twice has to be checked
						countConnections.put( tC.getFrom() ,  100);
					} else {
						usedUnaryRelations.get(tC.getFrom()).add(tC.getRelation().toString());
					}
				}
			}
		}
		
		/**
		 * How many assignments of same variable (s.x) exist?
		 */
		for ( Statement s : cdb.getStatements() ) {
			if ( !countAssignments.containsKey(s.getVariable()) ) {
				countAssignments.put( s.getVariable(), 0 );
			}
			countAssignments.put( s.getVariable(), countAssignments.get(s.getVariable()).intValue()+1 );
		}
				
		HashSet<Term> keeperKeys = new HashSet<Term>();
		for ( Term k : countConnections.keySet() ) {
			if ( countConnections.get(k).intValue() > 1 ) {
				keeperKeys.add(k);
			}
		}
		
		HashSet<Term> remKeys = new HashSet<Term>();
		ArrayList<Statement> remList = new ArrayList<Statement>(); 
		for ( Statement s : cdb.getStatements() ) {
			boolean keep = false;
			keep = (keeperKeys.contains(s.getKey())) || (countAssignments.get(s.getVariable()).intValue() > 1);
			
			if ( !keep ) {
				remList.add(s);
				remKeys.add(s.getKey());
			}
		}
		
		cdb.getStatements().removeAll(remList);
		TemporalNetworkTools.removeTemporalConstraintsWithKeys(cdb, remKeys);
	}
	
	/**
	 * Remove all {@link AllenConstraint}s using a given set of interval keys.
	 * @param keys
	 */
	public static void removeTemporalConstraintsWithKeys( ConstraintDatabase cdb, Collection<Term> keys ) {
		HashSet<Constraint> remSet = new HashSet<Constraint>();
		for ( Constraint c : cdb.getConstraints() ) {
			if ( c instanceof AllenConstraint ) {
				AllenConstraint tC = (AllenConstraint)c;
				
				if ( keys.contains(tC.getFrom()) || keys.contains(tC.getTo()) ) {
					remSet.add(tC);
				}				
			}
		}
		cdb.getConstraints().removeAll(remSet);
	}
		
	/**
	 * Specialized version of the difference function that only considers statements
	 * and temporal constraints.
	 * @param cdb
	 * @return
	 */
	public static ConstraintDatabase temporalDifference( ConstraintDatabase cdb1, ConstraintDatabase cdb2 ) {		
		ConstraintDatabase diff = new ConstraintDatabase();
		
		for ( Statement s : cdb1.getStatements() ) {
			if ( !cdb2.getStatements().contains(s) ) {
				diff.add(s);
			}
		}
		
		for ( AllenConstraint c : cdb1.getConstraints().get(AllenConstraint.class) ) {
			if ( !cdb2.getConstraints().contains(c) ) {
				diff.add(c);
			}
		}

		return diff;
	}
	public static boolean isContainedIn( ConstraintDatabase cdb1, ConstraintDatabase cdb2 ) {		
//		StopWatch.start("isContainedIn");

		List<Statement> S1 = cdb1.getConstraints().get(Statement.class);
		List<Statement> S2 = cdb2.getConstraints().get(Statement.class);
		
		if ( S1.size() > S2.size() ) {
			return false;
		}
		
		for ( int i = 0 ; i < S1.size() ; i++ ) {
			if ( !S1.get(i).equals(S2.get(i)) ) {
				return false;
			}
		}
		
		List<AllenConstraint> AC1 = cdb1.getConstraints().get(AllenConstraint.class);
		List<AllenConstraint> AC2 = cdb2.getConstraints().get(AllenConstraint.class);
		
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
	 * Specialized version of the difference function that only considers statements
	 * and temporal constraints.
	 * @param cdb
	 * @return
	 */
	public static int temporalHash( ConstraintDatabase cdb ) {		
		return 	cdb.getConstraints().get(Statement.class).hashCode() 
				+ 3*cdb.getConstraints().get(AllenConstraint.class).hashCode();
	}
	
	/**
	 * Creates subsets of increasing size of this TemporalDatabase and checks their consistency until
	 * an inconsistent TemporalDatabase is found. This inconsistent TemporalDatabase is then returned.
	 * This can be useful for debugging, since it summarizes unwanted inconsistencies.
	 * @param dM DomainManager is needed since it knows resource capacities. 
	 * @return
	 * @throws NonGroundThing
	 * @throws UnknownThing
	 */
	public static ConstraintDatabase getSmallestInconsistentSubDB( ConstraintDatabase cdb, TypeManager tM ) {
		IncrementalSTPSolver csp = new IncrementalSTPSolver(0, Global.MaxTemporalHorizon);
		
		for ( int i = 2 ; i <= cdb.getStatements().size(); i++ ) {
			ArrayList<ConstraintDatabase> subs = TemporalNetworkTools.getSubsetDatabases(cdb, i);
			
			for ( ConstraintDatabase subDB : subs ) {
				if ( !csp.isConsistent(subDB, tM) ) {
					return subDB;
				}
			}
		}
		return null;
	}
	
	/**
	 * Get all TemporalDatabases with n-size subsets of statements in F and 
	 * and only constraints between Statements in the subsets. Only keeps
	 * those TemporalDatabases that have at least one binary constraint.
	 * @param n
	 */
	public static ArrayList<ConstraintDatabase> getSubsetDatabases( ConstraintDatabase cdb, int n ) {
		ArrayList<ConstraintDatabase> r = new ArrayList<ConstraintDatabase>();
		
		GenericComboBuilder<Statement> cB = new GenericComboBuilder<Statement>();
		
		List<Statement> S = new ArrayList<Statement>();
		S.addAll(cdb.getStatements());
		List<List<Statement>> combos = cB.getCombosSingleList(S, n, false);
		
		for ( List<Statement> c : combos ) {
			ConstraintDatabase fC = new ConstraintDatabase();
			fC.getStatements().clear();
			fC.addStatements(c);
			int numBinConstraints = 0;
			for ( Constraint cons : cdb.getConstraints() ) {
				if ( cons instanceof AllenConstraint ) {
					AllenConstraint tC = (AllenConstraint)cons;
					
					if ( TemporalNetworkTools.hasFittingStatements(fC,tC)) {
						fC.add(tC);
						if ( tC.getTo() != null ) {
							numBinConstraints ++;
						}
					}
				}
			}
			if ( numBinConstraints > 0 ) {
				r.add(fC);
			}
		}		
		return r;
	}	
	
	/**
	 * Remove all groups in from {@link AllenConstraint}s and add new {@link AllenConstraint}s for 
	 * combinations of group members.
	 * @param C {@link Collection} of {@link Constraint}s
	 * @param groupMapping A mapping of group key {@link Term} to all their member key {@link Term}s.
	 */
	public static void replaceGroupKeys( Collection<Constraint> C, Map<Term,ArrayList<Term>> groupMapping ) {
		ArrayList<AllenConstraint> addList = new ArrayList<AllenConstraint>();
		ArrayList<AllenConstraint> remList = new ArrayList<AllenConstraint>();
		
		for ( Constraint c : C ) {
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
	
	/**
	 * Check a set of {@link PossibleIntersection} constraints given their bounds.
	 * This method is here so it can be used both for {@link MetaCSPAdapter} and
	 * {@link MetaCSPAdapterWithHistory} who provide the input data.
	 * <p>
	 * Definition of intersection is taken from:
	 * <p>
	 * A. Cesta, A. Oddi, and S.F. Smith. 
	 * A Constraint-Based Method for Project Scheduling with Time Windows. 
	 * Journal of Heuristics, 8:109–136, 2002.
	 * 
	 * @param piCs
	 * @param bounds
	 * @return <i>true</i> iff all intersections are possible (using earliest timeline assumption), <i>false</i> otherwise
	 */
	public static boolean checkPossibleIntersectionConstraints( Collection<PossibleIntersection> piCs, Map<Term,ArrayList<Long>> bounds ) {

		for ( PossibleIntersection piC : piCs ) {
			long minEET = Long.MAX_VALUE;
			long maxEST = 0;
			
			for ( Term interval : piC.getIntervals() ) {
				long EST = bounds.get(interval).get(0).longValue();
				long EET = bounds.get(interval).get(2).longValue();
					
				if ( EST > maxEST ) {
					maxEST = EST;
				}
				if ( EET < minEET ) {
					minEET = EET;
				}
			}
			if ( !(minEET > maxEST) ) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Returns a string with some compressed information about sizes and
	 * {@link AllenConstraint} counts.
	 * 
	 * @return
	 */
	public static String getStatsString( ConstraintDatabase cdb ) {
		String r = "";
		r += "Num. statements: " + cdb.getStatements().size() + "\n";
		r += "Num. constraints: " + cdb.getConstraints().size() + "\n";
		
		HashMap<String,Integer> cCount = new HashMap<String, Integer>();
		
		for( Constraint c : cdb.getConstraints() ) {
			if ( c instanceof AllenConstraint ) {
				AllenConstraint tC = (AllenConstraint)c;
				
				if ( !cCount.containsKey(tC.getRelation().toString())) {
					cCount.put(tC.getRelation().toString(), new Integer(0));
				}
				
				cCount.put(tC.getRelation().toString(), new Integer(cCount.get(tC.getRelation().toString()).intValue() + 1));
			}
		}
		
		for ( String k : cCount.keySet() ) {
			r += "Relation " + k + " -> " + cCount.get(k) + "\n";
		}
		return r;
	}
	
	
	public static ConstraintDatabase createSubNetwork( ConstraintDatabase cDB, Collection<Atomic> included, Collection<Atomic> excluded ) {
		ArrayList<Atomic> in = new ArrayList<Atomic>();
		ArrayList<Atomic> out = new ArrayList<Atomic>();
		
		if ( included != null ) {
			in.addAll(included);
		}
		if ( excluded != null ) {
			out.addAll(excluded);
		}
		
		ConstraintDatabase r = new ConstraintDatabase();
		for ( Statement s : cDB.getStatements() ) {
			if ( in.contains(s.getVariable()) && !out.contains(s.getVariable()) ) {
				r.add(s);
			}
		}
		ArrayList<Statement> S = new ArrayList<Statement>();
		S.addAll(r.getStatements());
		for ( int i = 0 ; i < r.getStatements().size()-1 ; i++ ) {
			for ( int j = i+1 ; j < r.getStatements().size() ; j++ ) {
//				Term a = r.getStatements().get(i).getKey();
//				Term b = r.getStatements().get(j).getKey();
				Term a = S.get(i).getKey();
				Term b = S.get(j).getKey();
				r.addConstraints(getTemporalConstraintsBetween(cDB, a, b));
			}
		}		
		return r;
	}
	

	public static Map<Atomic,List<Term>> getSequencedValues( ConstraintDatabase cdb, TypeManager tM ) {
		TemporalNetworkTools tools = new TemporalNetworkTools();
		
		IncrementalSTPSolver csp = new IncrementalSTPSolver(0, Global.MaxTemporalHorizon);
		csp.isConsistent(cdb, tM);
		
		Map<Atomic,List<TermAndEST>> sequencedValues = new HashMap<Atomic, List<TermAndEST>>();
		
		for ( Statement s : cdb.getConstraints().get(Statement.class)) {
			if ( !sequencedValues.containsKey(s.getVariable()) ) {
				sequencedValues.put(s.getVariable(), new ArrayList<TermAndEST>());
			}
			TermAndEST x = tools.new TermAndEST(s.getValue(), csp.getEST(s.getKey()));
			sequencedValues.get(s.getVariable()).add(x);
		}
		
		Map<Atomic,List<Term>> r = new HashMap<Atomic, List<Term>>();
		for ( Atomic key : sequencedValues.keySet() ) {
			Collections.sort(sequencedValues.get(key));
			List<Term> l = new ArrayList<Term>();
			for ( TermAndEST v : sequencedValues.get(key) ) {
				l.add(v.value);
			}
			r.put(key, l);
		}
		return r;
	}
	
	public static Map<Atomic,List<Term>> getSequencedIntervals( ConstraintDatabase cdb, TypeManager tM ) {
		TemporalNetworkTools tools = new TemporalNetworkTools();
		TemporalIntervalLookup tiLookup = cdb.getConstraints().get(TemporalIntervalLookup.class).get(0);
		
		Map<Atomic,List<TermAndEST>> sequencedValues = new HashMap<Atomic, List<TermAndEST>>();
		
		for ( Statement s : cdb.getConstraints().get(Statement.class)) {
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
		List<Statement> S = cdb.getConstraints().get(Statement.class);
		if ( S.isEmpty() ) {
			return new ArrayList<Statement>();
		}
		PlanningInterval pI = ConstraintRetrieval.getPlanningInterval(cdb);
		TemporalIntervalLookup tiLookup = cdb.getConstraints().get(TemporalIntervalLookup.class).get(0);
		
		long tMin = 0;
		long tMax = Global.MaxTemporalHorizon;
		
		if ( pI != null ) {
			tMin = pI.getStartTimeValue();
			tMax = pI.getHorizonValue();
		}
		
		Collection<Statement> r = new ArrayList<Statement>();
		
		for ( Statement s : cdb.getConstraints().get(Statement.class) ) {
			if ( tiLookup.getLET(s.getKey()) >= tMin && tiLookup.getEST(s.getKey()) <= tMax ) {
				r.add(s);
			}
		}
		
		return r;
	}
	

	
	public static void inspect( ConstraintDatabase cDB, TypeManager tM ) {
		IncrementalSTPSolver csp = new IncrementalSTPSolver(0, Global.MaxTemporalHorizon);
		csp.debug = true;
		csp.isConsistent(cDB, tM);
		Loop.start();
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
	 * @param fName Target filename
	 */
	public static void dumbTimeLineData( ConstraintDatabase cDB, TemporalIntervalLookup tiLookup, String fName ) {
		StringBuilder sb = new StringBuilder();
			
		for ( Statement s : cDB.getConstraints().get(Statement.class)) {
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
