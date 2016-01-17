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
package org.spiderplan.scheduling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.spiderplan.modules.STPSolver;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.expressions.Statement;
import org.spiderplan.representation.expressions.ExpressionTypes.TemporalRelation;
import org.spiderplan.representation.expressions.temporal.AllenConstraint;
import org.spiderplan.representation.expressions.temporal.Interval;
import org.spiderplan.representation.expressions.temporal.TemporalIntervalLookup;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.representation.types.TypeManager;
import org.spiderplan.temporal.stpSolver.IncrementalSTPSolver;
import org.spiderplan.tools.Loop;

/**
 * Note: Code based on scheduling code of the metacsp project (http://metacsp.org/) 
 */
public abstract class Scheduler {
	
	public enum PeakCollectionStrategy { BinaryPeakCollection, SamplingPeakCollection };
	
	protected PeakCollectionStrategy strategy = PeakCollectionStrategy.SamplingPeakCollection;
	
	private Atomic resourceVariable;
	private TemporalIntervalLookup tiLookup;
	
	private ConstraintDatabase currentCDB = null;
	
	private List<Statement> usages;
	
	public Scheduler( Atomic resourceVariable ) {
		this.resourceVariable = resourceVariable;
	}
	
	/**
	 * Update the list of statements using the resource.
	 * 
	 * @param cDB
	 */
	public List<AllenConstraint> resolveFlaw( ConstraintDatabase cDB ) {
		currentCDB = cDB;
		this.usages = new ArrayList<Statement>();
		
		this.tiLookup = cDB.get(TemporalIntervalLookup.class).get(0); 
			
		for ( Statement s : cDB.get(Statement.class) ) {
//			if ( s.getVariable().equals(this.resourceVariable) ) { //TODO: 
			if ( s.getVariable().match(this.resourceVariable) != null ) { //TODO:
//				if ( resourceVariable.toString().contains("meetingRoom") ) System.out.println("Considering: " + s);
				if ( !this.usages.contains(s) ) {
					this.usages.add(s);
				}
			}
		}
		
		List<List<Statement>> conflicts;
		if ( this.strategy.equals(PeakCollectionStrategy.BinaryPeakCollection) ) {
			conflicts = this.binaryPeakCollection();
		} else {
			conflicts = this.samplingPeakCollection();
		}
//		if ( resourceVariable.toString().contains("meetingRoom") ) System.out.println("Mine num comflicts: " + conflicts.size());

//		if ( conflicts.size() > 0 ) {
//			System.out.println(this.resourceVariable);
//			System.out.println(conflicts);
//			System.out.println("===============================");
//		}
		
		if ( conflicts.isEmpty() ) {
			return new ArrayList<AllenConstraint>();
		}
		
		List<MCSData> valueOrdering = this.getValueOrdering(conflicts.get(0));
		
		if ( valueOrdering == null ) {
			return null;
		}
		
		Collections.sort(valueOrdering);
		
		List<AllenConstraint> rList = new ArrayList<AllenConstraint>();
		
		for ( MCSData d : valueOrdering ) {
//			System.out.println(d.mcsPcMin + " " + d.mcsActFrom + " " + d.mcsActTo);
			AllenConstraint before = new AllenConstraint(d.mcsActFrom, d.mcsActTo, TemporalRelation.BeforeOrMeets, new Interval(Term.createInteger(0), Term.createConstant("inf")));
			rList.add(before);			
		}
		return rList;
	}
	
	private List<MCSData> getValueOrdering( List<Statement> MCS ) {
				
		List<Term[]> mcslist = new ArrayList<Term[]>();
		
		for (int i = 0; i < MCS.size(); i++) {
			for (int j = i+1; j < MCS.size(); j++) {
				Term[] oneMcs = new Term[2];
				oneMcs[0] = MCS.get(i).getKey();
				oneMcs[1] = MCS.get(j).getKey();
				mcslist.add(oneMcs);
			}
		}
		
		List<MCSData> r = new ArrayList<MCSData>();
		
		for ( Term[] pair : mcslist ) {
			boolean a_works = true;
			boolean b_works = true;
			
			float pc_min = 1.1f;
			float pc_min_bad = 1.1f; 
			
			List<Float> pc_vec = new ArrayList<Float>();
			
			long i_est = tiLookup.getEST(pair[0]);
			long i_lst = tiLookup.getLST(pair[0]);
			long i_eet = tiLookup.getEET(pair[0]);
			long i_let = tiLookup.getLET(pair[0]);
				
			long j_est = tiLookup.getEST(pair[1]);
			long j_lst = tiLookup.getLST(pair[1]);
			long j_eet = tiLookup.getEET(pair[1]);
			long j_let = tiLookup.getLET(pair[1]);
					
			long dmin = j_est - i_let;
			long dmax = j_lst - i_eet;
			float pc = 0.0f;
			
//			System.out.println("dmin " + dmin + " dmax " + dmax);
			
			if ( dmin != dmax ) {
				pc = ((float)(Math.min(dmax, 0) - Math.min(dmin, 0)))/((float)(dmax - dmin));
//				System.out.println("pc " +pc);
				pc_vec.add( pc);
				
				if ( pc < pc_min ) {
					pc_min = pc;
					pc_min_bad = pc_min;
				}
			} else {
				a_works = false;
			}
			
		
			dmin = i_est - j_let;
			dmax = i_lst - j_eet;
			
//			System.out.println("dmin " + dmin + " dmax " + dmax);
			
			if ( dmin != dmax ) {		
				pc = ((float)(Math.min(dmax, 0) - Math.min(dmin, 0)))/((float)(dmax - dmin));
//				System.out.println("pc " + pc);
				
				pc_vec.add(pc);				
				if ( pc < pc_min ) {
					pc_min = pc;
				}
			} else {
				b_works = false;
			}				
			
			float k = 0.0f;
			if ( a_works || b_works ) {
				for( int i = 0; i < pc_vec.size(); i++ ) {
					k += 1.0f/(1.0f + pc_vec.get(i) - pc_min);
				}
				if ( k != 0.0f ) {
					k = 1.0f/k;
				} else {
					k = 1.0f;
				}
			}
						
			if ( a_works )
				r.add(new MCSData(pc_min, pair[0], pair[1], k));
			if ( b_works )
				r.add(new MCSData(pc_min_bad, pair[1], pair[0], k));
		}
		
		if ( !r.isEmpty() )
			return r;
		else 
			return null;
	}
	
	/**
	 * Stolen from meta CSP framework
	 * @return
	 */
	protected List<List<Statement>> samplingPeakCollection() {

		if (usages != null && !usages.isEmpty()) {
			
			Statement[] groundVars = usages.toArray(new Statement[usages.size()]);
			Arrays.sort(groundVars,new IntervalComparator(this.tiLookup));
			
			List<List<Statement>> ret = new ArrayList<List<Statement>>();
			
			HashMap<Statement,List<Statement>> usagesOld = new HashMap<Statement,List<Statement>>();
			
			Vector<Vector<Statement>> overlappingAll = new Vector<Vector<Statement>>();
			
			/**
			 * Check single statement violations
			 */
			for ( Statement s : this.usages ) {
				if ( isConflicting(new Statement[] {s}) ) {
					List<Statement> temp = new ArrayList<Statement>();
					temp.add(s);
					ret.add(temp);
				}
			}
	
			//	groundVars are ordered activities
			for (int i = 0; i < groundVars.length; i++) {
				Vector<Statement> overlapping = new Vector<Statement>();
				overlapping.add(groundVars[i]);
								
				long start = tiLookup.getEST(groundVars[i].getKey());
				long end = tiLookup.getEET(groundVars[i].getKey());
				
				Bounds intersection = new Bounds(start, end);
				// starting from act[i] all the forthcoming activities are evaluated to see if they temporally
				// overlaps with act[i]
				for (int j = 0; j < groundVars.length; j++) {
					if (i != j) {
						start = tiLookup.getEST(groundVars[j].getKey());
						end = tiLookup.getEET(groundVars[j].getKey());

						Bounds nextInterval = new Bounds(start, end);
						Bounds intersectionNew = intersection.intersectStrict(nextInterval);
						// if act[j] overlaps it is added to the temporary (wrt i) set of activities
						if ( intersectionNew != null ) {
							overlapping.add(groundVars[j]);
							// the current set of overlapping activities is evaluated to see if
							// the resource capacity is exceeded
							if (isConflicting(overlapping.toArray(new Statement[overlapping.size()]))) {
								// if it is exceeded the Vector of activities gathered in this iteration is put
								// in a Vector<Vector<Activity>>
								overlappingAll.add(overlapping);
								break;						
							}
							// if they don't exceed the capacity, just the newIntersection is taken into account...
//							else intersection = intersectionNew;
						}
					}
				}
			}
	
			for (Vector<Statement> overlapping : overlappingAll) {
				if (overlapping.size() > 1) {
					Statement first = overlapping.get(0);
					List<Statement> temp = new ArrayList<Statement>();
					for ( Statement s : overlapping ) temp.add(s);
					usagesOld.put(first, temp);
				}
			}
			
			for ( Statement key : usagesOld.keySet() ) {
				if ( usagesOld.get(key).size() > 1) ret.add(usagesOld.get(key));
			}
			
			return ret;
		}
		return new ArrayList<List<Statement>>();		
	}

	protected List<List<Statement>> binaryPeakCollection() {
		if (usages != null && !usages.isEmpty()) {
			List<List<Statement>> ret = new ArrayList<List<Statement>>();

			Statement[] groundVars = usages.toArray(new Statement[usages.size()]);
			for (Statement s : groundVars) {
				if (isConflicting(new Statement[] {s})) {
					List<Statement> temp = new ArrayList<Statement>();
					temp.add(s);
					ret.add(temp);
				}
			}
			if (!ret.isEmpty()) {
				return ret;
			}
		
			for (int i = 0; i < groundVars.length-1; i++) {

				long i_min = tiLookup.getEST(groundVars[i].getKey());
				long i_max = tiLookup.getEET(groundVars[i].getKey());
				Bounds b_i = null;
				
				try {
					b_i = new Bounds(i_min, i_max);	
				} catch ( Exception e ) {
					IncrementalSTPSolver stpSolver = new IncrementalSTPSolver(0, 1000000);
					boolean r = stpSolver.isConsistent(currentCDB, new TypeManager());
					
					System.out.println("i: " + groundVars[i]);
					System.out.println(tiLookup.getEST(groundVars[i].getKey()));
					System.out.println(tiLookup.getEET(groundVars[i].getKey()));
					System.out.println(r);
					
//					System.out.println(stpSolver.getPropagatedTemporalIntervals());
					
					Loop.start();
				}
				
						
				for (int j = i+1; j < groundVars.length; j++) {
					long j_min = tiLookup.getEST(groundVars[j].getKey());
					long j_max = tiLookup.getEET(groundVars[j].getKey());
					
					Bounds b_j = new Bounds(j_min, j_max);
					
//					System.out.println("==============");
//					System.out.println(b_i);
//					System.out.println(b_j);
//					System.out.println(b_i.intersectStrict(b_j));
//					
//					System.out.println(isConflicting(new Statement[] {groundVars[i], groundVars[j]}));
					
					if ( b_i.intersectStrict(b_j) != null && isConflicting(new Statement[] {groundVars[i], groundVars[j]})) {
						List<Statement> conflict = new ArrayList<Statement>();
						conflict.add(groundVars[i]);
						conflict.add(groundVars[j]);
						ret.add(conflict);
					}
				}
			}
			if (!ret.isEmpty()) {
				return ret;			
			}
		}
		return new ArrayList<List<Statement>>();
	}
	
	public final boolean intersectStrict( long aMin, long aMax, long bMin, long bMax) {
		final long _min = Math.max(aMin, bMin);
		final long _max = Math.min(aMax, bMax);
		if(_min < _max) return true;
		return false;
	}
	
	/**
	 * Test if set of Terms violates capacity
	 * @param peak
	 * @param capacity
	 * @return
	 */
	protected abstract boolean isConflicting( Statement[] peak );
	
	
}
