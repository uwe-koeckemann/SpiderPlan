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
package org.spiderplan.temporal.stpSolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.expressions.Expression;
import org.spiderplan.representation.expressions.Statement;
import org.spiderplan.representation.expressions.ExpressionTypes.TemporalRelation;
import org.spiderplan.representation.expressions.temporal.AllenConstraint;
import org.spiderplan.representation.expressions.temporal.Interval;
import org.spiderplan.representation.expressions.temporal.PossibleIntersection;
import org.spiderplan.representation.expressions.temporal.TemporalIntervalLookup;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.representation.types.TypeManager;
import org.spiderplan.temporal.TemporalReasoningInterface;
import org.spiderplan.tools.logging.Logger;
import org.spiderplan.tools.profiler.Profiler;
import org.spiderplan.tools.statistics.Statistics;
import org.spiderplan.tools.stopWatch.StopWatch;

public class IncrementalSTPSolverArrayList implements TemporalReasoningInterface {
	
	private String name = "incSTP";
	private boolean verbose = false;

	private List<List<Long>> distance;
	private Map<Term,Integer> tpStart;
	private List<Statement> addedStatements;
	private List<AllenConstraint> addedAllenConstraints;
	
	private int maxHistorySize = 100;
	private List<List<List<Long>>> distanceHistory;
	private List<Map<Term,Integer>> tpStartHistory;
	private List<List<Statement>> addedStatementsHistory;
	private List<List<AllenConstraint>> addedAllenConstraintsHistory;
	
	private long O = 0L;
	private long H = 10;
	private long INF = Long.MAX_VALUE/2-2;
	
	private int tpOrigin = 0;
		
	private boolean needFromScratch = false;
	private boolean keepTimes = false;
	private boolean keepStats = false;
	private boolean propagationRequired = false;
	
	private boolean useLinearRevert = true;
		
	public IncrementalSTPSolverArrayList( long origin, long horizon ) {
		this.O = origin;
		this.H = horizon;
		reset();
	}
	

	
	@Override
	public boolean isConsistent( ConstraintDatabase cDB, TypeManager tM ) {
		propagationRequired = false;
		
		System.out.println(useLinearRevert);
		
		List<Statement> newStatements;
		List<AllenConstraint> newAllenConstraints;

		int revertToIndex;
		if ( useLinearRevert ) {
			if ( keepTimes ) StopWatch.start("[incSTP] 1) Finding revert level (linear)");
			
			revertToIndex = distanceHistory.size()-1;
			List<Integer> beginIndex = null;
			
			if ( !distanceHistory.isEmpty() ) {
				while ( revertToIndex >= 0) {
					beginIndex = this.getFirstNewIndex(revertToIndex, cDB);
					if ( beginIndex != null ) {
						break;
					}
					revertToIndex--;
				}
			}
			if ( keepTimes ) StopWatch.stop("[incSTP] 1) Finding revert level (linear)");
			
			if ( revertToIndex == -1 ) { // history does not contain suitable reverting point
				Profiler.probe(0);			
				if ( verbose ) Logger.msg(this.name, "Propagating from scratch...", 2);
				
				needFromScratch = true;
				beginIndex = new ArrayList<Integer>();
				beginIndex.add(0);
				beginIndex.add(0);
			} else {
				needFromScratch = false;
				
				if ( revertToIndex < distanceHistory.size()-1 ) {
					if ( verbose ) Logger.msg(this.name, "Reverting to " + revertToIndex + "/" + (distanceHistory.size()-1), 2);
					if ( verbose ) Logger.msg(this.name, "Begin index: " + beginIndex.toString(), 2);
					
					if ( keepTimes ) StopWatch.start("[incSTP] 1-a) Reverting");
					this.revert(revertToIndex);
					this.bookmark();
					if ( keepTimes ) StopWatch.stop("[incSTP] 1-a) Reverting");
				}
				if ( beginIndex.get(0) == addedStatements.size() && beginIndex.get(1) == addedAllenConstraints.size() ) {
					propagationRequired = false;
				} 
			}
			
			newStatements = cDB.get(Statement.class).subList(beginIndex.get(0), cDB.get(Statement.class).size());
			newAllenConstraints = cDB.get(AllenConstraint.class).subList(beginIndex.get(1), cDB.get(AllenConstraint.class).size());
		} else {
			if ( keepTimes ) StopWatch.start("[incSTP] 1) Finding revert level (quadratic)");
			
			List<Statement> cdbStatements = cDB.get(Statement.class);				
			List<AllenConstraint> cdbAllenConstraints = cDB.get(AllenConstraint.class);
			
			revertToIndex = distanceHistory.size()-1;
	
			if ( !distanceHistory.isEmpty() ) {
//				System.out.println("Trying to find revert level...");
				while ( revertToIndex >= 0 ) {
					List<Statement> statementsFromHistory = addedStatementsHistory.get(revertToIndex);
					List<AllenConstraint> allenConstraintsFromHistory = addedAllenConstraintsHistory.get(revertToIndex);
					
					boolean good = cdbStatements.containsAll(statementsFromHistory);
					good &= cdbAllenConstraints.containsAll(allenConstraintsFromHistory);
					
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
				
				newStatements.addAll(cdbStatements);
				newAllenConstraints.addAll(cdbAllenConstraints);	
			} else {
				needFromScratch = false;
				
				if ( revertToIndex < distanceHistory.size()-1 ) {
					if ( verbose ) Logger.msg(this.name, "Reverting to " + revertToIndex + "/" + (distanceHistory.size()-1), 2);
					if ( keepTimes ) StopWatch.start("[incSTP] 1-a) Reverting");
					this.revert(revertToIndex);
					this.bookmark();
									
					if ( keepTimes ) StopWatch.stop("[incSTP] 1-a) Reverting");
				}
				if ( cdbStatements.size() == addedStatementsHistory.get(revertToIndex).size() && cdbAllenConstraints.size() == addedAllenConstraintsHistory.get(revertToIndex).size() ) {
					// nothing new
					propagationRequired = false;
					newStatements = new ArrayList<Statement>();
					newAllenConstraints = new ArrayList<AllenConstraint>();
				} else {
					// only propagate new constraints
					newStatements = new ArrayList<Statement>();
					newAllenConstraints = new ArrayList<AllenConstraint>();
					
					newStatements.addAll(cdbStatements);
					newAllenConstraints.addAll(cdbAllenConstraints);
					
					newStatements.removeAll(addedStatementsHistory.get(revertToIndex));
					newAllenConstraints.removeAll(addedAllenConstraintsHistory.get(revertToIndex));
				}
			}
		}
		
		System.out.println("====================================================");
//		System.out.println(addedStatements);
//		System.out.println(addedAllenConstraints);
		
//		if ( !distanceHistory.isEmpty() ) {
//			System.out.println(addedStatementsHistory.get(revertToIndex));
//			System.out.println(addedAllenConstraintsHistory.get(revertToIndex));
//		}
		
		
//		System.out.println("From scratch? " + needFromScratch);
//		System.out.println("Propagation required? " + propagationRequired);
		System.out.println("Best revert: " + (distanceHistory.size()-1));
		System.out.println("Reverted to " + revertToIndex);
//		
		System.out.println("newStatements " + newStatements.size());
		System.out.println("newAllenConstraints " + newAllenConstraints.size());
		
		if ( needFromScratch ) {
			if ( keepStats ) Statistics.increment("[incSTP] #FromScratch");
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
			
			if ( !addedConstraints.isEmpty() ) {
				propagationRequired = true;
			} 
//		}
		
		if ( keepTimes ) StopWatch.start("[incSTP] Propagation");
		boolean isConsistent = true;
//		if ( propagationRequired ) {
			for ( Long[] con : addedConstraints ) {
				isConsistent &= incrementalDistanceMatrixComputation(con[0].intValue(), con[1].intValue(), con[2], con[3]);
				if ( !isConsistent ) {
					break;
				}
			}
//		}
		if ( keepTimes ) StopWatch.stop("[incSTP] Propagation");
		
		if ( isConsistent && propagationRequired ) {
			this.bookmark();
		}
		return isConsistent;
	}	
	
	
	private List<Long[]> setupDistanceMatrix( List<Statement> S ) {
		List<Long[]> r = new Vector<Long[]>();
		
		for ( int i = 0 ; i < S.size() ; i++ ) {
			Statement s = S.get(i);
			this.addedStatements.add(s);
//			if ( !tpStart.containsKey(s.getKey()) ) {
				int startTP = distance.size();
				int endTP = startTP+1;
				
				tpStart.put(s.getKey(), startTP);
				
				distance.add(new Vector<Long>(distance.size()+2));
				distance.add(new Vector<Long>(distance.size()+2));
				
				distance.get(0).add( H);
				distance.get(1).add( 0L);
				distance.get(0).add( H);
				distance.get(1).add( 0L);
				
				distance.get(startTP).add(0L); //0L
				distance.get(startTP).add(H);
				distance.get(endTP).add(0L); //0L
				distance.get(endTP).add(H);
				
				for ( int j = 2 ; j < distance.size()-2 ; j++ ) {
					distance.get(startTP).add(H);
					distance.get(j).add(H);
					distance.get(endTP).add(H);
					distance.get(j).add(H);
				}
				
				distance.get(startTP).add(0L);
				distance.get(startTP).add(H);
				distance.get(endTP).add(H);
				distance.get(endTP).add(0L);
				
				List<Long[]> cons = new ArrayList<Long[]>();
				
				{
					Long[] dConMinDur = new Long[4];
					dConMinDur[0] = (long) startTP;
					dConMinDur[1] = (long) endTP;
					dConMinDur[2] = 1L;
					dConMinDur[3] = H;
					r.add(dConMinDur);
					cons.add(dConMinDur);
				}

//				sdMap.put(s.getKey(), cons);
//			}
		}
		return r;
	}

	private void reset() {
		distance  = new ArrayList<List<Long>>();
		tpStart = new HashMap<Term, Integer>();

		addedStatements = new ArrayList<Statement>();
		addedAllenConstraints = new ArrayList<AllenConstraint>();
		
		distanceHistory = new ArrayList<List<List<Long>>>();
		tpStartHistory = new ArrayList<Map<Term,Integer>>();
		addedStatementsHistory = new ArrayList<List<Statement>>();
		addedAllenConstraintsHistory = new ArrayList<List<AllenConstraint>>();
		
		distance.add(new Vector<Long>(2));
		distance.add(new Vector<Long>(2));
		
		distance.get(0).add(0L);
		distance.get(0).add(H);
		distance.get(1).add(-H);
		distance.get(1).add(0L);
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
						throw new IllegalStateException("Interval " + ac.getFrom() + " of temporal constraint " + ac + " does not have corresponding statement.");
					}
					if ( ts == null ) {
						throw new IllegalStateException("Interval " + ac.getTo() + " of temporal constraint " + ac + " does not have corresponding statement.");
					}
					int fe = fs+1;
					int te = ts+1;
					addedConstraints.addAll(this.binaryAllen2SimpleDistance(ac, fs, fe, ts, te));
				}
							
//				this.acdMap.put(ac, addedConstraints);
				r.addAll(addedConstraints);
				
//				for ( Long[] sdc : addedConstraints ) {
//					System.out.println(Arrays.toString(sdc));
//				}
//			}
		}
		return r;
	}
	
	
	
	
	public String getDistanceMatrixString() {
		StringBuilder sb = new StringBuilder();
		
		for ( List<Long> r : this.distance ) {
			sb.append(r.toString());
			sb.append("\n");
		}
		return sb.toString();
	}
	
	public TemporalIntervalLookup getPropagatedTemporalIntervals() {
		Map<Term,Long[]> bounds = new HashMap<Term, Long[]>();
		
		for ( Statement s : addedStatements ) {
			Long[] bound = new Long[4];
			bound[0] = this.getEST(s.getKey());
			bound[1] = this.getLST(s.getKey());
			bound[2] = this.getEET(s.getKey());
			bound[3] = this.getLET(s.getKey());
			bounds.put(s.getKey(), bound);
		}
		return new TemporalIntervalLookup(bounds);
	}
	
	private long sum(long a, long b) {
		if ( a+b > H ) return H;
		return a+b;
	}
		
	private boolean incrementalDistanceMatrixComputation( int from, int to, long min, long max ) {
		if (distance.get(to).get(from) != INF && sum( max,distance.get(to).get(from)) < 0) return false;
		if (distance.get(from).get(to) != INF && sum(-min,distance.get(from).get(to)) < 0) return false;
				
		long sum1;
		long sum2;
		long sum3;
		long sum4;
		long temp;
		
		for (int u = 0 ; u < distance.size() ; u++) {
			for (int v = 0 ; v < distance.size() ; v++) {
				sum1 = sum(distance.get(u).get(to),-min);
				sum2 = sum(sum1,distance.get(from).get(v));
				sum3 = sum(distance.get(u).get(from),max);
				sum4 = sum(sum3,distance.get(to).get(v));
				temp = Math.min(sum2,sum4);
										
				if (distance.get(u).get(v) > temp) {			
					distance.get(u).set(v, temp);
				
					if (u == v && distance.get(u).get(v) != 0L) {
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

	private List<Integer> getFirstNewIndex( int n, ConstraintDatabase cdb ) {		
		List<Integer> r = new ArrayList<Integer>();

		List<Statement> S2 = cdb.get(Statement.class);
		
		if ( addedStatementsHistory.get(n).size() > S2.size() ) {
			return null;
		}
		
		for ( int i = 0 ; i < addedStatementsHistory.get(n).size() ; i++ ) {
			if ( !addedStatementsHistory.get(n).get(i).equals(S2.get(i)) ) {
				return null;
			}
		}
		
		List<AllenConstraint> AC2 = cdb.get(AllenConstraint.class);
		
		if ( addedAllenConstraintsHistory.get(n).size() > AC2.size() ) {
			return null;
		}
		
		for ( int i = 0 ; i < addedAllenConstraintsHistory.get(n).size() ; i++ ) {
			if ( !addedAllenConstraintsHistory.get(n).get(i).equals(AC2.get(i)) ) {
				return null;
			}
		}
		
		r.add(addedStatementsHistory.get(n).size());
		r.add(addedAllenConstraintsHistory.get(n).size());
		
//		StopWatch.stop("isContainedIn");
		return r;
	}
	
	private void bookmark() {
		List<List<Long>> distanceCopy = new ArrayList<List<Long>>();
		Map<Term,Integer> tpStartCopy = new HashMap<Term, Integer>();
		List<Statement> addedStatementsCopy = new ArrayList<Statement>();
		List<AllenConstraint> addedAllenConstraintsCopy = new ArrayList<AllenConstraint>();
		
		for ( List<Long> l1 : distance ) {
			List<Long> l2 = new ArrayList<Long>(); 
			l2.addAll(l1);
			distanceCopy.add(l2);
		}
		tpStartCopy.putAll(tpStart);
		addedStatementsCopy.addAll(addedStatements);
		addedAllenConstraintsCopy.addAll(addedAllenConstraints);
		
		this.distanceHistory.add(distanceCopy);
		this.tpStartHistory.add(tpStartCopy);
		this.addedStatementsHistory.add(addedStatementsCopy);
		this.addedAllenConstraintsHistory.add(addedAllenConstraintsCopy);
		
		if ( this.distanceHistory.size() > maxHistorySize ) {
			this.distanceHistory.remove(0);
			this.tpStartHistory.remove(0);
			this.addedStatementsHistory.remove(0);
			this.addedAllenConstraintsHistory.remove(0);
		}
	}
	
	private void revert(int n) {
		this.distance = distanceHistory.get(n);
		this.tpStart = tpStartHistory.get(n);
		this.addedStatements = this.addedStatementsHistory.get(n);
		this.addedAllenConstraints = this.addedAllenConstraintsHistory.get(n);
		
		int i;
		while ( this.distanceHistory.size() > n ) {
			i = distanceHistory.size()-1;
			this.distanceHistory.remove(i);
			this.tpStartHistory.remove(i);
			this.addedStatementsHistory.remove(i);
			this.addedAllenConstraintsHistory.remove(i);
		}
	}

	public void setName( String name ) {
		this.name = name;
	}
	
	@Override
	public boolean isTemporalConsistent() {
		return !this.needFromScratch;
	}

	@Override
	public boolean isResourceConsistent() {
		throw new IllegalAccessError("This is not implemented...");
	}

	@Override
	public ArrayList<Expression> getSchedulingDecisions() {
		throw new IllegalAccessError("This is not implemented...");
	}

	@Override
	public boolean hasInterval(Term k) {
		return tpStart.containsKey(k);
	}

	@Override
	public long getEST(Term interval) {
		int st = tpStart.get(interval);
		return -distance.get(st).get(tpOrigin);
	}

	@Override
	public long getLST(Term interval) {
		int st = tpStart.get(interval);
		return distance.get(tpOrigin).get(st);
	}

	@Override
	public long getEET(Term interval) {
		int et = tpStart.get(interval)+1;
		return -distance.get(et).get(tpOrigin);
	}

	@Override
	public long getLET(Term interval) {
		int et = tpStart.get(interval)+1;
		return distance.get(tpOrigin).get(et);
	}

	@Override
	public long[] getBoundsArray(Term interval) {
		int st = tpStart.get(interval);
		int et = st+1;
		long[] r = new long[4];
		
		r[0] = -distance.get(st).get(tpOrigin);
		r[1] = distance.get(tpOrigin).get(st);
		r[2] = -distance.get(et).get(tpOrigin);
		r[4] = distance.get(tpOrigin).get(et);
		
		return r;
	}

	@Override
	public AllenConstraint getBoundsConstraint(Term interval) {
		long[] bounds = getBoundsArray(interval);
		return new AllenConstraint(interval, TemporalRelation.At, new Interval(bounds[0],bounds[1]), new Interval(bounds[2],bounds[3]));
	}
	
	public long getOrigin() {
		return O;
	}
	public long getHorizon() {
		return H;
	}
	
	public boolean usedPropagation() {
		return propagationRequired;
	}
	
	public int getHistorySize() {
		return distanceHistory.size();
	}

	@Override
	public boolean possibleIntersection(PossibleIntersection pi) {
		long minEET = Long.MAX_VALUE;
		long maxEST = 0;
		
		for ( Term interval : pi.getIntervals() ) {
			long EST = this.getEST(interval);
			long EET = this.getEET(interval);
				
			if ( EST > maxEST ) {
				maxEST = EST;
			}
			if ( EET < minEET ) {
				minEET = EET;
			}
		}
		
		if ( !(minEET > maxEST) ) {
			return false;
		} else {
			return true;
		}
	}

	@Override
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

	@Override
	public double getRigidity() {
		int numTimePoints = this.distance.size();
		double rigidity[] = new double[numTimePoints];
		for (int i = 0; i < numTimePoints; i++) {
			long lb = -distance.get(i).get(tpOrigin); 
			long ub = distance.get(tpOrigin).get(i); 		
			rigidity[i] = ((double)1 / ((double)(1 + (ub - lb))));

		}
		double sigma = 0;
		for (int i = 0; i < numTimePoints; i++) {
			sigma += Math.pow(rigidity[i], 2.0);
		}			

		return Math.sqrt(sigma * ((double)2/(numTimePoints * (numTimePoints + 1))));
	}
	
	public void setMaxHistorySize( int max ) {
		this.maxHistorySize = max;
	}

	@Override
	public void setKeepTimes(boolean keepTimes) {
		this.keepTimes = keepTimes;	
	}

	@Override
	public void setKeepStatistics(boolean keepStats) {
		this.keepStats = keepStats;
	}
	
	protected String msg( String s ) {
		return "["+name+"] "+s;
	}
	
	public void setVerbose( boolean verbose ){
		this.verbose = verbose;
	}
}
