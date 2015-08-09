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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.constraints.constraintInterfaces.Assertable;
import org.spiderplan.representation.constraints.constraintInterfaces.Matchable;
import org.spiderplan.representation.constraints.constraintInterfaces.Mutable;
import org.spiderplan.representation.constraints.constraintInterfaces.Substitutable;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Substitution;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.tools.UniqueID;


/**
 * Any {@link ConstraintDatabase} that supports the Condition
 * must be consistent after adding one of the Resolvers:
 * 
 * C => (R1 v R2 v ... Rn)
 * 
 * Note: Resolvers can add new Statements.
 * 
 * @author Uwe Köckemann
 *
 */
public class InteractionConstraint extends Constraint implements Substitutable, Matchable, Mutable, Assertable {
	
	private Atomic name;
	
	private ConstraintDatabase Condition = new ConstraintDatabase();
	private ArrayList<ConstraintDatabase> Resolvers = new ArrayList<ConstraintDatabase>();
	
	private int pickedResolver = -1;
	
	private boolean isAsserted = false;
	
	public InteractionConstraint( Atomic name ) {
		super(ConstraintTypes.Conditional);
		this.name = name;
	}

	public Atomic getName() { return name; };
	public void setName( Atomic name ) { this.name = name; };
	
	public void setCondition( ConstraintDatabase C ) {
		this.Condition = C;
	}
	public void addResolver( ConstraintDatabase R ) {
		this.Resolvers.add(R);
	}
	
	public ConstraintDatabase getCondition( ) {
		return this.Condition;
	}
	
	public ArrayList<ConstraintDatabase> getResolvers() {
		return Resolvers;
	}
	
	/**
	 * Remember which resolver was chosen
	 * @param i index of resolver.
	 */
	public void setResolverIndex( int i ) { pickedResolver = i; }; 
	
	/**
	 * Get the chosen resolver
	 * @return index of chosen resolver
	 */
	public int getResolverIndex() { return pickedResolver; };
		
	public ArrayList<ConstraintDatabase> getAllEnablers( ConstraintDatabase cDB ) {
		Collection<Substitution> enablingSubst = cDB.getSubstitutions(this.getCondition());
		
		ArrayList<ConstraintDatabase> r = new ArrayList<ConstraintDatabase>();
		
		for ( Substitution theta : enablingSubst ) {
			ConstraintDatabase enabler = this.Condition.copy();
			enabler.substitute(theta);
			r.add(enabler);
		}
		return r;
	}
	
	/**
	 * Make all interval key {@link Term}s ground.
	 */
	public void makeKeysGround() {
		Substitution sub = new Substitution();
		for ( Statement s : this.getCondition().getConstraints().get(Statement.class) ) {
//			sub.add(s.getKey(), Term.createConstantID());
			sub.add(s.getKey(), s.getKey().makeConstant());
		}
		for ( ConstraintDatabase resolver : this.getResolvers() ) {
			for ( Statement s : resolver.getConstraints().get(Statement.class) ) {
//				sub.add(s.getKey(), Term.createConstantID());
				sub.add(s.getKey(), s.getKey().makeConstant());
			}
		}
		this.substitute(sub);
	}
	
	public void makeUniqueGroundKeys() {
		long ID = UniqueID.getID();
		Substitution sub = new Substitution();
		for ( Statement s : this.getCondition().getConstraints().get(Statement.class) ) {
			if ( s.getKey().isVariable() ) {
//				sub.add(s.getKey(), Term.createConstantID());
				sub.add(s.getKey(), Term.createConstant(s.getKey().toString().replace("?", "") + "#" + ID));
			}
		}
		for ( OpenGoal s : this.getCondition().getConstraints().get(OpenGoal.class) ) {
			if ( s.getStatement().getKey().isVariable() ) {
//				sub.add(s.getStatement().getKey(), Term.createConstantID());
				sub.add(s.getStatement().getKey(), Term.createConstant(s.getStatement().getKey().toString().replace("?", "") + "#" + ID));
			}
		}
		for ( ConstraintDatabase resolver : this.getResolvers() ) {
			for ( Statement s : resolver.getConstraints().get(Statement.class) ) {
				if ( s.getKey().isVariable() ) {
//					sub.add(s.getKey(), Term.createConstantID());
					sub.add(s.getKey(), Term.createConstant(s.getKey().toString().replace("?", "") + "#" + ID));
				}
			}
			for ( OpenGoal s : resolver.getConstraints().get(OpenGoal.class) ) {
				if ( s.getStatement().getKey().isVariable() ) {
//					sub.add(s.getStatement().getKey(), Term.createConstantID());
					sub.add(s.getStatement().getKey(), Term.createConstant(s.getStatement().getKey().toString().replace("?", "") + "#" + ID));
				}
			}
		}
		this.substitute(sub);
	}
	
		
	@Override
	public Collection<Term> getVariableTerms() {
		ArrayList<Term> r = new ArrayList<Term>();
		r.addAll(this.Condition.getVariableTerms());
		for ( ConstraintDatabase res : this.Resolvers ) {
			r.addAll(res.getVariableTerms());
		}
		r.addAll(this.name.getVariableTerms());
		return r;		
	}
	@Override
	public Collection<Term> getGroundTerms() {
		ArrayList<Term> r = new ArrayList<Term>();
		r.addAll(this.Condition.getGroundTerms());
		for ( ConstraintDatabase res : this.Resolvers ) {
			r.addAll(res.getGroundTerms());
		}
		r.addAll(this.name.getGroundTerms());
		return r;		
	}
	@Override
	public Collection<Atomic> getAtomics() {
		Set<Atomic> r = new HashSet<Atomic>();
		return r;
	}
		
	@Override
	public String toString() {
		StringBuilder r = new StringBuilder();
		r.append("(conditional\n\t" + this.name + "\n");
		r.append("\t(condition\n");
		String s = "\t\t"+this.Condition.toString().toString().replace("\n", "\n\t\t");	
		r.append(s.substring(0, s.length()-2));
		r.append("\t)");
		
		for ( ConstraintDatabase c : this.Resolvers ) {
			r.append("\n\t(resolver\n");
			s = "\t\t"+c.toString().replace("\n", "\n\t\t");
			
			r.append(s.substring(0, s.length()-2));
			r.append("\t)");
		}		
		r.append("\n)");
		return r.toString();
	}
	
	@Override
	public InteractionConstraint copy() {
//		Profiler.probe(3);
		InteractionConstraint c = new InteractionConstraint(this.name);

		c.Condition = this.Condition.copy();
		
		for ( ConstraintDatabase con : this.Resolvers ) {
			c.addResolver(con.copy());
		}
		c.pickedResolver = this.pickedResolver;
		c.setAsserted(this.isAsserted());
		return c;
	}
	@Override
	public Constraint substitute(Substitution theta) {
//		Profiler.probe(0);
		this.name = this.name.substitute(theta);
		this.Condition.substitute(theta);
		for ( ConstraintDatabase r : Resolvers ) {
			r.substitute(theta);
		}
	
		return this;
	}
	@Override
	public boolean equals(Object o) {	
		if ( ( o instanceof InteractionConstraint ) ) {			
			InteractionConstraint iaC = (InteractionConstraint)o;

			if ( !this.name.equals(iaC.name) ) {
				return false;
			}
//			if ( !this.Condition.equals(iaC.Condition) ) {
//				return false;
//			}
//			for ( int i = 0 ; i < this.Resolvers.size() ; i++ ) {
//				if ( ! this.Resolvers.get(i).equals((iaC).Resolvers.get(i))) {
//					return false;
//				}
//			}
			return true;
		}
		return false;
	}
	@Override
	public int hashCode() {
		return this.name.hashCode();
	}
	@Override
	public boolean isGround() {
		return name.isGround();
	}
	
	@Override
	public Substitution match(Constraint c) {
		if ( c instanceof InteractionConstraint ) {
			InteractionConstraint ic = (InteractionConstraint)c;
			return this.getName().match(ic.getName());
		}
		return null;
	}
	
	@Override
	public boolean isAsserted() {
		return isAsserted;
	}
	@Override
	public Constraint setAsserted(boolean asserted) {
		isAsserted = asserted;
		return this;
	}
}
