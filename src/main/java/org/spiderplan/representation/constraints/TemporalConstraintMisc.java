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
package org.spiderplan.representation.constraints;
//package org.spiderplan.representation.constraints;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.HashSet;
//import java.util.Set;
//
//import org.spiderplan.representation.logic.Atomic;
//import org.spiderplan.representation.logic.Substitutable;
//import org.spiderplan.representation.logic.Substitution;
//import org.spiderplan.representation.logic.Term;
//
//
//public class TemporalConstraintMisc extends Constraint implements MatchableConstraint, Substitutable {
//	private Atomic a;
//	
//	private Term from;
//	private Term to;
//	private Type relation;
//	private Interval bounds[];
//	
//	private boolean isAllensConstraint;
//	
//	public enum Type { 
//		Equals, Before, BeforeOrMeets, After, Meets, MetBy,  MetByOrAfter, 
//		Starts, StartedBy, During, DuringOrEquals, Contains, Finishes, FinishedBy, 
//		Overlaps, EndsDuring, OverlappedBy, At, StartStart, EndEnd, Duration, Release, 
//		Deadline, MetByOrOverlappedBy, 
//		MeetsOrOverlapsOrFinishedByOrContains, 
//		ContainsOrStartedByOrOverlappedByOrMetBy,
//		MeetsOrOverlapsOrBefore,
//		MetByOrOverlappedByOrAfter,
//		Makespan, Intersection, Rigidity, Draw
//	};
//	
//	/**
//	 * Create a binary {@link TemporalConstraintMisc}.
//	 * @param from First interval key {@link Term}
//	 * @param to Second interval key {@link Term}
//	 * @param relation {@link Type} of temporal constraint.
//	 * @param bounds
//	 */
//	public TemporalConstraintMisc( Term from, Term to, Type relation, Interval... bounds ) {
//		this.from = from;
//		this.to = to;
//		this.relation = relation;
//		this.bounds = bounds;
//		if ( this.bounds == null ) {
//			this.bounds = new Interval[0];
//		}
//		
//		String aName = relation + "(" + from + "," + to;
//		for ( Interval i : bounds ) {
//			aName += "," + i.getMinTerm() + "," + i.getMaxTerm();
//		}
//		this.a = new Atomic(aName);
//	}
//	
//	/**
//	 * Create a unary {@link TemporalConstraintMisc}.
//	 * @param from Interval key {@link Term}
//	 * @param relation {@link Type} of temporal constraint.
//	 * @param bounds
//	 */
//	public TemporalConstraintMisc( Term from, Type relation, Interval... bounds ) {
//		this.from = from;
//		this.to = from;
//		this.relation = relation;
//		this.bounds = bounds;
//		if ( this.bounds == null ) {
//			this.bounds = new Interval[0];
//		}
//		
//		String aName = relation + "(" + this.from;
//		if ( this.to != this.from ) {
//			aName += "," + this.to;
//		}
//		for ( Interval i : this.bounds ) {
//			aName += "," + i.getMinTerm() + "," + i.getMaxTerm();
//		}
//		this.a = new Atomic(aName);
//	}
//	
//	/**
//	 * Copy constructor
//	 * @param tC
//	 */
//	public TemporalConstraintMisc( TemporalConstraintMisc tC ) {
//		if ( tC.isAllensConstraint ) {
//			this.from = tC.from.copy();
//		}
//		if ( tC.to != null ) {
//			this.to= tC.to.copy();
//		} else {
//			this.to = this.from;
//		}
//		
//		this.relation = tC.relation;
//		this.isAllensConstraint = tC.isAllensConstraint;
//		this.a = tC.a.copy();
//		
//		Interval[] bounds = new Interval[tC.bounds.length];
//		
//		for ( int i = 0 ; i < bounds.length ; i++ ) {
//			bounds[i] = tC.bounds[i].copy();
//		}
//		this.bounds = bounds;
//	}
//	
//	/**
//	 * Create a {@link TemporalConstraintMisc} based on its {@link Atomic} representation.
//	 * @param a
//	 */
//	public TemporalConstraintMisc( Atomic a ) {
//		
//		from = null;
//		to = null;
//		relation = Type.valueOf(a.name());		
//		bounds = null;
//		
//		if ( a.name().toString().equals("Duration") ) {
//			from = a.getTerms()[0];
//			to = from;
//			bounds = new Interval[1];
//			bounds[0] = new Interval(a.getTerms()[1], a.getTerms()[2]);
//		} else if ( a.name().toString().equals("Release") ) {
//			from = a.getTerms()[0];
//			to = from;
//			bounds = new Interval[1];
//			bounds[0] = new Interval(a.getTerms()[1], a.getTerms()[2]);
//		} else if ( a.name().toString().equals("Deadline") ) {
//			from = a.getTerms()[0];
//			to = from;
//			bounds = new Interval[1];
//			bounds[0] = new Interval(a.getTerms()[1], a.getTerms()[2]);
//		} else if ( a.name().toString().equals("At") ) {
//			from = a.getTerms()[0];
//			to = from;
//			bounds = new Interval[2];
//			bounds[0] = new Interval(a.getTerms()[1], a.getTerms()[2]);
//			bounds[1] = new Interval(a.getTerms()[3], a.getTerms()[4]);
//		} else if ( a.name().toString().equals("Equals") ) {
//			from = a.getTerms()[0];
//			to = a.getTerms()[1];
//		} else if ( a.name().toString().equals("Before") ) {
//			from = a.getTerms()[0];
//			to = a.getTerms()[1];
//			bounds = new Interval[1];
//			bounds[0] = new Interval(a.getTerms()[2], a.getTerms()[3]);
//		} else if ( a.name().toString().equals("BeforeOrMeets") ) {
//			from = a.getTerms()[0];
//			to = a.getTerms()[1];
//			bounds = new Interval[1];
//			bounds[0] = new Interval(a.getTerms()[2], a.getTerms()[3]);
//		} else if ( a.name().toString().equals("After") ) {
//			from = a.getTerms()[0];
//			to = a.getTerms()[1];
//			bounds = new Interval[1];
//			bounds[0] = new Interval(a.getTerms()[2], a.getTerms()[3]);
//		} else if ( a.name().toString().equals("MetByOrAfter") ) {
//			from = a.getTerms()[0];
//			to = a.getTerms()[1];
//			bounds = new Interval[1];
//			bounds[0] = new Interval(a.getTerms()[2], a.getTerms()[3]);
//		} else if ( a.name().toString().equals("Meets") ) {
//			from = a.getTerms()[0];
//			to = a.getTerms()[1];
//		} else if ( a.name().toString().equals("MetBy") ) {
//			from = a.getTerms()[0];
//			to = a.getTerms()[1];
//		} else if ( a.name().toString().equals("Starts") ) {
//			from = a.getTerms()[0];
//			to = a.getTerms()[1];
//			bounds = new Interval[1];
//			bounds[0] = new Interval(a.getTerms()[2], a.getTerms()[3]);
//		} else if ( a.name().toString().equals("StartedBy") ) {
//			from = a.getTerms()[0];
//			to = a.getTerms()[1];
//			bounds = new Interval[1];
//			bounds[0] = new Interval(a.getTerms()[2], a.getTerms()[3]);
//		} else if ( a.name().toString().equals("During") ) {
//			from = a.getTerms()[0];
//			to = a.getTerms()[1];
//			bounds = new Interval[2];
//			bounds[0] = new Interval(a.getTerms()[2], a.getTerms()[3]);
//			bounds[1] = new Interval(a.getTerms()[4], a.getTerms()[5]);
//		} else if ( a.name().toString().equals("DuringOrEquals") ) {
//			from = a.getTerms()[0];
//			to = a.getTerms()[1];
//			bounds = new Interval[2];
//			bounds[0] = new Interval(a.getTerms()[2], a.getTerms()[3]);
//			bounds[1] = new Interval(a.getTerms()[4], a.getTerms()[5]);
//		} else if ( a.name().toString().equals("Contains") ) {
//			from = a.getTerms()[0];
//			to = a.getTerms()[1];
//			bounds = new Interval[2];
//			bounds[0] = new Interval(a.getTerms()[2], a.getTerms()[3]);
//			bounds[1] = new Interval(a.getTerms()[4], a.getTerms()[5]);
//		} else if ( a.name().toString().equals("Finishes") ) {
//			from = a.getTerms()[0];
//			to = a.getTerms()[1];
//			bounds = new Interval[1];
//			bounds[0] = new Interval(a.getTerms()[2], a.getTerms()[3]);
//		} else if ( a.name().toString().equals("FinishedBy") ) {
//			from = a.getTerms()[0];
//			to = a.getTerms()[1];
//			bounds = new Interval[1];
//			bounds[0] = new Interval(a.getTerms()[2], a.getTerms()[3]);
//		} else if ( a.name().toString().equals("Overlaps") ) {
//			from = a.getTerms()[0];
//			to = a.getTerms()[1];
//			bounds = new Interval[1];
//			bounds[0] = new Interval(a.getTerms()[2], a.getTerms()[3]);
//		} else if ( a.name().toString().equals("OverlappedBy") ) {
//			from = a.getTerms()[0];
//			to = a.getTerms()[1];
//			bounds = new Interval[1];
//			bounds[0] = new Interval(a.getTerms()[2], a.getTerms()[3]);
//		} else if ( a.name().toString().equals("StartStart") ) {
//			from = a.getTerms()[0];
//			to = a.getTerms()[1];
//			bounds = new Interval[1];
//			bounds[0] = new Interval(a.getTerms()[2], a.getTerms()[3]);
//		} else if ( a.name().toString().equals("EndEnd") ) {
//			from = a.getTerms()[0];
//			to = a.getTerms()[1];
//			bounds = new Interval[1];
//			bounds[0] = new Interval(a.getTerms()[2], a.getTerms()[3]);
//		} else if ( a.name().toString().equals("EndsDuring") ) {
//			from = a.getTerms()[0];
//			to = a.getTerms()[1];
//		} else if ( a.name().toString().equals("MeetsOrOverlapsOrBefore") ) {
//			from = a.getTerms()[0];
//			to = a.getTerms()[1];
//			bounds = new Interval[1];
//			bounds[0] = new Interval(a.getTerms()[2], a.getTerms()[3]);
//		} else if ( a.name().toString().equals("MetByOrOverlappedByOrAfter") ) {
//			from = a.getTerms()[0];
//			to = a.getTerms()[1];
//			bounds = new Interval[1];
//			bounds[0] = new Interval(a.getTerms()[2], a.getTerms()[3]);
//		} else if ( a.name().toString().equals("MeetsOrOverlapsOrFinishedByOrContains") ) {
//			from = a.getTerms()[0];
//			to = a.getTerms()[1];
//			bounds = new Interval[1];
//			bounds[0] = new Interval(a.getTerms()[2], a.getTerms()[3]);
//		} else if ( a.name().toString().equals("ContainsOrStartedByOrOverlappedByOrMetBy") ) {
//			from = a.getTerms()[0];
//			to = a.getTerms()[1];
//			bounds = new Interval[1];
//			bounds[0] = new Interval(a.getTerms()[2], a.getTerms()[3]);
//		} else if ( a.name().toString().equals("MetByOrOverlappedBy") ) {
//			from = a.getTerms()[0];
//			to = a.getTerms()[1];
//			bounds = new Interval[1];
//			bounds[0] = new Interval(a.getTerms()[2], a.getTerms()[3]);
//		} else if ( a.name().toString().equals("Makespan") ) {
//			this.a = a.copy();
//		} else if ( a.name().toString().equals("Intersect") ) {
//			this.a = a.copy();
//		} else if ( a.name().toString().equals("Draw") ) {
//			this.a = a.copy();
//		} else {
//			throw new IllegalStateException("Unknown type or missing arguments for temporal constraint: " + a);
//		}
//		
//		this.relation = Type.valueOf(a.name());
//
//		this.a = a;
//		isAllensConstraint = (from != null);
//		
//		if ( bounds == null ) {
//			bounds = new Interval[0];
//		}
//
//	}
//	
//	/**
//	 * Create a {@link TemporalConstraintMisc} based on its {@link String} representation.
//	 * @param s
//	 */
//	public TemporalConstraintMisc( String s ) {
//		from = new Term(s.split(" ")[0]);
//		String relation = s.split(" ")[1];
//		
//		this.relation = Type.valueOf(relation);
//		
//		if ( !s.split(" ")[2].trim().equals("") && !s.split(" ")[2].contains("[") ) {
//			to = new Term(s.split(" ")[2]);
//		} else {
//			to = null;
//		}
//		
//		ArrayList<Interval> boundsArray = new ArrayList<Interval>();
//		if ( s.contains("[") ) {
//			String[] tmp = s.split("\\[");
//			for ( int i = 1 ; i < tmp.length ; i++ ) {
//				tmp[i] = "[" + tmp[i];
//				boundsArray.add( new Interval(tmp[i]) );
//			}
//			Interval[] bounds = boundsArray.toArray(new Interval[boundsArray.size()]);
//			this.bounds = bounds;
//		} else {
//			this.bounds = new Interval[0];
//		}
//		
//		String aName = relation + "(" + this.from;
//		if ( this.to != null ) {
//			aName += "," + this.to;
//		}
//		for ( Interval i : this.bounds ) {
//			aName += "," + i.getMinTerm() + "," + i.getMaxTerm();
//		}
//		this.a = new Atomic(aName);
//	}
//	
//	public Term getFrom() { return from; };
//	public Term getTo() { return to; };
//	public Type getRelation() { return relation; };
//	public Atomic getAtomic() { return this.a; };
//	public Interval[] getBounds() { return bounds; };
//	
//	public void setFrom( Term from ) { 
//		this.from = from;		
//	};
//	public void setTo( Term to ) { this.to = to; };
//	public void setRelation( Type relation ) { this.relation = relation; };
//	public void setRelation( String relation ) { this.relation = Type.valueOf(relation);	};
//	public void setBounds( Interval[] bounds ) { this.bounds = bounds; };
//	
//	public boolean isUnary() { return to == null || to.equals(from); };
//	public boolean isBinary() { return to != null && !to.equals(from); };
//	public boolean isAllenConstraint() { return isAllensConstraint; };
//	
//	
//	/**
//	 * Variable terms in the bounds of this {@link TemporalConstraintMisc}
//	 * will be set to least constraining concrete values.
//	 * Allows to perform reasoning on non-ground temporal constraints.
//	 * This will be 0 or 1 for lower bounds (depending on the constraint)
//	 * and inf for all upper bounds.
//	 */
//	public void setVariableBoundsToMostRelaxed() {
//		for ( Interval i : bounds ) {
//			if ( i != null ) {
//				if ( i.getMinTerm().isVariable() ) {
//					if ( this.relation.equals(Type.After) 
//					   | this.relation.equals(Type.Before)
//					   | this.relation.equals(Type.Overlaps)
//					   | this.relation.equals(Type.OverlappedBy)
//					   | this.relation.equals(Type.Finishes)
//					   | this.relation.equals(Type.FinishedBy)
//					   | this.relation.equals(Type.Starts)
//					   | this.relation.equals(Type.StartedBy)
//					   | this.relation.equals(Type.During)
//					   | this.relation.equals(Type.Contains)
//					   | this.relation.equals(Type.OverlappedBy)
//					   | this.relation.equals(Type.OverlappedBy)
//					   | this.relation.equals(Type.OverlappedBy) ) {
//						i.setMinTerm(new Term("1"));	
//					} else {
//						i.setMinTerm(new Term("0"));
//					}
//				}
//				if ( i.getMaxTerm().isVariable() ) {
//					i.setMaxTerm(new Term("inf"));
//				}
//			}
//		}
//	}
//	
//	@Override
//	public Collection<Term> getVariableTerms() {
//		Set<Term> r = new HashSet<Term>(); 
//		r.addAll(from.getVariables());
//		r.addAll(to.getVariables());
//		for ( Interval b : bounds ) {
//			r.addAll(b.getMinTerm().getVariables());
//			r.addAll(b.getMaxTerm().getVariables());
//		}
//		return r;
//	}
//	@Override
//	public Collection<Term> getGroundTerms() {
//		Set<Term> r = new HashSet<Term>();
//		if ( from.isGround() )
//			r.add(from);
//		if ( to.isGround() )
//			r.add(to);
//		for ( Interval b : bounds ) {
//			if ( b.getMinTerm().isGround() )
//				r.add(b.getMinTerm());
//			if ( b.getMaxTerm().isGround() )
//				r.add(b.getMaxTerm());
//		}
//		return r;
//	}
//	@Override
//	public Collection<Atomic> getAtomics() {
//		ArrayList<Atomic> r = new ArrayList<Atomic>();
//		return r;		
//	}
//	
//	@Override
//	public boolean isGround() {
//		for ( Interval b : bounds ) {
//			if ( b != null && !b.isGround() ) {
//				return false;
//			}
//		}
//		return true;
//	}
//
//	@Override
//	public Substitution match(Constraint c) {
//		if ( c instanceof TemporalConstraintMisc ) {
//			TemporalConstraintMisc tc = (TemporalConstraintMisc)c;
//			
//			if ( !this.relation.equals(tc.relation) ) {
//				return null;
//			}
//			
//			if ( this.bounds.length != tc.bounds.length ) {
//				return null;
//			}
//			
//			Substitution theta = this.from.match(tc.from);
//			
//			if ( theta == null ) {
//				return null;
//			}
//			
//			if ( this.to != null && tc.to != null )  {
//				if ( !theta.add(this.to.match(tc.to)) ) {
//					return null;
//				}
//			} 
//			
//			for ( int i = 0; i < this.bounds.length ; i++ ) {
//				if ( bounds[i] != null ) {
//					if ( !theta.add( this.bounds[i].match(tc.bounds[i]) )) {
//						return null;
//					}
//				}
//			}
//			return theta;
//		}
//		return null;
//	}
//	
//	@Override
//	public void substitute(Substitution theta) {
//		if ( this.from != null ) {
//			this.from.substitute(theta);	
//		}		
//		if ( this.to != null )
//			this.to.substitute(theta);
//
//		for ( int i = 0 ; i < bounds.length ; i++ ) {
//			if ( bounds[i] != null ) {
//				bounds[i].substitute(theta);
//			}
//		}
//		this.a.substitute(theta);
//	}
//	
//	@Override
//	public TemporalConstraintMisc copy() {
//		TemporalConstraintMisc c = new TemporalConstraintMisc(this);
//		c.setAsserted(this.isAsserted());
//		c.setDescription(this.getDescription());
//		return c;
//	}
//		
//	@Override
//	public boolean equals(Object o) {
//		if ( o instanceof TemporalConstraintMisc ) {
////			TemporalConstraintMisc tC = (TemporalConstraintMisc)o;
////			if ( !this.from.equals(tC.from) )
////				return false;
////			if ( !((this.isUnary() && tC.isUnary() ) || this.isBinary() && tC.isBinary())  )
////				return false;
////			if ( this.isBinary() && !(this.to.equals(tC.to)))
////				return false;
////			
////			if ( ! this.relation.equals(tC.relation) )
////				return false;
////			
////			if ( !(this.bounds.length == tC.bounds.length) ) {
////				return false;
////			}
////			
////			for ( int i = 0 ; i < this.bounds.length; i++ ) {
////				if ( !this.bounds[i].equals(tC.bounds[i]) ) { 
////					return false;
////				}						
////			}		
//			return this.toString().equals(o.toString());
//		}
//		return false;
//	}
//
//	@Override
//	public int hashCode() {
//		return this.toString().hashCode();
//	}	
//	
//	@Override
//	public String toString() {
////		String r = from.toString() + " " + relation;
////		if ( this.isBinary() ) 
////			r += " " + to.toString();
////		
////		for ( int i = 0 ; i < bounds.length ; i++ ) {
////			if ( bounds[i] != null ) {
////				r += " " + bounds[i].toString(); 
////			}
////		}
//		
//		return a.toString();
//	}
//}
