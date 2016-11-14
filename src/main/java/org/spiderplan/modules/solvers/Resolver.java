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
package org.spiderplan.modules.solvers;

import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.expressions.domain.Substitution;
import org.spiderplan.representation.expressions.misc.Asserted;
import org.spiderplan.representation.expressions.misc.Delete;

/**
 * Describes a way to change a {@link ConstraintDatabase}
 * by performing a {@link Substitution} and adding another {@link ConstraintDatabase}.
 * Resolvers are proposed by solvers and impose decisions made by them.
 * 
 * @author Uwe Köckemann
 */
public class Resolver {
	private Substitution s;
	private ConstraintDatabase cDB;
	
	/**
	 * Create a new resolver by providing a {@link ConstraintDatabase}
	 * @param cDB The {@link ConstraintDatabase} that will be added to a context when applying this resolver
	 */
	public Resolver( ConstraintDatabase cDB ) {
		this(new Substitution(),cDB);
	}
	
	/**
	 * Create a new resolver by providing a {@link Substitution} and a {@link ConstraintDatabase}
	 * @param s The {@link Substitution} that will be applied to a context when applying this resolver
	 */
	public Resolver( Substitution s ) {
		this(s,new ConstraintDatabase());
	}
	
	/**
	 * Create a new resolver by providing a {@link Substitution} and a {@link ConstraintDatabase}
	 * @param s The {@link Substitution} that will be applied to a context when applying this resolver
	 * @param cDB The {@link ConstraintDatabase} that will be added to a context when applying this resolver
	 */
	public Resolver( Substitution s , ConstraintDatabase cDB ) {
		this.s = s;
		this.cDB = cDB;
	}
	
	
	/**
	 * Access the {@link Substitution}.
	 * @return The {@link Substitution}
	 */
	public Substitution getSubstitution() {
		return s;
	}
	
	/**
	 * Access the {@link ConstraintDatabase}.
	 * @return The {@link ConstraintDatabase}
	 */
	public ConstraintDatabase getConstraintDatabase() {
		return cDB;
	}
	
	/**
	 * Apply this resolver to change a context. Also apply {@link Asserted} constraints.
	 * 
	 * @param context The {@link ConstraintDatabase} to which this resolver is applied.
	 */
	public void apply( ConstraintDatabase context ) {
		if ( cDB != null )
			context.add(cDB.copy());
		if ( s != null && !s.isEmpty() )
			context.substitute(s);

		for ( Delete d : context.get(Delete.class) ) {
			if ( !d.isAsserted() ) {
				d.setAsserted(true);
				d.apply(context);
			}
		}
		
		for ( Asserted a : context.get(Asserted.class) ) {
			if ( !a.isAsserted() ) {
				a.setAsserted(true);
				context.processAsserted(a);
//				a.apply(context);
			}
		}
	}
	
	/**
	 * Add content of another {@link Resolver} to this one
	 * @param r Resolver to be added to this one
	 */
	public void add( Resolver r ) {
		this.s.add(r.s);
		this.cDB.add(r.cDB);
	}
	
	/**
	 * Returns a copy of this resolver
	 * @return A copy that can be changed without changing the original
	 */
	public Resolver copy() {
		return new Resolver(this.s.copy(), this.cDB.copy());
	}
	
	@Override
	public String toString() {
		return s + "\n" + cDB;
	}
}
