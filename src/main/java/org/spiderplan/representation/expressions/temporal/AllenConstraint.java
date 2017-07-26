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
package org.spiderplan.representation.expressions.temporal;

import java.util.ArrayList;

import java.util.Collection;
import org.spiderplan.representation.expressions.Expression;
import org.spiderplan.representation.expressions.ExpressionTypes;
import org.spiderplan.representation.expressions.ExpressionTypes.TemporalRelation;
import org.spiderplan.representation.expressions.domain.Substitution;
import org.spiderplan.representation.expressions.interfaces.Matchable;
import org.spiderplan.representation.expressions.interfaces.Substitutable;
import org.spiderplan.representation.logic.Term;

/**
 * Representing quantitative Allen interval constraints.
 *
 * TODO: check construction during parsing (e.g., duration constraints with > 1 interval leads 
 * to error)
 * 
 * @author Uwe Köckemann
 *
 */
public class AllenConstraint extends Expression implements Matchable, Substitutable {	
	private Term from;
	private Term to;
	private TemporalRelation relation;
	private Interval bounds[];
	
	final private static Term Time0 = Term.createInteger(0);
	final private static Term Time1 = Term.createInteger(1);
	final private static Term TimeInf = Term.createConstant("inf");
	
	/**
	 * Create a binary {@link AllenConstraint}.
	 * @param from First interval key {@link Term}
	 * @param to Second interval key {@link Term}
	 * @param relation {@link TemporalRelation} of temporal constraint.
	 * @param bounds
	 */
	public AllenConstraint( Term from, Term to, TemporalRelation relation, Interval... bounds ) {
		super(ExpressionTypes.Temporal);
		this.from = from;
		this.to = to;
		this.relation = relation;
		this.bounds = bounds;
		if ( this.bounds == null ) {
			this.bounds = new Interval[0];
		}
//		if ( !checkLegalBounds() ) {
//			throw new IllegalStateException("Illegal bounds for " + this + "\n" + ConstraintTypes.TemporalConstraints.toString());
//		}
	}
	
	/**
	 * Create a unary {@link AllenConstraint}.
	 * @param from Interval key {@link Term}
	 * @param relation {@link TemporalRelation} of temporal constraint.
	 * @param bounds
	 */
	public AllenConstraint( Term from, TemporalRelation relation, Interval... bounds ) {
		super(ExpressionTypes.Temporal);
		this.from = from;
		this.to = null;
		this.relation = relation;
		this.bounds = bounds;
		if ( this.bounds == null ) {
			this.bounds = new Interval[0];
		}
//		if ( !checkLegalBounds() ) {
//			throw new IllegalStateException("Illegal bounds for " + this + "\n" + ConstraintTypes.TemporalConstraints.toString());
//		}
	}
	
	/**
	 * Copy constructor
	 * @param tC
	 */
	public AllenConstraint( AllenConstraint tC ) {
		super(ExpressionTypes.Temporal);
		this.from = tC.from;
		if ( tC.to != null ) {
			this.to = tC.to;
		} else {
			this.to = null;
		}
		
		this.relation = tC.relation;
		
		Interval[] bounds = new Interval[tC.bounds.length];
		
		for ( int i = 0 ; i < bounds.length ; i++ ) {
			bounds[i] = tC.bounds[i];
		}
		this.bounds = bounds;
//		if ( !checkLegalBounds() ) {
//			throw new IllegalStateException("Illegal bounds for " + this + "\n" + ConstraintTypes.TemporalConstraints.toString());
//		}
	}
	
	/**
	 * Create a {@link AllenConstraint} based on its {@link Term} representation.
	 * @param a
	 */
	public AllenConstraint( Term a ) {
		super(ExpressionTypes.Temporal);
		from = null;
		to = null;		
		bounds = null;
		
		String baseName = a.getName();
		baseName = baseName.toLowerCase().replace("-", "");
				
		if ( baseName.equals("duration") ) {
			relation = TemporalRelation.Duration;
			from = a.getArg(0);
//			to = from;
			bounds = new Interval[1];
			if ( a.getNumArgs() == 2 ) {
				bounds[0] = new Interval(a.getArg(1));
			} else {
				bounds[0] = new Interval(a.getArg(1),a.getArg(2));
			}
		} else if ( baseName.equals("release") ) {
			relation = TemporalRelation.Release;
			from = a.getArg(0);
//			to = from;
			bounds = new Interval[1];
			if ( a.getNumArgs() == 2 ) {
				bounds[0] = new Interval(a.getArg(1));
			} else {
				bounds[0] = new Interval(a.getArg(1),a.getArg(2));
			}
		} else if ( baseName.equals("deadline") ) {
			relation = TemporalRelation.Deadline;
			from = a.getArg(0);
//			to = from;
			bounds = new Interval[1];
			bounds[0] = new Interval(a.getArg(1));
		} else if ( baseName.equals("at") ) {
			relation = TemporalRelation.At;
			from = a.getArg(0);
//			to = from;
			bounds = new Interval[2];
			
			if ( a.getNumArgs() == 3 ) {
				bounds[0] = new Interval(a.getArg(1));
				bounds[1] = new Interval(a.getArg(2));
			} else {
				bounds[0] = new Interval(a.getArg(1),a.getArg(2));
				bounds[1] = new Interval(a.getArg(3),a.getArg(4));
			}
		} else if ( baseName.equals("equals") ) {
			relation = TemporalRelation.Equals;
			from = a.getArg(0);
			to = a.getArg(1);
		} else if ( baseName.equals("before") ) {
			relation = TemporalRelation.Before;
			from = a.getArg(0);
			to = a.getArg(1);
			bounds = new Interval[1];
			if ( a.getNumArgs() == 3 ) {
				bounds[0] = new Interval(a.getArg(2));
			} else {
				bounds[0] = new Interval(a.getArg(2),a.getArg(3));
			}
		} else if ( baseName.equals("beforeormeets") ) {
			relation = TemporalRelation.BeforeOrMeets;
			from = a.getArg(0);
			to = a.getArg(1);
			bounds = new Interval[1];
			bounds[0] = new Interval(a.getArg(2));
		} else if ( baseName.equals("after") ) {
			relation = TemporalRelation.After;
			from = a.getArg(0);
			to = a.getArg(1);
			bounds = new Interval[1];
			if ( a.getNumArgs() == 3 ) {
				bounds[0] = new Interval(a.getArg(2));
			} else {
				bounds[0] = new Interval(a.getArg(2),a.getArg(3));
			}
		} else if ( baseName.equals("metbyorafter") ) {
			relation = TemporalRelation.MetByOrAfter;
			from = a.getArg(0);
			to = a.getArg(1);
			bounds = new Interval[1];
			bounds[0] = new Interval(a.getArg(2));
		} else if ( baseName.equals("meets") ) {
			relation = TemporalRelation.Meets;
			from = a.getArg(0);
			to = a.getArg(1);
		} else if ( baseName.equals("metby") ) {
			relation = TemporalRelation.MetBy;
			from = a.getArg(0);
			to = a.getArg(1);
		} else if ( baseName.equals("starts") ) {
			relation = TemporalRelation.Starts;
			from = a.getArg(0);
			to = a.getArg(1);
			bounds = new Interval[1];
			bounds[0] = new Interval(a.getArg(2));
		} else if ( baseName.equals("startedby") ) {
			relation = TemporalRelation.StartedBy;
			from = a.getArg(0);
			to = a.getArg(1);
			bounds = new Interval[1];
			bounds[0] = new Interval(a.getArg(2));
		} else if ( baseName.equals("during") ) {
			relation = TemporalRelation.During;
			from = a.getArg(0);
			to = a.getArg(1);
			bounds = new Interval[2];
			bounds[0] = new Interval(a.getArg(2));
			bounds[1] = new Interval(a.getArg(3));
		} else if ( baseName.equals("duringorequals") ) {
			relation = TemporalRelation.DuringOrEquals;
			from = a.getArg(0);
			to = a.getArg(1);
			bounds = new Interval[2];			
			if ( a.getNumArgs() == 3 ) {
				bounds[0] = new Interval(a.getArg(2));
				bounds[1] = new Interval(a.getArg(3));
			} else {
				bounds[0] = new Interval(a.getArg(2),a.getArg(3));
				bounds[1] = new Interval(a.getArg(4),a.getArg(5));
			}
		} else if ( baseName.equals("contains") ) {
			relation = TemporalRelation.Contains;
			from = a.getArg(0);
			to = a.getArg(1);
			bounds = new Interval[2];
			bounds[0] = new Interval(a.getArg(2));
			bounds[1] = new Interval(a.getArg(3));
		} else if ( baseName.equals("finishes") ) {
			relation = TemporalRelation.Finishes;
			from = a.getArg(0);
			to = a.getArg(1);
			bounds = new Interval[1];
			bounds[0] = new Interval(a.getArg(2));
		} else if ( baseName.equals("finishedby") ) {
			relation = TemporalRelation.FinishedBy;
			from = a.getArg(0);
			to = a.getArg(1);
			bounds = new Interval[1];
			bounds[0] = new Interval(a.getArg(2));
		} else if ( baseName.equals("overlaps") ) {
			relation = TemporalRelation.Overlaps;
			from = a.getArg(0);
			to = a.getArg(1);
			bounds = new Interval[1];
			bounds[0] = new Interval(a.getArg(2));
		} else if ( baseName.equals("overlappedby") ) {
			relation = TemporalRelation.OverlappedBy;
			from = a.getArg(0);
			to = a.getArg(1);
			bounds = new Interval[1];
			bounds[0] = new Interval(a.getArg(2));
//		} else if ( baseName.equals("startstart") ) {
//			relation = TemporalRelation.StartStart;
//			from = a.getArg(0);
//			to = a.getArg(1);
//			bounds = new Interval[1];
//			bounds[0] = new Interval(a.getArg(2));
//		} else if ( baseName.equals("endend") ) {
//			relation = TemporalRelation.EndEnd;
//			from = a.getArg(0);
//			to = a.getArg(1);
//			bounds = new Interval[1];
//			bounds[0] = new Interval(a.getArg(2));
//		} else if ( baseName.equals("endsduring") ) {
//			relation = TemporalRelation.EndsDuring;
//			from = a.getArg(0);
//			to = a.getArg(1);
//		} else if ( baseName.equals("meetsoroverlapsorbefore") ) {
//			relation = TemporalRelation.MeetsOrOverlapsOrBefore;
//			from = a.getArg(0);
//			to = a.getArg(1);
//			bounds = new Interval[1];
//			bounds[0] = new Interval(a.getArg(2));
//		} else if ( baseName.equals("metbyoroverlappedbyorafter") ) {
//			relation = TemporalRelation.MetByOrOverlappedByOrAfter;
//			from = a.getArg(0);
//			to = a.getArg(1);
//			bounds = new Interval[1];
//			bounds[0] = new Interval(a.getArg(2));
//		} else if ( baseName.equals("meetsoroverlapsorfinishedbyorcontains") ) {
//			relation = TemporalRelation.MeetsOrOverlapsOrFinishedByOrContains;
//			from = a.getArg(0);
//			to = a.getArg(1);
//		} else if ( baseName.equals("containsorstartedbyoroverlappedbyormetby") ) {
//			relation = TemporalRelation.ContainsOrStartedByOrOverlappedByOrMetBy;
//			from = a.getArg(0);
//			to = a.getArg(1);
		} else if ( baseName.equals("metbyoroverlappedby") ) {
			relation = TemporalRelation.MetByOrOverlappedBy;
			from = a.getArg(0);
			to = a.getArg(1);
			bounds = new Interval[1];
			bounds[0] = new Interval(a.getArg(2));
		} else {
			throw new IllegalStateException("Unknown type or missing arguments for temporal constraint: " + a);
		}
		
		
		if ( bounds == null ) {
			bounds = new Interval[0];
		}
//		if ( !checkLegalBounds() ) {
//			throw new IllegalStateException("Illegal bounds for " + this + "\n" + ConstraintTypes.TemporalConstraints.toString());
//		}
	}
	
	/**
	 * Create a {@link AllenConstraint} based on its {@link String} represenation.
	 * @param s
	 */
	public AllenConstraint( String s ) {
		super(ExpressionTypes.Temporal);
		from = Term.parse(s.split(" ")[0]);
		String relation = s.split(" ")[1];
		
		this.relation = TemporalRelation.valueOf(relation);
		
		if ( !s.split(" ")[2].trim().equals("") && !s.split(" ")[2].contains("[") ) {
			to = Term.parse(s.split(" ")[2]);
		} else {
			to = null;
		}
		
		ArrayList<Interval> boundsArray = new ArrayList<Interval>();
		if ( s.contains("[") ) {
			String[] tmp = s.split("\\[");
			for ( int i = 1 ; i < tmp.length ; i++ ) {
				tmp[i] = "[" + tmp[i];
				boundsArray.add( new Interval(tmp[i]) );
			}
			Interval[] bounds = boundsArray.toArray(new Interval[boundsArray.size()]);
			this.bounds = bounds;
		} else {
			this.bounds = new Interval[0];
		}
//		if ( !checkLegalBounds() ) {
//			throw new IllegalStateException("Illegal bounds for " + this + "\n" + ConstraintTypes.TemporalConstraints.toString());
//		}
	}
	
	/**
	 * Get first interval of Allen constraint.
	 * @return term representing the interval
	 */
	public Term getFrom() { return from; };
	/**
	 * Get second interval of binary Allen constraint.
	 * @return term representing the interval or <code>null</code> if constraint is unary
	 */
	public Term getTo() { return to; };
	
	/**
	 * Get the relation that determines the meaning of this constraint.
	 * @return the relation
	 */
	public TemporalRelation getRelation() { return relation; };
	
	/**
	 * Returns the number of bounds used by this constraint.
	 * @return number of bounds
	 */
	public int getNumBounds() {
		return bounds.length;
	}
	
	/**
	 * Access a specific bound interval that quantifies 
	 * the distance between specific parts of the interval.
	 * <p>
	 * See {@link TemporalRelation} for details
	 * on the meaning of bounds. 
	 * 
	 * @param n index of the bound interval
	 * @return bound
	 */
	public Interval getBound( int n ) {
		return bounds[n];
	}
	
//	public Interval[] getBounds() { return bounds; };
	
//	public void setFrom( Term from ) { 
//		this.from = from;		
//	};
//	public void setTo( Term to ) { this.to = to; };
//	public void setRelation( TemporalRelation relation ) { this.relation = relation; };
//	public void setRelation( String relation ) { this.relation = TemporalRelation.valueOf(relation);	};
//	public void setBounds( Interval[] bounds ) { this.bounds = bounds; };
	
	
	
	/**
	 * Test if this constraint is unary.
	 * @return <code>true</code> if this constraint is unary, <code>false</code> otherwise
	 */
	public boolean isUnary() { return to == null; };
	/**
	 * Test if this constraint is binary.
	 * @return <code>true</code> if this constraint is binary, <code>false</code> otherwise
	 */
	public boolean isBinary() { return to != null; };
	
	
	/**
	 * Variable terms in the bounds of this {@link AllenConstraint}
	 * will be set to least constraining concrete values.
	 * Allows to perform reasoning on non-ground temporal constraints.
	 * This will be 0 or 1 for lower bounds (depending on the constraint)
	 * and inf for all upper bounds.
	 */
	public void setVariableBoundsToMostRelaxed() {
		for ( Interval i : bounds ) {
			if ( i != null ) {
				if ( i.getLowerTerm().isVariable() ) {
					if ( this.relation.equals(TemporalRelation.After) 
					   | this.relation.equals(TemporalRelation.Before)
					   | this.relation.equals(TemporalRelation.Overlaps)
					   | this.relation.equals(TemporalRelation.OverlappedBy)
					   | this.relation.equals(TemporalRelation.Finishes)
					   | this.relation.equals(TemporalRelation.FinishedBy)
					   | this.relation.equals(TemporalRelation.Starts)
					   | this.relation.equals(TemporalRelation.StartedBy)
					   | this.relation.equals(TemporalRelation.During)
					   | this.relation.equals(TemporalRelation.Contains)
					   | this.relation.equals(TemporalRelation.OverlappedBy)
					   | this.relation.equals(TemporalRelation.OverlappedBy)
					   | this.relation.equals(TemporalRelation.OverlappedBy) ) {
						i.setLowerTerm(Time1);	
					} else {
						i.setLowerTerm(Time0);
					}
				}
				if ( i.getUpperTerm().isVariable() ) {
					i.setUpperTerm(TimeInf);
				}
			}
		}
	}
	
	/**
	 * Return an Allen constraint with a different set of bounds.
	 * @param intervals the new bounds
	 * @return the new constraint
	 */
	public AllenConstraint setBounds( Interval ... intervals )  {
		return new AllenConstraint(this.from, this.to, this.relation, intervals);
	}
	
//	private boolean checkLegalBounds() {
//		for ( Interval i : bounds ) {
//			if ( i != null ) {
//				if ( i.getMin() == 0 ) {
//					if ( this.relation.equals(TemporalRelation.After) 
//					   | this.relation.equals(TemporalRelation.Before)
//					   | this.relation.equals(TemporalRelation.Overlaps)
//					   | this.relation.equals(TemporalRelation.OverlappedBy)
//					   | this.relation.equals(TemporalRelation.Finishes)
//					   | this.relation.equals(TemporalRelation.FinishedBy)
//					   | this.relation.equals(TemporalRelation.Starts)
//					   | this.relation.equals(TemporalRelation.StartedBy)
//					   | this.relation.equals(TemporalRelation.During)
//					   | this.relation.equals(TemporalRelation.Contains)
//					   | this.relation.equals(TemporalRelation.OverlappedBy)
//					   | this.relation.equals(TemporalRelation.OverlappedBy) ) {
//						return false;	
//					} 
//				}
//			}
//		}
//		return true;
//	}
	
	@Override
	public boolean isMatchable() { return true; }
	
	@Override
	public boolean isSubstitutable() { return true; }
	
	
	@Override
	public boolean isGround() {
		for ( Interval b : bounds ) {
			if ( b != null && !b.isGround() ) {
				return false;
			}
		}
		return true;
	}

	@Override
	public Substitution match(Expression c) {
		if ( c instanceof AllenConstraint ) {
			AllenConstraint tc = (AllenConstraint)c;
			
			if ( !this.relation.equals(tc.relation) ) {
				return null;
			}
			
			if ( this.bounds.length != tc.bounds.length ) {
				return null;
			}
			
			Substitution theta = this.from.match(tc.from);
			
			if ( theta == null ) {
				return null; 
			}
			
			if ( this.to != null && tc.to != null )  {
				if ( !theta.add(this.to.match(tc.to)) ) {
					return null;
				}
			} 
			
			for ( int i = 0; i < this.bounds.length ; i++ ) {
				if ( bounds[i] != null ) {
					if ( !theta.add( this.bounds[i].match(tc.bounds[i]) )) {
						return null;
					}
				}
			}
			return theta;
		}
		return null;
	}
	
	@Override
	public Expression substitute(Substitution theta) {
		Interval[] boundsCopy = new Interval[this.bounds.length]; 
		
		for ( int i = 0 ; i < boundsCopy.length ; i++ ) {
			if ( this.bounds[i] != null ) {
				boundsCopy[i] = this.bounds[i].substitute(theta);
			}
		}		
		
		if ( this.to != null )
			return new AllenConstraint(from.substitute(theta), to.substitute(theta), relation, boundsCopy);
		else
			return new AllenConstraint(from.substitute(theta), relation, boundsCopy);
	}
	
//	@Override
//	public AllenConstraint copy() {
////		StopWatch.start("AllenConstraint.copy()");
////		AllenConstraint c = new AllenConstraint(this);
////		StopWatch.stop("AllenConstraint.copy()");
////		return c;
//		
//		return new AllenConstraint(this); 
//	}
	
	@Override
	public boolean equals(Object o) {
//		Profiler.probe(3);
//    	String cName = new Exception().getStackTrace()[1].getClassName();
//    	if( cName.contains("HashMap") || cName.contains("ArrayList")) {
//    		cName = new Exception().getStackTrace()[2].getClassName() + " -> " + cName;
//    		cName = new Exception().getStackTrace()[3].getClassName() + " -> " + cName;
//    		cName = new Exception().getStackTrace()[4].getClassName() + " -> " + cName;
//    		cName = new Exception().getStackTrace()[5].getClassName() + " -> " + cName;
//    	}
//    	stats.increment("Allen.equals() from " + cName); 
		
    	//StopWatch.start("Allen.equals()");
		if ( o instanceof AllenConstraint ) {
//			return this.toString().equals(o.toString());
			
			AllenConstraint tC = (AllenConstraint)o;
			if ( !this.relation.equals(tC.relation) ) {
				//StopWatch.stop("Allen.equals()");
				return false;
			}
			if ( !(this.bounds.length == tC.bounds.length) ) {
				//StopWatch.stop("Allen.equals()");
				return false;
			}
			if ( !((this.isUnary() && tC.isUnary() ) || this.isBinary() && tC.isBinary())  ) {
				//StopWatch.stop("Allen.equals()");
				return false;
			}
			if ( !this.from.equals(tC.from) ) {
				//StopWatch.stop("Allen.equals()");
				return false;
			}
			if ( this.isBinary() && !(this.to.equals(tC.to))) {
				//StopWatch.stop("Allen.equals()");
				return false;
			}
			for ( int i = 0 ; i < this.bounds.length; i++ ) {
				if ( !this.bounds[i].equals(tC.bounds[i]) ) { 
					//StopWatch.stop("Allen.equals()");
					return false;
				}						
			}		
			//StopWatch.stop("Allen.equals()");
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}	
	
	@Override
	public String toString() {
		StringBuilder r = new StringBuilder();
		
		r.append("(");
		r.append(convert(relation));
		r.append(" ");
		r.append(from.toString());
		if ( this.isBinary() ) {
			r.append(" ");
			r.append(to.toString());
		}
		
		for ( int i = 0 ; i < bounds.length ; i++ ) {
			if ( bounds[i] != null ) {
				r.append(" ");
				r.append(bounds[i].toString()); 
			}
		}
		r.append(")");
		return r.toString();
	}
	
	private String convert( TemporalRelation t ) {
		String tStr = t.toString();
		String s = "";
		for ( int i = 0 ; i < tStr.length() ; i++ ) {
			if ( Character.isUpperCase(tStr.charAt(i)) ) {
				if ( i > 0 )
					s += "-" + Character.toLowerCase(tStr.charAt(i));
				else
					s += Character.toLowerCase(tStr.charAt(i));
			} else {
				s += tStr.charAt(i);
			}
		}
		return s;
	}

	@Override
	public void getAllTerms(Collection<Term> collectedTerms, boolean getConstants, boolean getVariables, boolean getComplex) {
		super.type.getAllTerms(collectedTerms, getConstants, getVariables, getComplex);
		this.from.getAllTerms(collectedTerms, getConstants, getVariables, getComplex);
		if ( to != null ) {
			to.getAllTerms(collectedTerms, getConstants, getVariables, getComplex);
		}
	}
}
