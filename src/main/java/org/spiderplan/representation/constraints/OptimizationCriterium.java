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

import org.spiderplan.representation.constraints.constraintInterfaces.Mutable;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Term;


/**
 * Declares something to be optimized.
 * 
 * @author Uwe Köckemann
 *
 */
public class OptimizationCriterium extends Constraint implements Mutable {
	
	final private static Term ConstraintType = Term.createConstant("optimization");

	private Atomic optVarName;
	public enum OptDirection { Minimize, Maximize };
	private OptDirection dir;
	
	public OptimizationCriterium( Atomic optVarName, OptDirection dir ) {
		super(ConstraintType);
		this.optVarName = optVarName;
		this.dir = dir;
	}
	
	public Atomic getOptVar() { return optVarName; };
	public OptDirection getDir() { return dir; };
	
	@Override
	public Constraint copy() {
		Constraint c = new OptimizationCriterium(optVarName, dir);
		return c;
	}

	@Override
	public boolean equals(Object o) {
		if ( o instanceof OptimizationCriterium ) {
			OptimizationCriterium dC = (OptimizationCriterium)o;
			return this.optVarName.equals(optVarName) && this.dir.equals(dC.dir);
		}
		
		
		return false;
	}

	@Override
	public int hashCode() {
		return (optVarName.hashCode() + 31*dir.hashCode());
	}

	@Override
	public String toString() {
		return dir.toString() + " " + optVarName.toString();
	}

	@Override
	public Collection<Term> getVariableTerms() {
		Set<Term> r = new HashSet<Term>(); 
		r.addAll(optVarName.getVariableTerms());
		return r;
	}

	@Override
	public Collection<Term> getGroundTerms() {
		Set<Term> r = new HashSet<Term>(); 
		r.addAll(optVarName.getGroundTerms());
		return r;
	}
	@Override
	public Collection<Atomic> getAtomics() {
		ArrayList<Atomic> r = new ArrayList<Atomic>();
		r.add(optVarName);
		return r;		
	}
	
	

}
