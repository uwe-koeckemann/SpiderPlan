/*******************************************************************************
 * Copyright (c) 2015-2017 Uwe Köckemann <uwe.kockemann@oru.se>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package org.spiderplan.temporal.stpSolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.expressions.Statement;
import org.spiderplan.representation.expressions.ValueLookup;
import org.spiderplan.representation.expressions.ExpressionTypes.TemporalRelation;
import org.spiderplan.representation.expressions.temporal.AllenConstraint;
import org.spiderplan.representation.expressions.temporal.Interval;
import org.spiderplan.representation.expressions.temporal.PossibleIntersection;
import org.spiderplan.representation.expressions.temporal.SimpleDistanceConstraint;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.tools.logging.Logger;
import org.spiderplan.tools.stopWatch.StopWatch;

/**
 * Solver for {@link AllenConstraint} and {@link SimpleDistanceConstraint}.
 * 
 * @author Uwe Köckemann
 *
 */
public class IncrementalSTPSolver { //implements TemporalReasoningInterface {
	
	private String name = "incSTP";
	private boolean verbose = false;

	private long[][] d;
	private Map<Term,Integer> tpStart;
	private List<Statement> addedStatements;
	private List<AllenConstraint> addedAllenConstraints;
	private List<SimpleDistanceConstraint> addedSimpleDistanceConstraints;
	
	private int maxHistorySize = 100;
	private List<long[][]> dHistory;
	private List<Map<Term,Integer>> tpStartHistory;
	private List<List<Statement>> addedStatementsHistory;
	private List<List<AllenConstraint>> addedAllenConstraintsHistory;
	private List<List<SimpleDistanceConstraint>> addedSimpleDistanceConstraintsHistory;
	
	private Map<Long[],Object> debugLookUp = new HashMap<Long[], Object>(); 
	
	private long O = 0L;
	private long H = 10;
	private long INF = Long.MAX_VALUE/2-2;
	
	private int tpOrigin = 0;
		
	private boolean needFromScratch = false;
//	@SuppressWarnings("unused")
	private boolean keepTimes = false;
	@SuppressWarnings("unused")
	private boolean keepStats = false;
	private boolean propagationRequired = false;
	private boolean usedRevert = false;
	
	private boolean useLinearRevert = false;
	/**
	 * Simple flag to switch debug mode on and off. 
	 * Debug mode will provide details about 
	 * individual temporal constraints and point out
	 * when inconsistencies arise.
	 */
	public boolean useCulpritDetection = false;
	
	/**
	 * Create new solver by providing temporal origin and horizon.
	 * @param origin the earliest considered time
	 * @param horizon the latest considered time
	 */
	public IncrementalSTPSolver( long origin, long horizon ) {
		this.O = origin;
		this.H = horizon;
		reset();
	}
	
	/**
	 * Check a Constraint Database for temporal consistency. 
	 * @param cdb Constraint database
	 * @return <code>true</code> if simple temporal network resulting of all constraints is consistent.
	 */
	public boolean isConsistent( ConstraintDatabase cdb ) {
		return this.isConsistent(cdb.get(Statement.class), cdb.get(AllenConstraint.class), cdb.get(SimpleDistanceConstraint.class));
	}
		
	/**
	 * Test temporal consistency of a constraint-database
	 * @param S Statements
	 * @param AC Quantified Allen interval constraints
	 * @param SDC Simple distance constraints
	 * @return <code>true</code> if the CDB is consistent, <code>false</code> otherwise
	 */
	public boolean isConsistent( List<Statement> S, List<AllenConstraint> AC, List<SimpleDistanceConstraint> SDC ) {
		propagationRequired = false;
		usedRevert = false;
		
		List<Statement> newStatements;
		List<AllenConstraint> newAllenConstraints;
		List<SimpleDistanceConstraint> newSimpleDistanceConstraints;

		int revertToIndex;
		
		if ( !useCulpritDetection ) {
			if ( useLinearRevert ) {
				if ( keepTimes ) StopWatch.start("[incSTP] 1) Finding revert level (linear)");
				
				revertToIndex = dHistory.size()-1;
				List<Integer> beginIndex = null;
				
				if ( !dHistory.isEmpty() ) {
					while ( revertToIndex >= 0) {
						beginIndex = this.getFirstNewIndex(revertToIndex, S, AC, SDC);
						if ( beginIndex != null ) {
							break;
						}
						revertToIndex--;
					}
				}
				if ( keepTimes ) StopWatch.stop("[incSTP] 1) Finding revert level (linear)");
				
				if ( revertToIndex == -1 ) { // history does not contain suitable reverting point
					if ( verbose ) Logger.msg(this.name, "Propagating from scratch...", 2);
					
					needFromScratch = true;
					beginIndex = new ArrayList<Integer>();
					beginIndex.add(0);
					beginIndex.add(0);
					beginIndex.add(0);
				} else {
					needFromScratch = false;
					
					if ( revertToIndex < dHistory.size()-1 ) {
						if ( verbose ) Logger.msg(this.name, "Reverting to " + revertToIndex + "/" + (dHistory.size()-1), 2);
						if ( verbose ) Logger.msg(this.name, "Begin index: " + beginIndex.toString(), 2);
						
						if ( keepTimes ) StopWatch.start("[incSTP] 1-a) Reverting");
						this.revert(revertToIndex);
						this.bookmark();
						if ( keepTimes ) StopWatch.stop("[incSTP] 1-a) Reverting");
					}
					if ( beginIndex.get(0) == addedStatements.size() 
							&& beginIndex.get(1) == addedAllenConstraints.size() 
							&& beginIndex.get(2) == addedSimpleDistanceConstraints.size() ) 
					{
						propagationRequired = false;
					} 
				}
				
				newStatements = S.subList(beginIndex.get(0), S.size());
				newAllenConstraints = AC.subList(beginIndex.get(1), AC.size());
				newSimpleDistanceConstraints = SDC.subList(beginIndex.get(2), SDC.size());
			} else {
				if ( keepTimes ) StopWatch.start("[incSTP] 1) Finding revert level (quadratic)");				
				
//				List<Statement> cdbStatements = cDB.get(Statement.class);				
//				List<AllenConstraint> cdbAllenConstraints = cDB.get(AllenConstraint.class);
//				List<SimpleDistanceConstraint> cdbSimpleDistanceConstraints = cDB.get(SimpleDistanceConstraint.class);
								
				revertToIndex = dHistory.size()-1;
		
				if ( !dHistory.isEmpty() ) {
	//				System.out.println("Trying to find revert level...");
					while ( revertToIndex >= 0 ) {
						List<Statement> statementsFromHistory = addedStatementsHistory.get(revertToIndex);
						List<AllenConstraint> allenConstraintsFromHistory = addedAllenConstraintsHistory.get(revertToIndex);
						
						boolean good = S.containsAll(statementsFromHistory);
						good &= AC.containsAll(allenConstraintsFromHistory);
						
						if ( good ) {
							break;
						}
						revertToIndex--;
					}
				}
				if ( keepTimes ) StopWatch.stop("[incSTP] 1) Finding revert level (quadratic)");
				
	//			System.out.println("Result: "  + revertToIndex);
				
				if ( revertToIndex == -1 ) { // history does not contain suitable reverting point
					needFromScratch = true;
					
					if ( verbose ) Logger.msg(this.name, "Propagating from scratch...", 2);
					newStatements = new ArrayList<Statement>();
					newAllenConstraints = new ArrayList<AllenConstraint>();
					newSimpleDistanceConstraints = new ArrayList<SimpleDistanceConstraint>();
					
					newStatements.addAll(S);
					newAllenConstraints.addAll(AC);	
					newSimpleDistanceConstraints.addAll(SDC);				
				} else {
					needFromScratch = false;
					
					if ( revertToIndex < dHistory.size()-1 ) {
						usedRevert = true;
						if ( verbose ) Logger.msg(this.name, "Reverting to " + revertToIndex + "/" + (dHistory.size()-1), 2);
						if ( keepTimes ) StopWatch.start("[incSTP] 1-a) Reverting");
						this.revert(revertToIndex);
						this.bookmark();
										
						if ( keepTimes ) StopWatch.stop("[incSTP] 1-a) Reverting");
					}
					if ( S.size() == addedStatementsHistory.get(revertToIndex).size() && AC.size() == addedAllenConstraintsHistory.get(revertToIndex).size() ) {
						// nothing new
						propagationRequired = false;
						newStatements = new ArrayList<Statement>();
						newAllenConstraints = new ArrayList<AllenConstraint>();
						newSimpleDistanceConstraints = new ArrayList<SimpleDistanceConstraint>();
					} else {
						// only propagate new constraints
						newStatements = new ArrayList<Statement>();
						newAllenConstraints = new ArrayList<AllenConstraint>();
						newSimpleDistanceConstraints = new ArrayList<SimpleDistanceConstraint>();
						
						newStatements.addAll(S);
						newAllenConstraints.addAll(AC);
						newSimpleDistanceConstraints.addAll(SDC);
						
						newStatements.removeAll(addedStatementsHistory.get(revertToIndex));
						newAllenConstraints.removeAll(addedAllenConstraintsHistory.get(revertToIndex));
						newSimpleDistanceConstraints.remove(addedSimpleDistanceConstraintsHistory.get(revertToIndex));
					}
				}
			}
		} else {
//			List<Statement> cdbStatements = cDB.get(Statement.class);				
//			List<AllenConstraint> cdbAllenConstraints = cDB.get(AllenConstraint.class);
//			List<SimpleDistanceConstraint> cdbSimpleDistanceConstraints = cDB.get(SimpleDistanceConstraint.class);
			
			newStatements = new ArrayList<Statement>();
			newAllenConstraints = new ArrayList<AllenConstraint>();
			newSimpleDistanceConstraints = new ArrayList<SimpleDistanceConstraint>();
			
			newStatements.addAll(S);
			newAllenConstraints.addAll(AC);
			newSimpleDistanceConstraints.addAll(SDC);
		}
		
		
//		System.out.println("====================================================");
//		System.out.println(addedStatements);
//		System.out.println(addedAllenConstraints);
		
//		if ( !distanceHistory.isEmpty() ) {
//			System.out.println(addedStatementsHistory.get(revertToIndex));
//			System.out.println(addedAllenConstraintsHistory.get(revertToIndex));
//		}
		
		
//		System.out.println("From scratch? " + needFromScratch);
//		System.out.println("Propagation required? " + propagationRequired);
//		System.out.println("Best revert: " + (dHistory.size()-1));
//		System.out.println("Reverted to " + revertToIndex);
//		
//		System.out.println("newStatements " + newStatements.size());
//		System.out.println("newAllenConstraints " + newAllenConstraints.size());
		
		if ( useCulpritDetection ) {
			needFromScratch = true;
		}
		
		if ( needFromScratch ) {
//			if ( keepStats ) Module.getStats().increment("[incSTP] #FromScratch");
			reset();
			propagationRequired = true;
		}
		
		List<Long[]> addedConstraints = new ArrayList<Long[]>();
		
//		if ( propagationRequired ) {			
			if ( keepTimes ) StopWatch.start("[incSTP] Matrix setup");		
			addedConstraints.addAll(setupDistanceMatrix(newStatements));
			if ( keepTimes ) StopWatch.stop("[incSTP] Matrix setup");
			
			if ( keepTimes ) StopWatch.start("[incSTP] Generating simple distance constraints");
			addedConstraints.addAll(this.getNewDistanceConstraints(newAllenConstraints));
			if ( keepTimes ) StopWatch.stop("[incSTP] Generating simple distance constraints");
			
			addedConstraints.addAll(this.convertSimpleDistanceConstraints(newSimpleDistanceConstraints));
			
			if ( !addedConstraints.isEmpty() ) {
				propagationRequired = true;
			} 
//		}
		
		if ( keepTimes ) StopWatch.start("[incSTP] Propagation");
		boolean isConsistent = true;

		for ( Long[] con : addedConstraints ) {
			isConsistent &= incrementalDistanceMatrixComputation(con[0].intValue(), con[1].intValue(), con[2], con[3]);
						
			if ( !isConsistent ) {
				if ( useCulpritDetection ) {
					System.err.println("Looking for culprit...");
					
					List<Long[]> culprit = this.findCulprit(addedConstraints, S);
					
					for ( Long[] culprit_con : culprit ) {
						System.err.println("[CULPRIT] " + debugLookUp.get(culprit_con));
					}
				}
				break;
			}
		}

		if ( keepTimes ) StopWatch.stop("[incSTP] Propagation");
		
		if ( isConsistent && propagationRequired && !useCulpritDetection ) {
			this.bookmark();
		} else if ( !isConsistent && !useCulpritDetection && getHistorySize() > 0) {
			this.revert(getHistorySize()-1);
			this.bookmark();
		}
//		System.out.println("RETURNING: " + isConsistent);
		return isConsistent;
	}	
	
	private List<Long[]> findCulprit( List<Long[]> addedC, List<Statement> S ) {
		LinkedList<Long[]> C = new LinkedList<Long[]>();
		for ( Long[] c : addedC ) {
			C.add(c);
		}
		
		boolean done = false;
		
		int culpritFringe = -1;
		
		while ( !done ) {
			
			this.d = null;
			this.setupDistanceMatrix(S);
			
			boolean isConsistent = true;
			
			int i = 0;
			
			for ( Long[] c : C ) {
				isConsistent &= incrementalDistanceMatrixComputation(c[0].intValue(), c[1].intValue(), c[2], c[3]);
				
				if ( !isConsistent ) {
					LinkedList<Long[]> Cnew = new LinkedList<>();
					Cnew.addFirst(c);
					for ( int j = 0 ; j < i ; j++ ) {
						Cnew.add(C.get(j));
					}
					C = Cnew;
					culpritFringe++;
					done = (i <= culpritFringe);					
					break;
				}
				i++;
			}
		}
				
		return C;
	}
	
	
	private List<Long[]> setupDistanceMatrix( List<Statement> S ) {
		List<Long[]> r = new Vector<Long[]>();
		
		if ( S.isEmpty() ) {
			return r;
		}

		int nextFreeTP;
		
		if ( d == null ) {
			d = new long[S.size()*2+2][S.size()*2+2];
			d[0][0] = 0L;
			d[0][1] = H;
			d[1][0] = -H;
			d[1][1] = 0L;
			nextFreeTP = 2;
		} else {
			int newDim = d.length + S.size()*2;
			nextFreeTP = d.length;
			
			long[][] d_prime = new long[newDim][newDim];
			for ( int i = 0 ; i < d.length ; i++ ) {
				for ( int j = 0 ; j < d.length ; j++ ) {
					d_prime[i][j] = d[i][j];
				}
			}
			d = d_prime;
		}		
		
		
		for ( int i = 0 ; i < S.size() ; i++ ) {
			Statement s = S.get(i);
			this.addedStatements.add(s);
//			if ( !tpStart.containsKey(s.getKey()) ) {
				int startTP = nextFreeTP;
				int endTP = startTP+1;
				
				nextFreeTP += 2;
				
				tpStart.put(s.getKey(), startTP);
					
				d[0][startTP] = H;
				d[0][endTP] = H;	
				d[1][startTP] = 0L;
				d[1][endTP] = 0L;
				d[startTP][0] = 0L;
				d[startTP][1] = H;
				d[endTP][0] = 0L;
				d[endTP][1] = H;
								
				for ( int j = 2 ; j < d.length-2 ; j++ ) {
					d[startTP][j] = H;
					d[endTP][j] = H;
					d[j][startTP] = H;
					d[j][endTP] = H;
				}
				
				d[startTP][startTP] = 0L;
				d[endTP][endTP] = 0L;
				d[startTP][endTP] = H;
				d[endTP][startTP] = H;
				
//				distance.get(startTP).add(0L);
//				distance.get(startTP).add(H);
//				distance.get(endTP).add(H);
//				distance.get(endTP).add(0L);
				
				List<Long[]> cons = new ArrayList<Long[]>();
				
				{
					Long[] dConMinDur = new Long[4];
					dConMinDur[0] = (long) startTP;
					dConMinDur[1] = (long) endTP;
					dConMinDur[2] = 0L;
					dConMinDur[3] = H;
					r.add(dConMinDur);
					cons.add(dConMinDur);
					if ( useCulpritDetection ) {
						debugLookUp.put(dConMinDur, s);
					}
				}

//				sdMap.put(s.getKey(), cons);
//			}
		}
		return r;
	}

	private void reset( ) {
		d = null; //new ArrayList<List<Long>>();
		tpStart = new HashMap<Term, Integer>();
		
		tpStart.put(Term.createConstant("_OH_"), 0); //TODO find better name

		addedStatements = new ArrayList<Statement>();
		addedAllenConstraints = new ArrayList<AllenConstraint>();
		addedSimpleDistanceConstraints = new ArrayList<SimpleDistanceConstraint>();
		
		dHistory = new ArrayList<long[][]>();
		tpStartHistory = new ArrayList<Map<Term,Integer>>();
		addedStatementsHistory = new ArrayList<List<Statement>>();
		addedAllenConstraintsHistory = new ArrayList<List<AllenConstraint>>();
		addedSimpleDistanceConstraintsHistory = new ArrayList<List<SimpleDistanceConstraint>>();
	}
	
	private List<Long[]> convertSimpleDistanceConstraints( List<SimpleDistanceConstraint> SDC ) {
		List<Long[]> r = new Vector<Long[]>();
		
		for ( SimpleDistanceConstraint sdc : SDC ) {
			this.addedSimpleDistanceConstraints.add(sdc);
			Long[] dCon = new Long[4];
			dCon[0] = Long.valueOf(tpStart.get(sdc.getFrom()));		// set to start time point of interval
			dCon[1] = Long.valueOf(tpStart.get(sdc.getTo()));		// set to start time point of interval
			dCon[2] = sdc.getBound().getLower();
			dCon[3] = sdc.getBound().getUpper();

			if ( sdc.getFromPoint().equals(SimpleDistanceConstraint.TimePoint.ET)) {
				dCon[0]++; 		// change to end time point if needed
			}
			if ( sdc.getToPoint().equals(SimpleDistanceConstraint.TimePoint.ET)) {
				dCon[1]++;		// change to end time point if needed
			}
			r.add(dCon);
		}
		
		return r;
	}
	
	private List<Long[]> getNewDistanceConstraints( List<AllenConstraint> AC ) {
		List<Long[]> r = new Vector<Long[]>();
		for ( int i = 0 ; i < AC.size() ; i++ ) {
			AllenConstraint ac = AC.get(i);
						
			this.addedAllenConstraints.add(ac);
//			if ( !this.acdMap.containsKey(ac) ) {
//				System.out.println(ac);
				List<Long[]> addedConstraints = new Vector<Long[]>();
				if ( ac.isUnary() ) {
					Integer fs = tpStart.get(ac.getFrom());
					if ( fs == null ) {
						throw new IllegalStateException("Interval " + ac.getFrom() + " of temporal constraint " + ac + " does not have corresponding statement.");
					}
					int fe = fs+1;
					addedConstraints.addAll(this.unaryAllen2SimpleDistance(ac, fs, fe));
				} else {
					Integer fs = tpStart.get(ac.getFrom());
					Integer ts = tpStart.get(ac.getTo());
					if ( fs == null ) {
//						throw new IllegalStateException("Interval " + ac.getFrom() + " of temporal constraint " + ac + " does not have corresponding statement.");
						continue;
					}
					if ( ts == null ) {
						continue;
//						throw new IllegalStateException("Interval " + ac.getTo() + " of temporal constraint " + ac + " does not have corresponding statement.");
					}
					int fe = fs+1;
					int te = ts+1;
					addedConstraints.addAll(this.binaryAllen2SimpleDistance(ac, fs, fe, ts, te));
				}
							
//				this.acdMap.put(ac, addedConstraints);
				r.addAll(addedConstraints);
				
				if ( useCulpritDetection ) {
					for ( Long[] con : addedConstraints ) {
						debugLookUp.put(con, ac);
					}
					
//					System.err.println(addedConstraints);
				}
				
//				for ( Long[] sdc : addedConstraints ) {
//					System.out.println(Arrays.toString(sdc));
//				}
//			}
		}
		return r;
	}
	
	
	
	
//	public String getDistanceMatrixString() {
//		StringBuilder sb = new StringBuilder();
//		
//		for ( long r : this.d ) {
//			sb.append(r.toString());
//			sb.append("\n");
//		}
//		return sb.toString();
//	}
	
	/**
	 * Get a map from temporal interval keys to the propagated bounds
	 * of their start and end times.
	 * @param valueLookup value lookup table to add the intervals 
	 * @return <code>true</code> if something has change, <code>false</code> otherwise
	 */
	public boolean getPropagatedTemporalIntervals( ValueLookup valueLookup ) {
		boolean change = false;
		for ( Statement s : addedStatements ) {
			Long[] bound = new Long[4];
			bound[0] = this.getEST(s.getKey());
			bound[1] = this.getLST(s.getKey());
			bound[2] = this.getEET(s.getKey());
			bound[3] = this.getLET(s.getKey());
			if ( !valueLookup.hasInterval(s.getKey()) ) {
				change = true;
			} else if ( bound[0] != this.getEST(s.getKey()) ) {
				change = true;
			} else if ( bound[1] != this.getLST(s.getKey()) ) {
				change = true;
			} else if ( bound[2] != this.getEET(s.getKey()) ) {
				change = true;
			} else if ( bound[3] != this.getLET(s.getKey()) ) {
				change = true;
			}
			valueLookup.putInterval(s.getKey(), bound);
		}
		return change;
	}
	
	private long sum(long a, long b) {
		if ( a+b > H ) return H;
		return a+b;
	}
		
	private boolean incrementalDistanceMatrixComputation( int from, int to, long min, long max ) {
		if (d[to][from] != INF && sum( max,d[to][from]) < 0) return false;
		if (d[from][to] != INF && sum(-min,d[from][to]) < 0) return false;
				
		long sum1;
		long sum2;
		long sum3;
		long sum4;
		long temp;
		
		for (int u = 0 ; u < d.length ; u++) {
			for (int v = 0 ; v < d.length ; v++) {
				sum1 = sum(d[u][to],-min);
				sum2 = sum(sum1,d[from][v]);
				sum3 = sum(d[u][from],max);
				sum4 = sum(sum3,d[to][v]);
				temp = Math.min(sum2,sum4);
										
				if (d[u][v] > temp) {			
					d[u][v] = temp;
				
					if (u == v && d[u][v] != 0L) {
						return false;
					}
				}
			}
		}
		return true;
	}
	
	private List<Long[]> unaryAllen2SimpleDistance( AllenConstraint c, long fs, long fe ) {
		List<Long[]> r = new ArrayList<Long[]>();
		 if ( c.getRelation().equals(TemporalRelation.Release) ) {
			Long[] dCon1 = new Long[4];
			dCon1[0] = (long) tpOrigin;
			dCon1[1] = fs;
			dCon1[2] = Math.min(INF, c.getBound(0).getLower());
			dCon1[3] = Math.min(INF, c.getBound(0).getUpper());
		
			r.add(dCon1);
		} else if ( c.getRelation().equals(TemporalRelation.Deadline) ) {
			Long[] dCon1 = new Long[4];
			dCon1[0] = (long) tpOrigin;
			dCon1[1] = fe;
			dCon1[2] = Math.min(INF, c.getBound(0).getLower());
			dCon1[3] = Math.min(INF, c.getBound(0).getUpper());
		
			r.add(dCon1);
		}  else if ( c.getRelation().equals(TemporalRelation.Duration) ) {
			Long[] dCon1 = new Long[4];
			dCon1[0] = fs;
			dCon1[1] = fe;
			dCon1[2] = Math.min(INF, c.getBound(0).getLower());
			dCon1[3] = Math.min(INF, c.getBound(0).getUpper());
		
			r.add(dCon1);
		} else if ( c.getRelation().equals(TemporalRelation.At) ) {
			Long[] dCon1 = new Long[4];
			dCon1[0] = (long) tpOrigin;
			dCon1[1] = fs;
			dCon1[2] = Math.min(INF, c.getBound(0).getLower());
			dCon1[3] = Math.min(INF, c.getBound(0).getUpper());
			
			Long[] dCon2 = new Long[4];
			dCon2[0] = (long) tpOrigin;
			dCon2[1] = fe;
			dCon2[2] = Math.min(INF, c.getBound(1).getLower());
			dCon2[3] = Math.min(INF, c.getBound(1).getUpper());
			r.add(dCon1);
			r.add(dCon2);
		} 
		 return r;
	}
	
	
	private List<Long[]> binaryAllen2SimpleDistance( AllenConstraint c, long fs, long fe, long ts, long te ) {
		List<Long[]> r = new ArrayList<Long[]>();
		
		if ( c.getRelation().equals(TemporalRelation.Before) || c.getRelation().equals(TemporalRelation.BeforeOrMeets)) {
			Long[] dCon = new Long[4];
			dCon[0] = fe;
			dCon[1] = ts;
			dCon[2] = Math.min(INF, c.getBound(0).getLower());
			dCon[3] = Math.min(INF, c.getBound(0).getUpper());
			r.add(dCon);
		} else if ( c.getRelation().equals(TemporalRelation.After) || c.getRelation().equals(TemporalRelation.MetByOrAfter)) {
			Long[] dCon = new Long[4];
			dCon[0] = te;
			dCon[1] = fs;
			dCon[2] = Math.min(INF, c.getBound(0).getLower());
			dCon[3] = Math.min(INF, c.getBound(0).getUpper());
			r.add(dCon);
		} else if ( c.getRelation().equals(TemporalRelation.Equals) ) {
			Long[] dCon1 = new Long[4];
			dCon1[0] = fs;
			dCon1[1] = ts;
			dCon1[2] = 0L;
			dCon1[3] = 0L;
			
			Long[] dCon2 = new Long[4];
			dCon2[0] = fe;
			dCon2[1] = te;
			dCon2[2] = 0L;
			dCon2[3] = 0L;
			
			r.add(dCon1);
			r.add(dCon2);
		} else if ( c.getRelation().equals(TemporalRelation.Meets) ) {
			Long[] dCon1 = new Long[4];
			dCon1[0] = fe;
			dCon1[1] = ts;
			dCon1[2] = 0L;
			dCon1[3] = 0L;
		
			r.add(dCon1);
		} else if ( c.getRelation().equals(TemporalRelation.MetBy) ) {
			Long[] dCon1 = new Long[4];
			dCon1[0] = fs;
			dCon1[1] = te;
			dCon1[2] = 0L;
			dCon1[3] = 0L;
		
			r.add(dCon1);
		} else if ( c.getRelation().equals(TemporalRelation.Starts) ) {
			Long[] dCon1 = new Long[4];
			dCon1[0] = fs;
			dCon1[1] = ts;
			dCon1[2] = 0L;
			dCon1[3] = 0L;
			
			Long[] dCon2 = new Long[4];
			dCon2[0] = fe;
			dCon2[1] = te;
			dCon2[2] = Math.min(INF, c.getBound(0).getLower());
			dCon2[3] = Math.min(INF, c.getBound(0).getUpper());
		
			r.add(dCon1);
			r.add(dCon2);
		} else if ( c.getRelation().equals(TemporalRelation.StartedBy) ) {
			Long[] dCon1 = new Long[4];
			dCon1[0] = fs;
			dCon1[1] = ts;
			dCon1[2] = 0L;
			dCon1[3] = 0L;
			
			Long[] dCon2 = new Long[4];
			dCon2[0] = te;
			dCon2[1] = fe;
			dCon2[2] = Math.min(INF, c.getBound(0).getLower());
			dCon2[3] = Math.min(INF, c.getBound(0).getUpper());
		
			r.add(dCon1);
			r.add(dCon2);
		} else if ( c.getRelation().equals(TemporalRelation.During) || c.getRelation().equals(TemporalRelation.DuringOrEquals) ) {
			Long[] dCon1 = new Long[4];
			dCon1[0] = ts;
			dCon1[1] = fs;
			dCon1[2] = Math.min(INF, c.getBound(0).getLower());
			dCon1[3] = Math.min(INF, c.getBound(0).getUpper());
			
			Long[] dCon2 = new Long[4];
			dCon2[0] = fe;
			dCon2[1] = te;
			dCon2[2] = Math.min(INF, c.getBound(1).getLower());
			dCon2[3] = Math.min(INF, c.getBound(1).getUpper());
		
			r.add(dCon1);
			r.add(dCon2);
		} else if ( c.getRelation().equals(TemporalRelation.Contains) ) {
			Long[] dCon1 = new Long[4];
			dCon1[0] = fs;
			dCon1[1] = ts;
			dCon1[2] = Math.min(INF, c.getBound(0).getLower());
			dCon1[3] = Math.min(INF, c.getBound(0).getUpper());
			
			Long[] dCon2 = new Long[4];
			dCon2[0] = te;
			dCon2[1] = fe;
			dCon2[2] = c.getBound(1).getLower();
			dCon2[3] = c.getBound(1).getUpper();
		
			r.add(dCon1);
			r.add(dCon2);
		} else if ( c.getRelation().equals(TemporalRelation.Finishes) ) {
			Long[] dCon1 = new Long[4];
			dCon1[0] = fe;
			dCon1[1] = te;
			dCon1[2] = 0L;
			dCon1[3] = 0L;
			
			Long[] dCon2 = new Long[4];
			dCon2[0] = ts;
			dCon2[1] = fs;
			dCon2[2] = Math.min(INF, c.getBound(0).getLower());
			dCon2[3] = Math.min(INF, c.getBound(0).getUpper());
		
			r.add(dCon1);
			r.add(dCon2);
		} else if ( c.getRelation().equals(TemporalRelation.FinishedBy) ) {
			Long[] dCon1 = new Long[4];
			dCon1[0] = fe;
			dCon1[1] = te;
			dCon1[2] = 0L;
			dCon1[3] = 0L;
			
			Long[] dCon2 = new Long[4];
			dCon2[0] = fs;
			dCon2[1] = ts;
			dCon2[2] = Math.min(INF, c.getBound(0).getLower());
			dCon2[3] = Math.min(INF, c.getBound(0).getUpper());
		
			r.add(dCon1);
			r.add(dCon2);
		} else if ( c.getRelation().equals(TemporalRelation.Overlaps) ) {
			Long[] dCon1 = new Long[4];
			dCon1[0] = fs;
			dCon1[1] = ts;
			dCon1[2] = 1L;
			dCon1[3] = H;
			
			Long[] dCon2 = new Long[4];
			dCon2[0] = fe;
			dCon2[1] = te;
			dCon2[2] = 1L;
			dCon2[3] = H;
			
			Long[] dCon3 = new Long[4];
			dCon3[0] = ts;
			dCon3[1] = fe;
			dCon3[2] = Math.min(INF, c.getBound(0).getLower());
			dCon3[3] = Math.min(INF, c.getBound(0).getUpper());
		
			r.add(dCon1);
			r.add(dCon2);
			r.add(dCon3);
		} else if ( c.getRelation().equals(TemporalRelation.OverlappedBy) || c.getRelation().equals(TemporalRelation.MetByOrOverlappedBy) ) {
			Long[] dCon1 = new Long[4];
			dCon1[0] = ts;
			dCon1[1] = fs;
			dCon1[2] = 1L;
			dCon1[3] = H;
			
			Long[] dCon2 = new Long[4];
			dCon2[0] = te;
			dCon2[1] = fe;
			dCon2[2] = 1L;
			dCon2[3] = H;
			
			Long[] dCon3 = new Long[4];
			dCon3[0] = fs;
			dCon3[1] = te;
			dCon3[2] = Math.min(INF, c.getBound(0).getLower());
			dCon3[3] = Math.min(INF, c.getBound(0).getUpper());
		
			r.add(dCon1);
			r.add(dCon2);
			r.add(dCon3);
		}	
		return r;
	}

	private List<Integer> getFirstNewIndex( int n, List<Statement> S, List<AllenConstraint> AC, List<SimpleDistanceConstraint> SDC ) {		
		List<Integer> r = new ArrayList<Integer>();

//		List<Statement> S2 = S;
		
		if ( addedStatementsHistory.get(n).size() > S.size() ) {
			return null;
		}
		
		for ( int i = 0 ; i < addedStatementsHistory.get(n).size() ; i++ ) {
			if ( !addedStatementsHistory.get(n).get(i).equals(S.get(i)) ) {
				return null;
			}
		}
		
//		List<AllenConstraint> AC2 = AC;
		
		if ( addedAllenConstraintsHistory.get(n).size() > AC.size() ) {
			return null;
		}
		
		for ( int i = 0 ; i < addedAllenConstraintsHistory.get(n).size() ; i++ ) {
			if ( !addedAllenConstraintsHistory.get(n).get(i).equals(AC.get(i)) ) {
				return null;
			}
		}
		
//		List<SimpleDistanceConstraint> SDC2 = SDC;
		
		if ( addedSimpleDistanceConstraintsHistory.get(n).size() > SDC.size() ) {
			return null;
		}
		
		for ( int i = 0 ; i < addedSimpleDistanceConstraintsHistory.get(n).size() ; i++ ) {
			if ( !addedSimpleDistanceConstraintsHistory.get(n).get(i).equals(SDC.get(i)) ) {
				return null;
			}
		}
		
		
		r.add(addedStatementsHistory.get(n).size());
		r.add(addedAllenConstraintsHistory.get(n).size());
		r.add(addedSimpleDistanceConstraintsHistory.get(n).size());
	
		
//		StopWatch.stop("isContainedIn");
		return r;
	}
	
	private void bookmark() {
		long[][] distanceCopy = new long[d.length][d.length];
		Map<Term,Integer> tpStartCopy = new HashMap<Term, Integer>();
		List<Statement> addedStatementsCopy = new ArrayList<Statement>();
		List<AllenConstraint> addedAllenConstraintsCopy = new ArrayList<AllenConstraint>();
		List<SimpleDistanceConstraint> addedSimpleDistanceConstraintsCopy = new ArrayList<SimpleDistanceConstraint>();
		
		for ( int i = 0 ; i < d.length ; i++ ) {
			for ( int j = 0 ; j < d.length ;j++ ) {
				distanceCopy[i][j] = d[i][j];
			}
		}
		tpStartCopy.putAll(tpStart);
		addedStatementsCopy.addAll(addedStatements);
		addedAllenConstraintsCopy.addAll(addedAllenConstraints);
		addedSimpleDistanceConstraintsCopy.addAll(addedSimpleDistanceConstraints);
		
		this.dHistory.add(distanceCopy);
		this.tpStartHistory.add(tpStartCopy);
		this.addedStatementsHistory.add(addedStatementsCopy);
		this.addedAllenConstraintsHistory.add(addedAllenConstraintsCopy);
		this.addedSimpleDistanceConstraintsHistory.add(addedSimpleDistanceConstraintsCopy);
		
		if ( this.dHistory.size() > maxHistorySize ) {
			this.dHistory.remove(0);
			this.tpStartHistory.remove(0);
			this.addedStatementsHistory.remove(0);
			this.addedAllenConstraintsHistory.remove(0);
			this.addedSimpleDistanceConstraintsHistory.remove(0);
		}
	}
	
	private void revert(int n) {
		this.d = dHistory.get(n);
		this.tpStart = tpStartHistory.get(n);
		this.addedStatements = this.addedStatementsHistory.get(n);
		this.addedAllenConstraints = this.addedAllenConstraintsHistory.get(n);
		this.addedSimpleDistanceConstraints = this.addedSimpleDistanceConstraintsHistory.get(n);
		
		int i;
		while ( this.dHistory.size() > n ) {
			i = dHistory.size()-1;
			this.dHistory.remove(i);
			this.tpStartHistory.remove(i);
			this.addedStatementsHistory.remove(i);
			this.addedAllenConstraintsHistory.remove(i);
			this.addedSimpleDistanceConstraintsHistory.remove(i);
		}
	}

	/**
	 * Set a name for this solver (used for logging)
	 * @param name the name
	 */
	public void setName( String name ) {
		this.name = name;
	}
	
	/**
	 * Get earliest start-time (EST) of an interval
	 * @param interval 
	 * @return earliest start-time
	 */
	public long getEST(Term interval) {
		int st = tpStart.get(interval);
		return -d[st][tpOrigin];
	}

	/**
	 * Get latest start-time (LST) of an interval
	 * @param interval 
	 * @return latest start-time
	 */
	public long getLST(Term interval) {
		int st = tpStart.get(interval);
		return d[tpOrigin][st];
	}

	/**
	 * Get earliest end-time (EET) of an interval
	 * @param interval 
	 * @return earliest end-time
	 */
	public long getEET(Term interval) {
		int et = tpStart.get(interval)+1;
		return -d[et][tpOrigin];
	}

	/**
	 * Get latest end-time (LET) of an interval
	 * @param interval 
	 * @return latest end-time
	 */
	public long getLET(Term interval) {
		int et = tpStart.get(interval)+1;
		return d[tpOrigin][et];
	}
	
	/**
	 * Check if the last run of isConsistent 
	 * found new constraints to propagate.
	 * @return <code>true</code> if new constraints were added, <code>false</code> otherwise
	 */
	public boolean changedMatrix() {
		return propagationRequired || usedRevert;
	}

	
	/**
	 * Get array with earliest and latest start and end times of an interval (EST, LST, EET, LET).
	 * @param interval
	 * @return array containing earliest and latest start and end times
	 */
	public long[] getBoundsArray(Term interval) {
		int st = tpStart.get(interval);
		int et = st+1;
		long[] r = new long[4];
		
		r[0] = -d[st][tpOrigin];
		r[1] = d[tpOrigin][st];
		r[2] = -d[et][tpOrigin];
		r[3] = d[tpOrigin][et];
		
		return r;
	}

	/**
	 * Get temporal constraint representing earliest and latest start and end times of an interval (EST, LST, EET, LET).
	 * @param interval
	 * @return constraint enforcing earliest and latest start and end times
	 */
	public AllenConstraint getBoundsConstraint(Term interval) {
		long[] bounds = getBoundsArray(interval);
		return new AllenConstraint(interval, TemporalRelation.At, new Interval(bounds[0],bounds[1]), new Interval(bounds[2],bounds[3]));
	}
	
	/**
	 * Get earliest time considered by temporal reasoner
	 * @return the origin
	 */
	public long getOrigin() {
		return O;
	}
	/**
	 * Get latest time considered by temporal reasoner
	 * @return the temporal horizon
	 */
	public long getHorizon() {
		return H;
	}
		
	/**
	 * Get the number of backups of the distance matrix that have been created.
	 * @return history size
	 */
	public int getHistorySize() {
		return dHistory.size();
	}

	
	/**
	 * Tests if an intersection is possible in the propagated temporal network
	 * 
	 * @param pi possible intersection query
	 * @return <code>true</code> if all intervals in <code>pi</code> have an earliest time intersection, <code>false</code> otherwise
	 */
	public boolean possibleIntersection(PossibleIntersection pi) {
		long minEET = Long.MAX_VALUE;
		long maxEST = 0;
		
		for ( Term interval : pi.getIntervals() ) {
			long EST, EET;
			if ( this.tpStart.containsKey(interval) ) {
				EST = this.getEST(interval);
				EET = this.getEET(interval);
			} else {
				EST = 0;
				EET = Long.MAX_VALUE;
			}
				
			if ( EST > maxEST ) {
				maxEST = EST;
			}
			if ( EET < minEET ) {
				minEET = EET;
			}
		}
		
//		System.out.println(maxEST + " to " + minEET);
		
		if ( !(minEET > maxEST) ) {
			return false;
		} else {
			return true;
		}
	}

	
	/**
	 * Get the makespan of a propagated CDB (maximum of all earliest end-times)
	 * optionally ignoring a set of statements.
	 * @param cDB 
	 * @param ignoredStatements
	 * @return maximum of all considered earliest end-times
	 */
	public long getMakespan(ConstraintDatabase cDB, ArrayList<Statement> ignoredStatements) {
		long maxEET = 0;
		long EET;
		for ( Statement s : cDB.get(Statement.class) ) {
			if ( !ignoredStatements.contains(s) ) {
				EET = this.getEET(s.getKey());
				if (EET > maxEET) {
					maxEET = EET;
				}
			}
		}
		return maxEET;
	}

	
	/**
	 * Calculate and return temporal rigidity.
	 * @return the rigidity
	 */
	public double getRigidity() {
		int numTimePoints = this.d.length;
		double rigidity[] = new double[numTimePoints];
		for (int i = 0; i < numTimePoints; i++) {
			long lb = -d[i][tpOrigin]; 
			long ub = d[tpOrigin][i]; 		
			rigidity[i] = ((double)1 / ((double)(1 + (ub - lb))));

		}
		double sigma = 0;
		for (int i = 0; i < numTimePoints; i++) {
			sigma += Math.pow(rigidity[i], 2.0);
		}			

		return Math.sqrt(sigma * ((double)2/(numTimePoints * (numTimePoints + 1))));
	}
	
	/**
	 * How many backups are taken before the earliest ones are deleted.
	 * @param max maximum size of distance matrix history
	 */
	public void setMaxHistorySize( int max ) {
		this.maxHistorySize = max;
	}

	/**
	 * Decide if stop-watch should be used
	 * @param keepTimes
	 */
	public void setKeepTimes(boolean keepTimes) {
		this.keepTimes = keepTimes;	
	}

	/**
	 * Decide if statistics should be kept
	 * @param keepStats
	 */
	public void setKeepStatistics(boolean keepStats) {
		this.keepStats = keepStats;
	}
		
	/**
	 * Set this solver's verbose mode.
	 * @param verbose
	 */
	public void setVerbose( boolean verbose ){
		this.verbose = verbose;
	}
	
	/**
	 * Get list of statements whose intervals include time-point <code>t</code>.
	 * In case this is true for multiple assignments, the earlier
	 * one is preferred.
	 *  
	 * @param t Time point of the snapshot
	 * @return list of statements whose values hold at time <code>t</code>
	 */
	public ArrayList<Statement> getTemporalSnapshot( long t ) {
		ArrayList<Statement> r = new ArrayList<Statement>();
		
		Map<Term,Long> estLookup = new HashMap<Term, Long>();
		Map<Term,Statement> choiceLookup = new HashMap<Term, Statement>();
		
		for ( Statement s : this.addedStatements ) {
			
			long currentMinEST;
			Long currentMinESTfromLookup = estLookup.get(s.getVariable());
			
			if ( currentMinESTfromLookup != null ) {
				currentMinEST = currentMinESTfromLookup;
			} else {
				currentMinEST = Long.MAX_VALUE;
			}
						
			long est = this.getEST(s.getKey());
			long eet = this.getEET(s.getKey());
				
			if ( t >= est && t < eet && est < currentMinEST ) {
				estLookup.put(s.getVariable(), est);
				choiceLookup.put(s.getVariable(), s);
			}
			
		}
		
		r.addAll(choiceLookup.values());

		return r;
	}
	
	/**
	 * Similar to getTemporalSnapshot(t) but will consider variables whose intervals 
	 * do not contain t but start after it (i.e., the future).
	 * @param t Time point of the snapshot
	 * @return list of all statements whose intervals include <code>t</code> or start after <code>t</code>
	 */
	public ArrayList<Statement> getTemporalSnapshotWithFuture( long t ) {
		ArrayList<Statement> r = new ArrayList<Statement>();
		
		Map<Term,Long> estLookup = new HashMap<Term, Long>();
		Map<Term,Statement> choiceLookup = new HashMap<Term, Statement>();
		
		for ( Statement s : this.addedStatements ) {
			
			long currentMinEST;
			Long currentMinESTfromLookup = estLookup.get(s.getVariable());
			
			if ( currentMinESTfromLookup != null ) {
				currentMinEST = currentMinESTfromLookup;
			} else {
				currentMinEST = Long.MAX_VALUE;
			}
						
			long est = this.getEST(s.getKey());
				
			if ( t >= est && est < currentMinEST ) {
				estLookup.put(s.getVariable(), est);
				choiceLookup.put(s.getVariable(), s);
			}
			
		}
		
		r.addAll(choiceLookup.values());

		return r;
	}
	
}
