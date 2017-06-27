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
package org.spiderplan.representation.expressions.interaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.expressions.ExpressionTypes;
import org.spiderplan.representation.expressions.Expression;
import org.spiderplan.representation.expressions.Statement;
import org.spiderplan.representation.expressions.causal.OpenGoal;
import org.spiderplan.representation.expressions.domain.Substitution;
import org.spiderplan.representation.expressions.interfaces.Assertable;
import org.spiderplan.representation.expressions.interfaces.Matchable;
import org.spiderplan.representation.expressions.interfaces.Mutable;
import org.spiderplan.representation.expressions.interfaces.Substitutable;
import org.spiderplan.representation.expressions.misc.Assertion;
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
public class InteractionConstraint extends Expression implements Substitutable, Matchable, Mutable, Assertable {
	
	private Term name;
	
	private ConstraintDatabase Condition = new ConstraintDatabase();
	private ArrayList<ConstraintDatabase> Resolvers = new ArrayList<ConstraintDatabase>();
	
	private int pickedResolver = -1;
	
	private boolean isAsserted = false;
	
	/**
	 * Create a new interaction constraint by providing its name.
	 * 
	 * @param name the name
	 */
	public InteractionConstraint( Term name ) {
		super(ExpressionTypes.Interaction);
		this.name = name;
	}

	/**
	 * Get the name of this interaction constraint.
	 * @return the name
	 */
	public Term getName() { return name; };
	
	/**
	 * Set the condition of this interaction constraint.
	 * @param C the condition
	 */
	public void setCondition( ConstraintDatabase C ) {
		this.Condition = C;
	}
	/**
	 * Add a resolver to this interaction constraint.
	 * @param R a resolver
	 */
	public void addResolver( ConstraintDatabase R ) {
		this.Resolvers.add(R);
	}
	
	/**
	 * Get the condition of this interaction constraint.
	 * @return the condition
	 */
	public ConstraintDatabase getCondition( ) {
		return this.Condition;
	}
	
	/**
	 * Get the resolvers of this interaction constraint.
	 * @return the list of resolvers
	 */
	public List<ConstraintDatabase> getResolvers() {
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
	
	/**
	 * Get assertion that marks this IC as satisfied
	 * @return the assertion
	 */
	public Assertion getAssertion() {
		Term resolver;
		if ( this.pickedResolver != -1 ) {
			resolver = Term.createComplex("resolver", Term.createInteger(this.getResolverIndex()));
		} else {
			resolver = Term.createComplex("resolver", Term.createVariable("Unknown_"+UniqueID.getID()));
		}
		Assertion assertion = new Assertion(super.getType(), name, resolver);
		return assertion;
	}
			
	/**
	 * Replace all interval terms that are variables by unique ground terms.
	 * Used to assure that intervals added by resolvers do not exist in the 
	 * constraint database they are added to.
	 */
	public void makeUniqueGroundKeys() {
		long ID = UniqueID.getID();
		Substitution sub = new Substitution();
		for ( Statement s : this.getCondition().get(Statement.class) ) {
			if ( s.getKey().isVariable() ) {
				sub.add(s.getKey(), Term.createConstant(s.getKey().toString().replace("?", "") + "#" + ID));
			}
		}
		for ( OpenGoal s : this.getCondition().get(OpenGoal.class) ) {
			if ( s.getStatement().getKey().isVariable() ) {
				sub.add(s.getStatement().getKey(), Term.createConstant(s.getStatement().getKey().toString().replace("?", "") + "#" + ID));
			}
		}
		for ( ConstraintDatabase resolver : this.getResolvers() ) {
			for ( Statement s : resolver.get(Statement.class) ) {
				if ( s.getKey().isVariable() ) {
					sub.add(s.getKey(), Term.createConstant(s.getKey().toString().replace("?", "") + "#" + ID));
				}
			}
			for ( OpenGoal s : resolver.get(OpenGoal.class) ) {
				if ( s.getStatement().getKey().isVariable() ) {
					sub.add(s.getStatement().getKey(), Term.createConstant(s.getStatement().getKey().toString().replace("?", "") + "#" + ID));
				}
			}
		}
		this.substitute(sub);
	}
	
	@Override
	public boolean isAssertable() { return true; }
	
	@Override
	public boolean isMatchable() { return true; }	
	
	@Override
	public boolean isMutable() { return true; }
	
	@Override
	public boolean isSubstitutable() { return true; }
		
//	@Override
//	public Collection<Term> getVariableTerms() {
//		ArrayList<Term> r = new ArrayList<Term>();
//		r.addAll(this.Condition.getVariableTerms());
//		for ( ConstraintDatabase res : this.Resolvers ) {
//			r.addAll(res.getVariableTerms());
//		}
//		r.addAll(this.name.getVariableTerms());
//		return r;		
//	}
//	@Override
//	public Collection<Term> getGroundTerms() {
//		ArrayList<Term> r = new ArrayList<Term>();
//		r.addAll(this.Condition.getGroundTerms());
//		for ( ConstraintDatabase res : this.Resolvers ) {
//			r.addAll(res.getGroundTerms());
//		}
//		r.addAll(this.name.getGroundTerms());
//		return r;		
//	}
//	@Override
//	public Collection<Term> getComplexTerms() {
//		Set<Term> r = new HashSet<Term>();
//		r.add(this.name);
//		r.addAll(this.Condition.getAtomics());
//		for ( ConstraintDatabase resolver : this.Resolvers ) {
//			r.addAll(resolver.getAtomics());
//		}
//		return r;
//	}
	
	@Override
	public void getAllTerms(Collection<Term> collectedTerms, boolean getConstants, boolean getVariables, boolean getComplex) {
		this.name.getAllTerms(collectedTerms, getConstants, getVariables, getComplex);
		this.Condition.getAllTerms(collectedTerms, getConstants, getVariables, getComplex);
		for ( ConstraintDatabase resolver : this.Resolvers ) {
			resolver.getAllTerms(collectedTerms, getConstants, getVariables, getComplex);
		}
	}
		
	@Override
	public String toString() {
		StringBuilder r = new StringBuilder();
		r.append("(:ic\n\t" + this.name + "\n");
		r.append("\t(:condition\n");
		String s = "\t\t"+this.Condition.toString().toString().replace("\n", "\n\t\t");	
		r.append(s.substring(0, s.length()-2));
		r.append("\t)");
		
		for ( ConstraintDatabase c : this.Resolvers ) {
			r.append("\n\t(:resolver\n");
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
	public Expression substitute(Substitution theta) {
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
	public Substitution match(Expression c) {
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
	public Expression setAsserted(boolean asserted) {
		isAsserted = asserted;
		return this;
	}

	@Override
	public boolean appliesTo(Assertion assertion) {
		return assertion.getExpressionType().equals(super.getType()) && this.name.equals(assertion.getParameter(0));
	}
	
}
